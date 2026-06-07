// 테스트 환경: http://localhost:8080 (dev) / https://api.piroin.com (prod)
// 작성 기준: application.yml, SecurityConfig.java, *Controller.java
// SSE 구현: SseEmitter
// 대상: 최대 40 VU, 약 16분, 정상 부하 시뮬레이션
// 이미지 업로드: VU 중 20% 확률로 POST /api/images 수행

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, THRESHOLDS, login, getHeadersWithToken, SESSION_ID, uploadTestImage } from './config.js';

export const options = {
  stages: [
    { duration: '2m', target: 15 },
    { duration: '5m', target: 15 },
    { duration: '2m', target: 40 },
    { duration: '5m', target: 40 },
    { duration: '2m', target: 0 },
  ],
  thresholds: THRESHOLDS.load,
};

export function setup() {
  return { token: login() };
}

export default function (data) {
  const headers = getHeadersWithToken(data.token);

  group('읽기 — 병렬 GET', () => {
    const responses = http.batch([
      ['GET', `${BASE_URL}/api/curriculums`, null, { headers }],
      ['GET', `${BASE_URL}/api/sessions`, null, { headers }],
      ['GET', `${BASE_URL}/api/attendance/user`, null, { headers }],
      ['GET', `${BASE_URL}/api/deposit/me`, null, { headers }],
    ]);

    check(responses[0], { '커리큘럼 200': (r) => r.status === 200 });
    check(responses[1], { '세션 목록 200': (r) => r.status === 200 });
    check(responses[2], { '출석 200': (r) => r.status === 200 });
    check(responses[3], { '보증금 200': (r) => r.status === 200 });

    responses.forEach((r, i) => {
      if (r.status !== 200) {
        console.log(`batch[${i}] 실패: status=${r.status} body=${r.body?.slice(0, 200)}`);
      }
    });
  });

  sleep(Math.random() * 0.5 + 0.5);

  group('질문 조회', () => {
    const questions = http.get(
      `${BASE_URL}/api/sessions/${SESSION_ID}/questions?understandingIndex=0`,
      { headers }
    );
    check(questions, { '질문 목록 200': (r) => r.status === 200 });

    try {
      const list = questions.json();
      if (Array.isArray(list) && list.length > 0) {
        const qId = list[0].id || list[0].questionId;
        if (qId) {
          const detail = http.get(`${BASE_URL}/api/questions/${qId}`, { headers });
          check(detail, { '질문 상세 200': (r) => r.status === 200 });
        }
      }
    } catch { /* 파싱 실패 무시 */ }
  });

  sleep(Math.random() * 0.5 + 0.5);

  group('과제 조회', () => {
    const week = Math.floor(Math.random() * 8) + 1;
    const assignment = http.get(`${BASE_URL}/api/assignments/me/${week}`, { headers });
    check(assignment, { '과제 조회 200': (r) => r.status === 200 });
  });

  sleep(Math.random() * 0.5 + 0.5);

  // QnA 사진 첨부 시뮬레이션 — 20% 확률 (40명 중 약 8명이 동시 업로드)
  if (Math.random() < 0.2) {
    group('이미지 업로드', () => {
      const imgRes = uploadTestImage(data.token);
      check(imgRes, {
        '이미지 업로드 200': (r) => r.status === 200,
        'imageUrl 포함': (r) => (r.json('imageUrl') || '').length > 0,
      });
      if (imgRes.status !== 200) {
        console.log(`이미지 업로드 실패: status=${imgRes.status} body=${imgRes.body}`);
      }
    });
  }

  sleep(Math.random() * 0.5 + 0.5);
}
