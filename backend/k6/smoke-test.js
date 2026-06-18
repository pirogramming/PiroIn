// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, *Controller.java
// SSE 구현: SseEmitter
// 대상: 1 VU, 2분, 배포 직후 기본 동작 확인

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, THRESHOLDS, login, getHeadersWithToken, SESSION_ID, uploadTestImage } from './config.js';

export const options = {
  vus: 1,
  duration: '2m',
  thresholds: THRESHOLDS.smoke,
};

export function setup() {
  return { token: login() };
}

export default function (data) {
  const headers = getHeadersWithToken(data.token);

  // 1. 헬스 체크 (public)
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, {
    '헬스 체크 200': (r) => r.status === 200,
    '헬스 UP': (r) => r.json('status') === 'UP',
  });
  sleep(0.5);

  // 2. 커리큘럼 목록
  const curriculum = http.get(`${BASE_URL}/api/curriculums`, { headers });
  check(curriculum, {
    '커리큘럼 200': (r) => r.status === 200,
    '커리큘럼 배열': (r) => Array.isArray(r.json()),
  });
  if (curriculum.status !== 200) {
    console.log(`커리큘럼 실패: status=${curriculum.status} body=${curriculum.body}`);
  }
  sleep(0.5);

  // 3. QnA 세션 목록
  const sessions = http.get(`${BASE_URL}/api/sessions`, { headers });
  check(sessions, { '세션 목록 200': (r) => r.status === 200 });
  sleep(0.5);

  // 4. 질문 목록
  const questions = http.get(
    `${BASE_URL}/api/sessions/${SESSION_ID}/questions?understandingIndex=0`,
    { headers }
  );
  check(questions, { '질문 목록 200': (r) => r.status === 200 });
  sleep(0.5);

  // 5. 나의 출석 현황
  const attendance = http.get(`${BASE_URL}/api/attendance/user`, { headers });
  check(attendance, { '출석 200': (r) => r.status === 200 });
  sleep(0.5);

  // 6. 이미지 업로드 (QnA 사진 첨부 시뮬레이션)
  const imgRes = uploadTestImage(data.token);
  check(imgRes, {
    '이미지 업로드 200': (r) => r.status === 200,
    'imageUrl 포함': (r) => (r.json('imageUrl') || '').length > 0,
  });
  if (imgRes.status !== 200) {
    console.log(`이미지 업로드 실패: status=${imgRes.status} body=${imgRes.body}`);
  }

  sleep(1);
}
