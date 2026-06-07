// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, *Controller.java
// SSE 구현: SseEmitter
// 대상: 최대 120 VU (40명 기준 3배), 약 35분, 한계 부하 탐색
// ⚠️ HikariCP pool-size=10 → 120 VU 시 DB 커넥션 병목 관측 예상 (의도된 관측)

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, login, getHeadersWithToken, SESSION_ID } from './config.js';

const errorCount = new Counter('custom_errors');

export const options = {
  stages: [
    { duration: '2m', target: 20 },
    { duration: '5m', target: 20 },
    { duration: '2m', target: 40 },   // 정상 부하
    { duration: '5m', target: 40 },
    { duration: '2m', target: 80 },   // 2배
    { duration: '5m', target: 80 },
    { duration: '2m', target: 120 },  // 3배, 한계 탐색
    { duration: '5m', target: 120 },
    { duration: '5m', target: 0 },    // 회복 확인
  ],
  thresholds: {
    ...THRESHOLDS.stress,
    custom_errors: ['count<100'],
  },
};

export function setup() {
  return { token: login() };
}

export default function (data) {
  const headers = getHeadersWithToken(data.token);

  const cur = http.get(`${BASE_URL}/api/curriculums`, { headers });
  const ok = check(cur, {
    '커리큘럼 200': (r) => r.status === 200,
    '응답 3s 이내': (r) => r.timings.duration < 3000,
  });
  if (!ok || cur.status !== 200) {
    errorCount.add(1);
    if (cur.timings.duration >= 3000) {
      console.log(`응답 지연: ${cur.timings.duration}ms status=${cur.status}`);
    }
  }

  sleep(0.5);

  const att = http.get(`${BASE_URL}/api/attendance/user`, { headers });
  check(att, { '출석 200': (r) => r.status === 200 });
  if (att.status !== 200) errorCount.add(1);

  sleep(0.5);

  const q = http.get(
    `${BASE_URL}/api/sessions/${SESSION_ID}/questions?understandingIndex=0`,
    { headers }
  );
  check(q, { '질문 목록 200': (r) => r.status === 200 });
  if (q.status !== 200) errorCount.add(1);

  sleep(0.5);
}

export function teardown() {
  console.log('Stress test 완료 — 회복 구간(마지막 5분) 에러율을 결과에서 확인할 것');
}
