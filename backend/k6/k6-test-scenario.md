# k6 부하 테스트 시나리오

> **Claude에게**: 이 문서를 읽고 아래 지시사항에 따라 k6 테스트 스크립트를 직접 작성해줘.
> 프로젝트 디렉토리를 먼저 탐색하고, 실제 API 엔드포인트·인증 방식·환경변수를 확인한 뒤 스크립트를 생성해.

---

## 0. 전제 조건

- **대상 유저 수**: 약 40명 (동아리 실사용 규모 — 회원 30명 + 운영진 5~7명)
- **접근 제어**: 로그인된 회원만 접근 가능 → 최대 동시 접속자 고정
- **SSE 구현**: SseEmitter (Spring WebMVC) — Tomcat 스레드 1개 = SSE 연결 1개 점유
- **이미지 업로드**: QnA 질문/댓글 작성 시 `POST /api/images` (multipart/form-data) 동시 발생
- **HikariCP pool-size**: 10 (운영 기준 적절, Stress 시 병목 관측 목적)
- **Tomcat threads.max**: 200

---

## 1. 공통 설정 (`config.js`)

```javascript
export const BASE_URL  = __ENV.BASE_URL || 'http://localhost:8080';

// 인증: JWT Bearer Token
// k6 실행 시 -e USER_NAME=xxx -e USER_PASSWORD=xxx 로 주입
// 또는 -e API_TOKEN=xxx 로 직접 주입

// 이미지 업로드: POST /api/images (multipart/form-data, file 파라미터)
// 테스트용 1x1 JPEG를 base64로 인라인 포함 (외부 파일 불필요)
export function uploadTestImage(token) { ... }

export const THRESHOLDS = {
  smoke:  { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<500']  },
  load:   { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000', 'p(99)<2000'] },
  stress: { http_req_failed: ['rate<0.05'], http_req_duration: ['p(95)<3000'] },
  soak:   { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000'] },
  sse:    { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<2000'] },
};
```

---

## 2. 테스트별 시나리오 정의

### 2-1. Smoke Test (`smoke-test.js`) — 기본 동작 확인

| 항목 | 값 |
|------|-----|
| 목적 | 배포 후 API 정상 동작 확인 |
| VU | 1명 |
| 시간 | 2분 |
| 통과 기준 | 에러 0%, p(95) < 500ms |
| 실행 시점 | 배포 직후, 매 PR 머지 후 |

**테스트 엔드포인트**

```
GET  /actuator/health                                        — 헬스 체크 (public)
GET  /api/curriculums                                        — 커리큘럼 목록
GET  /api/sessions                                           — QnA 세션 목록
GET  /api/sessions/{sessionId}/questions?understandingIndex=0 — 질문 목록
GET  /api/attendance/user                                    — 나의 출석 현황
POST /api/images                                             — 이미지 업로드 (multipart)
```

---

### 2-2. Load Test (`load-test.js`) — 정상 부하 시뮬레이션

| 항목 | 값 |
|------|-----|
| 목적 | 40명 동시 사용 처리 능력 검증 |
| VU | 최대 40명 |
| 시간 | 약 16분 |
| 통과 기준 | 에러율 < 1%, p(95) < 1s, p(99) < 2s |

**stages**

```
2m → 15 VU  (워밍업)
5m → 15 VU  (유지)
2m → 40 VU  (전체 유저 시뮬레이션)
5m → 40 VU  (유지)
2m → 0 VU   (종료)
```

**시나리오 흐름**

```
그룹1 — 읽기 (병렬 GET)
  GET /api/curriculums
  GET /api/sessions
  GET /api/attendance/user
  GET /api/deposit/me

그룹2 — 질문 조회 + 단건 조회

그룹3 — 과제 조회 (주차 랜덤)

그룹4 — 이미지 업로드 (POST /api/images, multipart)
  → VU 중 20%만 이미지 업로드 수행 (Math.random() < 0.2)
  → QnA 진행 시 사진 올리는 상황 시뮬레이션
```

---

### 2-3. Stress Test (`stress-test.js`) — 한계 부하 탐색

| 항목 | 값 |
|------|-----|
| 목적 | 40명 기준의 3배 부하로 병목 지점 파악 |
| VU | 최대 120명 |
| 시간 | 약 35분 |
| 통과 기준 | 에러율 < 5%, 회복 구간 에러 0% 복귀 |

**stages**

```
2m → 20 VU
5m → 20 VU
2m → 40 VU   (정상 부하)
5m → 40 VU
2m → 80 VU   (2배)
5m → 80 VU
2m → 120 VU  (3배, 한계 탐색)
5m → 120 VU
5m → 0 VU    (회복 확인)
```

---

### 2-4. SSE Test (`sse-test.js`) — 스트리밍 연결 부하 검증

