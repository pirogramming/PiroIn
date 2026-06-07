// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, *Controller.java
// SSE 구현: SseEmitter
// 대상: 20 VU, 120분, 메모리 누수·HikariCP 고갈·파일 핸들 누수 감지

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, login, getHeadersWithToken, SESSION_ID, uploadTestImage } from './config.js';

const slowResponses = new Trend('slow_response_trend');
const dbErrorCount  = new Counter('db_error_count');

let consecutiveFails = 0;

export const options = {
  stages: [
    { duration: '5m',   target: 20 },
    { duration: '110m', target: 20 },
    { duration: '5m',   target: 0 },
  ],
  thresholds: {
    ...THRESHOLDS.soak,
    db_error_count: ['count<20'],
  },
};

export function setup() {
  return { token: login(), startTime: Date.now() };
}

export default function (data) {
  const headers = getHeadersWithToken(data.token);
  const elapsed = (Date.now() - data.startTime) / 1000 / 60;

  const cur = http.get(`${BASE_URL}/api/curriculums`, { headers });
  const curOk = check(cur, {
    '커리큘럼 200': (r) => r.status === 200,
    '1s 이내': (r) => r.timings.duration < 1000,
  });

  if (!curOk) {
    consecutiveFails++;
    dbErrorCount.add(1);
    if (consecutiveFails >= 5) {
      console.warn(`⚠️ DB 커넥션 고갈 의심 — 연속 실패 ${consecutiveFails}회 (${elapsed.toFixed(1)}분 경과)`);
    }
  } else {
    consecutiveFails = 0;
  }

  if (cur.timings.duration > 1000) {
    slowResponses.add(cur.timings.duration);
    console.log(`응답 지연: ${cur.timings.duration}ms (${elapsed.toFixed(1)}분 경과)`);
  }

  sleep(1);

  const att = http.get(`${BASE_URL}/api/attendance/user`, { headers });
  check(att, { '출석 200': (r) => r.status === 200 });
  if (att.status !== 200) dbErrorCount.add(1);

  sleep(1);

  const q = http.get(
    `${BASE_URL}/api/sessions/${SESSION_ID}/questions?understandingIndex=0`,
    { headers }
  );
  check(q, { '질문 목록 200': (r) => r.status === 200 });
  if (elapsed >= 30 && q.timings.duration > 1000) {
    console.warn(`⚠️ 30분 경과 후 응답 저하: ${q.timings.duration}ms — 메모리 누수 또는 DB 부하 의심`);
  }

  sleep(1);

  // 이미지 업로드 20% 확률 — 파일 핸들 누수 장시간 감지
  if (Math.random() < 0.2) {
    const imgRes = uploadTestImage(data.token);
    check(imgRes, { '이미지 업로드 200': (r) => r.status === 200 });
    if (imgRes.status !== 200) {
      console.log(`이미지 업로드 실패 (${elapsed.toFixed(1)}분 경과): status=${imgRes.status}`);
    }
  }

  sleep(2);
}
