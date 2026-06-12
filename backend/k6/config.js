// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, *Controller.java
// SSE 구현: SseEmitter (Spring WebMVC, Tomcat 스레드 점유 방식)
// 대상 유저 수: 약 40명

import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';

export const BASE_URL  = __ENV.BASE_URL || 'http://localhost:8080';
export const SESSION_ID = __ENV.SESSION_ID || '1';

export const CREDENTIALS = {
  name:     __ENV.USER_NAME     || 'test_user',
  password: __ENV.USER_PASSWORD || 'test_password',
};

export const AUTH = {
  type:  'bearer',
  token: __ENV.API_TOKEN || '',
};

export function getHeaders(withAuth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (withAuth && AUTH.token) {
    headers['Authorization'] = `Bearer ${AUTH.token}`;
  }
  return headers;
}

export function getHeadersWithToken(token) {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

export function login() {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ name: CREDENTIALS.name, password: CREDENTIALS.password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { '로그인 성공': (r) => r.status === 200 });
  try {
    return res.json('token') || '';
  } catch {
    return '';
  }
}

// 테스트용 1x1 JPEG (base64 인라인 — 외부 파일 불필요)
const TEST_IMAGE_B64 =
  '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkS' +
  'Ew8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/wAAR' +
  'CAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAA' +
  'AAAAAAAAAAAAAP/EABQBAQAAAAAAAAAAAAAAAAAAAAD/xAAUEQEAAAAAAAAAAAAA' +
  'AAAAAAAA/9oADAMBAAIRAxEAPwCwAB//2Q==';

// QnA 이미지 업로드: POST /api/images (multipart/form-data)
// Authorization 헤더만 포함 — Content-Type은 k6가 multipart로 자동 설정
export function uploadTestImage(token) {
  const imageBytes = encoding.b64decode(TEST_IMAGE_B64, 'std', 'b');
  const data = {
    file: http.file(imageBytes, 'test.jpg', 'image/jpeg'),
  };
  return http.post(`${BASE_URL}/api/images`, data, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

// ⚠️ HikariCP pool-size=10, Tomcat threads.max=200
// 40명 운영에는 pool-size=10 적절
// Stress(120 VU) 시 DB 커넥션 병목 관측 예상 — 의도된 설정
export const THRESHOLDS = {
  smoke:  { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<500'] },
  load:   { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000', 'p(99)<2000'] },
  stress: { http_req_failed: ['rate<0.05'], http_req_duration: ['p(95)<3000'] },
  soak:   { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000'] },
  sse:    { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<2000'] },
};