| 항목 | 값 |
|------|-----|
| 목적 | SSE 동시 연결 + 일반 API 영향도 측정 |
| VU | SSE 30명 + 일반 API 10명 (총 40명) |
| 시간 | 약 11분 |
| 통과 기준 | 연결 수립 p(95) < 2s, 일반 API p(95) < 1s |

**시나리오**

```
시나리오 A (sse_connections, 30 VU)
  GET /api/sessions/{sessionId}/questions/events
  Accept: text/event-stream, timeout: 10s
  → sse_connect_time: res.timings.waiting (TTFB) 측정
  → timeout이어도 body에 'connected' 포함 시 성공으로 처리
  → 재연결 반복 (시나리오 C)

시나리오 B (normal_api, 10 VU, startTime: 30s)
  GET /api/curriculums
  GET /api/attendance/user
  GET /api/sessions/{sessionId}/questions
  POST /api/images  (30% 확률 — 이미지 업로드 병행)
  → SSE 중 일반 API 응답시간이 올라가면 스레드 고갈 신호
```

**k6 SSE 측정 방식**

```
sse_connect_time: res.timings.waiting (TTFB) — 실제 첫 이벤트 수신 시간만 측정
sse_error: timeout이어도 body에 'connected' 있으면 성공, 4xx/5xx만 진짜 오류로 카운트
```

**⚠️ SseEmitter 주의**

```
Tomcat threads.max=200
SSE 30개 + 일반 10개 = 40 스레드 → 안전 범위
```

---

### 2-5. Soak Test (`soak-test.js`) — 장시간 안정성 검증

| 항목 | 값 |
|------|-----|
| 목적 | 메모리 누수, 커넥션 풀 고갈, SSE emitter 미정리 감지 |
| VU | 20명 지속 |
| 시간 | 120분 |
| 통과 기준 | 에러율 < 1%, 30분 경과 후에도 p(95) 유지 |

**stages**

```
5m  → 20 VU  (워밍업)
110m → 20 VU (장시간 유지)
5m  → 0 VU   (종료)
```

**감지 항목**

```
- 연속 실패 5회 → HikariCP 고갈 경고
- 30분 경과 후 응답 > 1s → 메모리 누수 또는 DB 부하 의심
- 이미지 업로드 (20% 확률) — 파일 핸들 누수 감지
```

---

## 3. 이미지 업로드 시나리오

```
엔드포인트: POST /api/images
Content-Type: multipart/form-data
파라미터: file (이미지 파일)
응답: { "imageUrl": "/api/images/{filename}" }
인증: Bearer JWT 필요

테스트 전략:
- load-test: VU 중 20% (랜덤)가 이미지 업로드 수행
- sse-test: normal_api VU 중 30%가 이미지 업로드 수행
- soak-test: 20% 확률로 이미지 업로드 — 파일 핸들 누수 장시간 감지
- 테스트용 이미지: 1x1 JPEG (base64 인라인, 외부 파일 불필요)
```

---

## 4. 실행 방법

```bash
# Smoke
k6 run -e USER_NAME=이름 -e USER_PASSWORD=비번 -e SESSION_ID=1 k6/smoke-test.js

# Load
k6 run -e USER_NAME=이름 -e USER_PASSWORD=비번 -e SESSION_ID=1 k6/load-test.js

# Stress
k6 run -e USER_NAME=이름 -e USER_PASSWORD=비번 -e SESSION_ID=1 k6/stress-test.js

# SSE
k6 run -e USER_NAME=이름 -e USER_PASSWORD=비번 -e SESSION_ID=1 k6/sse-test.js

# Soak (백그라운드 권장)
k6 run -e USER_NAME=이름 -e USER_PASSWORD=비번 -e SESSION_ID=1 k6/soak-test.js &
```

---

## 5. 테스트 순서 체크리스트

```
[ ] 1. Smoke  → 에러 0%, 이미지 업로드 포함 기본 동작 확인
[ ] 2. Load   → 40명 p(95) < 1s, 이미지 업로드 20% 혼합
[ ] 3. SSE    → SSE 30개 + 일반 API 10개, 스레드 고갈 없음 확인
[ ] 4. Stress → 120명, 한계 VU 기록
[ ] 5. Soak   → 2시간, 이미지 업로드 포함 메모리 누수 없음 확인
```

---

## 6. Spring Boot 설정 현황

```yaml
# 현재 설정 (application.yml 기준)
spring:
  datasource:
    hikari:
      maximum-pool-size: 10       # 40명 운영에는 적절
      connection-timeout: 30000   # Stress 120명 시 병목 관측 포인트

server:
  tomcat:
    threads:
      max: 200
      min-spare: 20

# ⚠️ Stress(120 VU) 시 HikariCP 병목 예상 → 관측 목적으로 설정 그대로 유지
# ⚠️ SSE 30개 연결 시 스레드 30개 점유 → threads.max=200 기준 안전
```
