// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, QuestionController.java, QuestionEventService.java
// SSE 구현: SseEmitter (Spring WebMVC)
// 대상: SSE 30 VU + 일반 API 10 VU = 총 40명
//
// ⚠️ k6 SSE 측정 방식
//   http.get()은 연결이 닫혀야 status를 확정함
//   SSE는 서버가 3분간 연결 유지 → k6 timeout(10s) 후 status=0으로 기록됨
//   따라서:
//   - sse_connect_time: res.timings.waiting (TTFB, 첫 바이트 수신 시간) 사용
//   - sse_error: timeout이어도 body에 'connected' 포함 시 성공으로 처리
//
// ⚠️ SseEmitter 주의사항
//   Tomcat threads.max=200, SSE 30개 + 일반 10개 = 40 스레드 → 안전 범위
//   SSE timeout=3분 (QuestionEventService.SSE_TIMEOUT_MILLIS) 자동 해제

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, login, getHeadersWithToken, SESSION_ID, uploadTestImage } from './config.js';

const sseConnectTime = new Trend('sse_connect_time');   // TTFB (첫 바이트 수신 시간)
const sseEventCount  = new Counter('sse_event_received');
const sseErrorCount  = new Counter('sse_error');         // 진짜 서버 오류만 카운트

export const options = {
  scenarios: {
    sse_connections: {
      executor: 'constant-vus',
      vus: 30,
      duration: '10m',
      exec: 'sseScenario',
    },
    normal_api: {
      executor: 'constant-vus',
      vus: 10,
      duration: '10m',
      exec: 'apiScenario',
      startTime: '30s',
    },
  },
  thresholds: {
    // TTFB 기준 — 연결 수립 + 첫 이벤트 수신 시간
    sse_connect_time: ['p(95)<2000'],
    // 진짜 서버 오류만 카운트 (timeout 제외)
    sse_error: ['count<10'],
    'http_req_duration{scenario:normal_api}': ['p(95)<1000'],
  },
};

export function setup() {
  return { token: login() };
}

export function sseScenario(data) {
  const params = {
    headers: {
      ...getHeadersWithToken(data.token),
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache',
    },
    timeout: '10s',  // connected 이벤트 수신에 충분한 시간, 전체 연결 유지 불필요
  };

  const res = http.get(
    `${BASE_URL}/api/sessions/${SESSION_ID}/questions/events`,
    params
  );

  const body = res.body || '';
  const receivedConnected = body.includes('connected');
  const isTimeout = res.status === 0;

  // TTFB — 실제 서버가 첫 바이트를 보낼 때까지 걸린 시간
  if (res.timings.waiting > 0) {
    sseConnectTime.add(res.timings.waiting);
  }

  if (res.status === 200 || (isTimeout && receivedConnected)) {
    // 정상 연결: 200 응답 또는 timeout이어도 connected 이벤트 수신한 경우
    const events = body.split('\n').filter(l => l.startsWith('data:'));
    sseEventCount.add(events.length);

    check(res, {
      'SSE connected 이벤트 수신': () => receivedConnected,
      'Content-Type text/event-stream': (r) =>
        r.status === 200
          ? (r.headers['Content-Type'] || '').includes('text/event-stream')
          : true, // timeout 시 헤더 미수신 허용
    });
  } else {
    // 진짜 서버 오류 (4xx, 5xx, 네트워크 오류)
    sseErrorCount.add(1);
    console.log(`SSE 연결 실패: status=${res.status} body=${body.slice(0, 200)}`);
  }

  sleep(1);
}

export function apiScenario(data) {
  const headers = getHeadersWithToken(data.token);

  const responses = http.batch([
    ['GET', `${BASE_URL}/api/curriculums`, null, { headers }],
    ['GET', `${BASE_URL}/api/attendance/user`, null, { headers }],
    ['GET', `${BASE_URL}/api/sessions/${SESSION_ID}/questions?understandingIndex=0`, null, { headers }],
  ]);

  check(responses[0], { '[SSE중] 커리큘럼 200': (r) => r.status === 200 });
  check(responses[1], { '[SSE중] 출석 200': (r) => r.status === 200 });
  check(responses[2], { '[SSE중] 질문 목록 200': (r) => r.status === 200 });

  responses.forEach((r, i) => {
    if (r.status !== 200) {
      console.log(`⚠️ SSE 중 일반 API 실패[${i}]: status=${r.status} — 스레드 고갈 의심`);
    }
  });

  sleep(0.5);

  if (Math.random() < 0.3) {
    const imgRes = uploadTestImage(data.token);
    check(imgRes, { '[SSE중] 이미지 업로드 200': (r) => r.status === 200 });
    if (imgRes.status !== 200) {
      console.log(`⚠️ SSE 중 이미지 업로드 실패: status=${imgRes.status}`);
    }
  }

  sleep(1);
}
