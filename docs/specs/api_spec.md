# KAIROS — API 명세서

> **Version**: 2.6
**작성일**: 2026-02-12
**기준**: PRD v10.0, 기능명세서 v2.3, 데이터 모델 명세서 v2.0
**서버 프레임워크**: FastAPI (Python 3.11+)
**Base URL**: `https://api.kairos.app/api/v1`
>

---

## 변경 이력

| 버전 | 날짜 | 주요 변경 내용 |
| --- | --- | --- |
| 1.0 | 2026-02-06 | 초기 작성 (PRD v9.2 기준) |
| 2.0 | 2026-02-07 | PRD v10.0 + 기능명세서 v2.1 + 데이터 모델 v2.0 반영. (1) 분류 체계 변경 — classified_type을 SCHEDULE/TODO/NOTES/TEMP 4개로 재편, IDEA를 NOTES 하위 note_sub_type으로 이동, (2) classify 응답에 note_sub_type 필드 추가, (3) classify 응답에 split_items[] 추가 (다중 의도 분리, Phase 2b), (4) Analytics 이벤트 유형 업데이트 — classification_confirmed/trash_restored/split_capture_created 추가, inbox_item_resolved → temp_item_resolved 변경, classification_modified에 sub_type 필드 추가, (5) Notes API에 Inbox AI 자동 분류 엔드포인트 추가 (Phase 3a), (6) Phase 배정 조정 — 멀티 인텐트 분할 Phase 2b, 외부 공유 Phase 3b, (7) 문서 기준 버전 갱신 |
| 2.1 | 2026-02-07 | Phase 2a 캘린더 토큰 저장 경로 추가. (1) POST /calendar/token 추가 — 디바이스별 Google OAuth 토큰 서버 저장, (2) 캘린더 API 섹션/요약 테이블 갱신 |
| 2.2 | 2026-02-07 | Phase 2a OAuth 코드 교환 경로 추가. (1) POST /calendar/token/exchange 추가 — authorization code를 토큰으로 교환 후 저장, (2) 캘린더 API 섹션/요약 테이블 갱신 |
| 2.3 | 2026-02-07 | PRD v10.0 우선 정렬. (1) user_context.modification_history를 Phase 2b로 조정, (2) 수정 이력 기반 학습(modification_learning) Phase를 3b→2b로 조정 |
| 2.4 | 2026-02-07 | PRD 충돌 정렬. (1) 수정 이력 기반 학습을 2b(초안)/3b(고도화)로 분리, (2) 분류 프리셋/사용자 지시를 3a로 조정, (3) AI 자동 그룹화 명칭을 AI 통합 그룹화로 통일 |
| 2.5 | 2026-02-10 | API 계약 정렬. (1) 구독 features에 `analytics_dashboard`/`ocr` 추가, (2) OCR 엔드포인트 추가 (`POST /ocr/extract`, JSON+Base64), (3) Auth refresh 응답 `user` nullable 명시, (4) Auth/me에 `google_calendar_connected` 필드 명시, (5) 시맨틱 검색/대시보드 엔드포인트 추가, (6) 요약 테이블 갱신 |
| 2.6 | 2026-02-12 | Android 클라이언트 기준 정렬. (1) 캘린더 연동이 CalendarProvider(로컬)로 전환되어 `/calendar/*`를 Android에서는 사용 중단, (2) 초기 동기화용 `/sync/push`, `/sync/pull` 엔드포인트를 Android 연동 대상으로 명시, (3) Google 로그인 표현을 OAuth 일반 표현에서 Google Sign-In(ID Token) + 서버 JWT로 정정 |

---

## 0.A 2026-02-12 Addendum (우선 적용)

본 문서의 기존 `Google Calendar API 프록시(/calendar/*)` 섹션은 서버 호환성(iOS 포함) 관점의 레거시 계약이다.  
Android 클라이언트 기준으로는 아래가 우선 적용된다.

- 캘린더 생성/조회/삭제: Android `CalendarProvider` 직접 사용
- `/calendar/token`, `/calendar/token/exchange`, `/calendar/events*`: Android에서 호출하지 않음
- 로그인: Google Sign-In(ID Token) -> `/auth/google` -> 서버 JWT 발급
- 초기 동기화: `/sync/push`, `/sync/pull` 사용

즉, Android 앱 동작 기준 계약은 본 Addendum + `/sync/*`, `/auth/*`, `/classify*`, `/analytics/events`를 기준으로 본다.

## 1. 아키텍처 개요

### 1.1 서버 역할

서버는 상태를 영구 저장하지 않는 처리 파이프라인이다. 핵심 데이터는 클라이언트 로컬 DB에 저장되며, 서버는 다음 역할만 담당한다.

| 역할 | 설명 | Phase |
| --- | --- | --- |
| AI 분류 | 텍스트 분석 → 유형(classified_type + note_sub_type)·제목·태그·엔티티 반환 | 1 |
| 캘린더 동기화 | Android: CalendarProvider 로컬 연동 / 서버: `/calendar/*` 레거시 유지(iOS 호환) | 2a |
| 분석 이벤트 수집 | 클라이언트 이벤트 배치 수신 | 2a |
| 노트 그룹화 | AI 기반 노트 그룹화·재정리 | 3a |
| Inbox AI 분류 | Inbox 노트 자동 폴더 배치 | 3a |
| 인증 | Google Sign-In(ID Token) 검증 + JWT 발급 | 3a |
| 구독 관리 | 구독 상태 확인·변경 | 3a |

### 1.2 인증

| Phase | 인증 방식 |
| --- | --- |
| 1 ~ 2b | 디바이스 ID 기반 식별 (Header: `X-Device-ID`) |
| 3a+ | JWT Bearer Token (Header: `Authorization: Bearer {token}`) |

### 1.3 공통 응답 형식

**성공 응답:**

```json
{
  "status": "ok",
  "data": { ... }
}
```

**에러 응답:**

```json
{
  "status": "error",
  "error": {
    "code": "CLASSIFICATION_FAILED",
    "message": "분류 처리 중 오류가 발생했습니다"
  }
}
```

### 1.4 공통 에러 코드

| HTTP | 코드 | 설명 |
| --- | --- | --- |
| 400 | INVALID_REQUEST | 요청 파라미터 오류 |
| 401 | UNAUTHORIZED | 인증 실패 (Phase 3a) |
| 403 | SUBSCRIPTION_REQUIRED | 구독 필요 기능 (Phase 3a) |
| 429 | RATE_LIMITED | 요청 한도 초과 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |
| 503 | AI_SERVICE_UNAVAILABLE | AI 모델 서비스 불가 |

---

## 2. AI 분류 API (Phase 1)

### 2.1 POST /classify — 단건 분류

캡처 텍스트를 AI가 분석하여 분류 결과를 반환한다.

**Request:**

```json
{
  "text": "금요일 19시 강남역 저녁 약속",
  "source": "APP",
  "device_id": "uuid-device-123",
  "user_context": {
    "modification_history": null,
    "preset_id": null,
    "custom_instruction": null
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| text | string | ✓ | 캡처 원문 (1~5000자) |
| source | string | ✓ | APP / SHARE_INTENT / WIDGET |
| device_id | string | ✓ | 디바이스 식별자 |
| user_context.modification_history | object? | | 수정 이력 요약 (Phase 2b 초안, Phase 3b 고도화) |
| user_context.preset_id | string? | | 분류 프리셋 ID (Phase 3a) |
| user_context.custom_instruction | string? | | 사용자 지시 텍스트 (Phase 3a) |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "classified_type": "SCHEDULE",
    "note_sub_type": null,
    "confidence": "HIGH",
    "ai_title": "금요일 19시 강남역 저녁 약속",
    "tags": ["약속", "저녁"],
    "entities": [
      {"type": "DATE", "value": "금요일", "normalized_value": "2026-02-13"},
      {"type": "TIME", "value": "19시", "normalized_value": "19:00"},
      {"type": "PLACE", "value": "강남역", "normalized_value": "강남역"}
    ],
    "schedule_info": {
      "start_time": "2026-02-13T19:00:00+09:00",
      "end_time": null,
      "location": "강남역",
      "is_all_day": false
    },
    "todo_info": null,
    "split_items": null
  }
}
```

| 응답 필드 | 타입 | 설명 |
| --- | --- | --- |
| classified_type | string | SCHEDULE / TODO / NOTES / TEMP |
| note_sub_type | string? | INBOX / IDEA / BOOKMARK. classified_type=NOTES일 때만 포함, 그 외 null |
| confidence | string | HIGH / MEDIUM / LOW |
| ai_title | string | AI 생성 요약 제목 (≤30자) |
| tags | string[] | 자동 태그 (0~5개) |
| entities | object[] | 추출 엔티티 목록 |
| entities[].type | string | PERSON / PLACE / DATE / TIME / AMOUNT / OTHER |
| entities[].value | string | 원문 표현 |
| entities[].normalized_value | string? | 정규화 값 |
| schedule_info | object? | SCHEDULE일 때만 포함 |
| schedule_info.start_time | string? | ISO 8601 |
| schedule_info.end_time | string? | ISO 8601 |
| schedule_info.location | string? | 장소 |
| schedule_info.is_all_day | boolean | 종일 이벤트 여부 |
| todo_info | object? | TODO일 때만 포함 |
| todo_info.deadline | string? | ISO 8601 마감일 |
| todo_info.deadline_source | string | AI_EXTRACTED / AI_SUGGESTED |
| todo_info.sub_type | string? | 확인 / 알아볼것 / null (일반 할 일) |
| split_items | object[]? | 다중 의도 분리 결과 (Phase 2b). null이면 단일 의도 |

**classified_type 열거형:**

| 값 | 설명 | 사용자 표시 |
| --- | --- | --- |
| TEMP | 미분류 임시 상태 (이미지만 첨부, 분류 실패 등) | (미표시) |
| SCHEDULE | 일정 | 일정 |
| TODO | 할 일 | 할 일 |
| NOTES | 노트 (note_sub_type으로 세분화) | 노트 / 아이디어 |

**note_sub_type 열거형 (classified_type=NOTES일 때):**

| 값 | 설명 | 기본 folder_id |
| --- | --- | --- |
| INBOX | 미분류 노트 (기본) | system-inbox |
| IDEA | 아이디어 | system-ideas |
| BOOKMARK | URL 포함 콘텐츠 (장기 로드맵. 현재 비활성) | system-bookmarks (예정) |

**사용자 분류 수정 UI 옵션 ↔ 서버 응답 매핑:**

| UI 표시 | classified_type | note_sub_type |
| --- | --- | --- |
| 일정 | SCHEDULE | null |
| 할 일 | TODO | null |
| 노트 | NOTES | INBOX |
| 아이디어 | NOTES | IDEA |

**split_items 상세 (Phase 2b):**

다중 의도가 감지된 경우 split_items 배열로 분리 결과를 반환한다. 각 항목은 단건 분류 응답과 동일한 구조를 갖는다.

```json
{
  "status": "ok",
  "data": {
    "classified_type": "NOTES",
    "note_sub_type": "INBOX",
    "confidence": "MEDIUM",
    "ai_title": "금요일 미팅 후 카페 알아보기",
    "tags": [],
    "entities": [],
    "schedule_info": null,
    "todo_info": null,
    "split_items": [
      {
        "split_text": "금요일 미팅",
        "classified_type": "SCHEDULE",
        "note_sub_type": null,
        "confidence": "HIGH",
        "ai_title": "금요일 미팅",
        "tags": ["미팅"],
        "entities": [{"type": "DATE", "value": "금요일", "normalized_value": "2026-02-13"}],
        "schedule_info": {
          "start_time": "2026-02-13T09:00:00+09:00",
          "end_time": null,
          "location": null,
          "is_all_day": false
        },
        "todo_info": null
      },
      {
        "split_text": "카페 알아보기",
        "classified_type": "TODO",
        "note_sub_type": null,
        "confidence": "MEDIUM",
        "ai_title": "카페 알아보기",
        "tags": ["알아볼것"],
        "entities": [],
        "schedule_info": null,
        "todo_info": {"deadline": null, "deadline_source": null, "sub_type": "알아볼것"}
      }
    ]
  }
}
```

| split_items[] 필드 | 타입 | 설명 |
| --- | --- | --- |
| split_text | string | 분리된 의도에 해당하는 텍스트 |
| classified_type | string | 분리 항목의 분류 유형 |
| note_sub_type | string? | NOTES인 경우 서브 분류 |
| confidence | string | 신뢰도 |
| ai_title | string | 분리 항목의 AI 제목 |
| tags | string[] | 태그 |
| entities | object[] | 엔티티 |
| schedule_info | object? | SCHEDULE일 때 |
| todo_info | object? | TODO일 때 |

**비기록성 입력 처리:**

AI는 질문형/대화형 입력도 행동 항목으로 환원하여 분류한다.

| 입력 패턴 | classified_type | note_sub_type | ai_title 예시 |
| --- | --- | --- | --- |
| 정보 확인형 ("오늘 날씨 뭐야?") | TODO | null | "날씨 확인" |
| 추천 요청형 ("좋은 카페 추천해줘") | TODO | null | "카페 알아보기" |
| 감정 표현형 ("오늘 진짜 힘들다") | NOTES | INBOX | "오늘 힘들었음" |
| 의도 모호 | NOTES | INBOX | 원문 기반 |

**에러:**

| HTTP | 코드 | 조건 |
| --- | --- | --- |
| 400 | TEXT_EMPTY | text가 비어있거나 공백만 |
| 400 | TEXT_TOO_LONG | text가 5000자 초과 |
| 503 | AI_SERVICE_UNAVAILABLE | AI 모델 응답 불가 |
| 504 | CLASSIFICATION_TIMEOUT | 5초 내 분류 미완료 |

### 2.2 POST /classify/batch — 배치 분류

Temp 재분류용. 여러 캡처를 일괄 분류한다.

**Request:**

```json
{
  "items": [
    {"capture_id": "uuid-1", "text": "내일 미팅 준비"},
    {"capture_id": "uuid-2", "text": "좋은 카페 추천해줘"}
  ],
  "device_id": "uuid-device-123"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| items | object[] | ✓ | 분류 대상 목록 (최대 20건) |
| items[].capture_id | string | ✓ | 클라이언트 측 Capture ID |
| items[].text | string | ✓ | 캡처 원문 |
| device_id | string | ✓ | 디바이스 식별자 |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "results": [
      {
        "capture_id": "uuid-1",
        "classified_type": "TODO",
        "note_sub_type": null,
        "confidence": "MEDIUM",
        "ai_title": "내일 미팅 준비",
        "tags": ["미팅", "준비"],
        "entities": [{"type": "DATE", "value": "내일", "normalized_value": "2026-02-08"}],
        "schedule_info": null,
        "todo_info": {"deadline": "2026-02-08T09:00:00+09:00", "deadline_source": "AI_EXTRACTED", "sub_type": null},
        "split_items": null
      },
      {
        "capture_id": "uuid-2",
        "classified_type": "TODO",
        "note_sub_type": null,
        "confidence": "MEDIUM",
        "ai_title": "카페 알아보기",
        "tags": ["알아볼것"],
        "entities": [],
        "schedule_info": null,
        "todo_info": {"deadline": null, "deadline_source": null, "sub_type": "알아볼것"},
        "split_items": null
      }
    ],
    "failed": []
  }
}
```

| 응답 필드 | 설명 |
| --- | --- |
| results | 분류 성공 목록 (각 항목은 단건 분류 응답과 동일 + capture_id) |
| failed | 분류 실패 목록: [{"capture_id": "...", "error": "..."}] |

---

## 3. Google Calendar API (Phase 2a)

클라이언트가 Google Calendar API를 직접 호출하지 않고, 서버를 프록시로 사용한다. 서버가 OAuth 토큰을 관리하고 API 호출을 대행한다.

### 3.1 POST /calendar/token/exchange — OAuth 코드 교환 + 토큰 저장

Google OAuth에서 받은 authorization code를 서버가 Google Token API로 교환하고, 결과 토큰을 디바이스 기준으로 저장한다.

**Request:**

```json
{
  "device_id": "uuid-device-123",
  "code": "oauth-auth-code",
  "redirect_uri": "com.kairos.app:/oauth2redirect"
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "connected": true,
    "expires_at": "2026-03-07T00:00:00+09:00"
  }
}
```

**에러:**

| HTTP | 코드 | 조건 |
| --- | --- | --- |
| 503 | GOOGLE_TOKEN_EXCHANGE_FAILED | Google OAuth 코드 교환 실패 |

### 3.2 POST /calendar/token — 디바이스 토큰 저장

Google OAuth 완료 후 디바이스별 access_token/refresh_token을 서버에 저장한다.

**Request:**

```json
{
  "device_id": "uuid-device-123",
  "access_token": "google-access-token",
  "refresh_token": "google-refresh-token",
  "expires_at": "2026-03-07T00:00:00+09:00"
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "connected": true
  }
}
```

**검증 규칙:** Header `X-Device-ID`와 body `device_id`가 일치해야 한다.

### 3.3 POST /calendar/events — 이벤트 생성

**Request:**

```json
{
  "capture_id": "uuid-capture-1",
  "title": "강남역 저녁 약속",
  "start_time": "2026-02-13T19:00:00+09:00",
  "end_time": "2026-02-13T21:00:00+09:00",
  "location": "강남역",
  "is_all_day": false
}
```

**Response (201):**

```json
{
  "status": "ok",
  "data": {
    "google_event_id": "google-event-abc123",
    "html_link": "https://calendar.google.com/event?eid=..."
  }
}
```

**에러:**

| HTTP | 코드 | 조건 |
| --- | --- | --- |
| 401 | GOOGLE_AUTH_REQUIRED | Google OAuth 미연결 |
| 401 | GOOGLE_TOKEN_EXPIRED | 토큰 만료 (클라이언트에서 리프레시 요청) |
| 503 | GOOGLE_API_ERROR | Google Calendar API 오류 |

### 3.4 DELETE /calendar/events/{google_event_id} — 이벤트 삭제

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "deleted": true
  }
}
```

### 3.5 GET /calendar/events — 이벤트 조회

**Query Parameters:**

| 파라미터 | 타입 | 설명 |
| --- | --- | --- |
| start_date | string | 조회 시작일 (ISO date) |
| end_date | string | 조회 종료일 (ISO date) |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "events": [
      {
        "google_event_id": "google-event-abc123",
        "title": "강남역 저녁 약속",
        "start_time": "2026-02-13T19:00:00+09:00",
        "end_time": "2026-02-13T21:00:00+09:00",
        "location": "강남역",
        "is_all_day": false,
        "source": "kairos"
      }
    ]
  }
}
```

`source` 필드: "kairos" (Kairos에서 생성) / "google" (Google Calendar에서 직접 생성)

---

## 4. 분석 이벤트 API (Phase 2a)

### 4.1 POST /analytics/events — 배치 이벤트 전송

**Request:**

```json
{
  "device_id": "uuid-device-123",
  "events": [
    {
      "event_type": "capture_created",
      "event_data": {"source": "APP", "input_length": 42},
      "timestamp": "2026-02-06T14:30:00+09:00"
    },
    {
      "event_type": "classification_completed",
      "event_data": {"classified_type": "TODO", "note_sub_type": null, "confidence": "HIGH", "processing_time_ms": 1200},
      "timestamp": "2026-02-06T14:30:01+09:00"
    },
    {
      "event_type": "classification_confirmed",
      "event_data": {"capture_id": "uuid-1", "confirmed_type": "TODO", "changed": false},
      "timestamp": "2026-02-06T14:30:10+09:00"
    },
    {
      "event_type": "classification_modified",
      "event_data": {"original_type": "TODO", "original_sub_type": null, "new_type": "SCHEDULE", "new_sub_type": null, "time_since_classification_ms": 45000},
      "timestamp": "2026-02-06T14:30:46+09:00"
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| device_id | string | ✓ | 익명 디바이스 ID |
| events | object[] | ✓ | 이벤트 목록 (최대 100건) |
| events[].event_type | string | ✓ | 이벤트 유형 |
| events[].event_data | object | ✓ | 이벤트 데이터 (JSON) |
| events[].timestamp | string | ✓ | 이벤트 발생 시각 (ISO 8601) |

**이벤트 유형:**

| event_type | event_data 포함 필드 | Phase |
| --- | --- | --- |
| capture_created | source, input_length | 2a |
| classification_completed | classified_type, note_sub_type, confidence, processing_time_ms | 2a |
| classification_confirmed | capture_id, confirmed_type, changed(boolean) | 2a |
| classification_modified | original_type, original_sub_type, new_type, new_sub_type, time_since_classification_ms | 2a |
| schedule_suggestion_response | action(approved/rejected/ignored), confidence, source(app/widget/share) | 2a |
| schedule_auto_deleted | time_since_auto_add_ms | 2a |
| temp_item_resolved | resolution(ai/user), time_in_temp_ms | 2a |
| trash_restored | time_in_trash_ms | 2a |
| split_capture_created | parent_capture_id, split_count | 2a |
| capture_revisited | time_since_creation_ms, access_method(list/search) | 2b |
| todo_completed | time_since_creation_ms | 2b |
| search_performed | result_count, result_clicked | 2b |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "received": 4
  }
}
```

**프라이버시:** event_data에 원문 텍스트 포함 금지. 서버에서 원문이 포함된 이벤트는 필터링.

---

## 5. 인증 API (Phase 3a)

### 5.1 POST /auth/google — Google Sign-In(ID Token) 로그인

**Request:**

```json
{
  "id_token": "google-id-token-...",
  "device_id": "uuid-device-123"
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "access_token": "jwt-token-...",
    "refresh_token": "refresh-token-...",
    "expires_in": 3600,
    "user": {
      "id": "uuid-user-1",
      "email": "user@gmail.com",
      "subscription_tier": "FREE"
    }
  }
}
```

### 5.2 POST /auth/refresh — 토큰 갱신

**Request:**

```json
{
  "refresh_token": "refresh-token-..."
}
```

**Response (200):** 새 access_token + expires_in 반환. `user` 필드는 선택적(nullable)이며, 포함되지 않을 수 있다. 클라이언트는 `user` 없을 때 기존 캐시된 사용자 정보를 유지해야 한다.

### 5.3 GET /auth/me — 현재 사용자 정보

**Headers:** `Authorization: Bearer {access_token}`

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "id": "uuid-user-1",
    "email": "user@gmail.com",
    "subscription_tier": "FREE",
    "google_calendar_connected": true
  }
}
```

---

## 6. 구독 API (Phase 3a)

### 6.1 GET /subscription — 구독 상태 조회

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "tier": "FREE",
    "features": {
      "inbox_auto_classify": false,
      "auto_grouping": false,
      "reorganize": false,
      "classification_preset": false,
      "custom_instruction": false,
      "semantic_search": false,
      "modification_learning": false,
      "analytics_dashboard": false,
      "ocr": false
    }
  }
}
```

### 6.2 POST /subscription/verify — 구독 검증

Google Play Billing 영수증 검증.

**Request:**

```json
{
  "purchase_token": "google-play-purchase-token-...",
  "product_id": "kairos_premium_monthly"
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "tier": "PREMIUM",
    "expires_at": "2026-03-07T00:00:00Z",
    "features": {
      "inbox_auto_classify": true,
      "auto_grouping": true,
      "reorganize": true,
      "classification_preset": true,
      "custom_instruction": true,
      "semantic_search": true,
      "modification_learning": true,
      "analytics_dashboard": true,
      "ocr": true
    }
  }
}
```

**구독 기능 ↔ Phase 매핑:**

| 기능 키 | 설명 | Phase |
| --- | --- | --- |
| inbox_auto_classify | Inbox 노트 AI 자동 분류 | 3a |
| auto_grouping | AI 통합 그룹화 | 3a |
| reorganize | 전체 노트 재정리 | 3a |
| classification_preset | 분류 프리셋 | 3a |
| custom_instruction | 사용자 분류 지시 | 3a |
| semantic_search | AI 시맨틱 검색 | 3a |
| modification_learning | 수정 이력 기반 학습 (고도화) | 3b |
| analytics_dashboard | 분석 대시보드 | 3b |
| ocr | 이미지 OCR 텍스트 추출 | 3b |

---

## 7. 노트 API (Phase 2b~3a)

### 7.1 POST /notes/group — AI 통합 그룹화 (Phase 3a, 구독)

Inbox 및 Ideas 폴더의 노트를 토픽 기반으로 그룹화한다.

**Request:**

```json
{
  "notes": [
    {"capture_id": "uuid-1", "ai_title": "React 상태 관리", "tags": ["개발", "프론트엔드"], "note_sub_type": "INBOX"},
    {"capture_id": "uuid-2", "ai_title": "Vue vs React 비교", "tags": ["개발"], "note_sub_type": "INBOX"},
    {"capture_id": "uuid-3", "ai_title": "독서 메모: 원씽", "tags": ["독서"], "note_sub_type": "IDEA"}
  ],
  "existing_folders": [
    {"id": "system-inbox", "name": "Inbox", "type": "INBOX"},
    {"id": "system-ideas", "name": "Ideas", "type": "IDEAS"},
    {"id": "system-bookmarks", "name": "Bookmarks", "type": "BOOKMARKS"},
    {"id": "folder-user-1", "name": "개발 관련", "type": "USER"}
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| notes | object[] | ✓ | 그룹화 대상 노트 목록 |
| notes[].capture_id | string | ✓ | Capture ID |
| notes[].ai_title | string | ✓ | AI 제목 |
| notes[].tags | string[] | ✓ | 태그 목록 |
| notes[].note_sub_type | string | ✓ | INBOX / IDEA |
| existing_folders | object[] | ✓ | 현재 폴더 구조 (시스템 + 사용자 + AI 그룹) |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "groups": [
      {
        "folder_name": "프론트엔드 개발",
        "folder_type": "AI_GROUP",
        "capture_ids": ["uuid-1", "uuid-2"]
      },
      {
        "folder_name": "독서 메모",
        "folder_type": "AI_GROUP",
        "capture_ids": ["uuid-3"]
      }
    ]
  }
}
```

### 7.2 POST /notes/reorganize — 전체 재정리 (Phase 3a, 구독)

전체 노트를 재분석하여 새 폴더 구조를 제안한다. 사용자가 Before/After 비교 후 적용 여부를 결정.

**Request:**

```json
{
  "notes": [
    {"capture_id": "uuid-1", "ai_title": "React 상태 관리", "tags": ["개발"], "note_sub_type": "INBOX", "folder_id": "system-inbox"},
    {"capture_id": "uuid-2", "ai_title": "Vue vs React 비교", "tags": ["개발"], "note_sub_type": "USER_FOLDER", "folder_id": "folder-user-1"}
  ],
  "existing_folders": [
    {"id": "system-inbox", "name": "Inbox", "type": "INBOX"},
    {"id": "system-ideas", "name": "Ideas", "type": "IDEAS"},
    {"id": "system-bookmarks", "name": "Bookmarks", "type": "BOOKMARKS"},
    {"id": "folder-user-1", "name": "개발 관련", "type": "USER"}
  ]
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "proposed_structure": [
      {
        "folder_name": "Inbox",
        "folder_type": "INBOX",
        "capture_ids": ["uuid-5", "uuid-8"]
      },
      {
        "folder_name": "Ideas",
        "folder_type": "IDEAS",
        "capture_ids": ["uuid-3", "uuid-7"]
      },
      {
        "folder_name": "프론트엔드 개발",
        "folder_type": "AI_GROUP",
        "action": "NEW",
        "capture_ids": ["uuid-1", "uuid-2"]
      },
      {
        "folder_name": "독서 메모",
        "folder_type": "AI_GROUP",
        "action": "NEW",
        "capture_ids": ["uuid-4", "uuid-6"]
      }
    ]
  }
}
```

| 응답 필드 | 설명 |
| --- | --- |
| proposed_structure | 제안된 전체 폴더 구조 |
| folder_type | INBOX / IDEAS / BOOKMARKS / AI_GROUP / USER |
| action | NEW (신규 생성) / EXISTING (기존 폴더 유지) / MERGE (병합 제안). 기존 시스템 폴더는 action 없음 |

### 7.3 POST /notes/inbox-classify — Inbox AI 자동 분류 (Phase 3a, 구독)

Inbox에 쌓인 미분류 노트를 AI가 적절한 폴더로 자동 배치한다.

**Request:**

```json
{
  "notes": [
    {"capture_id": "uuid-1", "ai_title": "React 상태 관리", "tags": ["개발", "프론트엔드"]},
    {"capture_id": "uuid-2", "ai_title": "좋은 카페 목록", "tags": ["카페"]}
  ],
  "existing_folders": [
    {"id": "system-inbox", "name": "Inbox", "type": "INBOX"},
    {"id": "system-ideas", "name": "Ideas", "type": "IDEAS"},
    {"id": "system-bookmarks", "name": "Bookmarks", "type": "BOOKMARKS"},
    {"id": "folder-user-1", "name": "개발 관련", "type": "USER"},
    {"id": "folder-ai-1", "name": "프론트엔드", "type": "AI_GROUP"}
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| notes | object[] | ✓ | Inbox 내 미분류 노트 목록 |
| existing_folders | object[] | ✓ | 현재 전체 폴더 구조 |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "assignments": [
      {
        "capture_id": "uuid-1",
        "target_folder_id": "folder-ai-1",
        "target_folder_name": "프론트엔드",
        "new_note_sub_type": "USER_FOLDER"
      },
      {
        "capture_id": "uuid-2",
        "target_folder_id": "system-ideas",
        "target_folder_name": "Ideas",
        "new_note_sub_type": "IDEA"
      }
    ],
    "new_folders": []
  }
}
```

| 응답 필드 | 설명 |
| --- | --- |
| assignments | 각 노트의 폴더 배치 결과 |
| assignments[].target_folder_id | 배치 대상 폴더 ID (기존 폴더 또는 신규 폴더) |
| assignments[].new_note_sub_type | 변경될 note_sub_type |
| new_folders | 신규 생성이 필요한 폴더 목록: [{"name": "...", "type": "AI_GROUP"}] |

### 7.4 POST /notes/search-semantic — 시맨틱 검색 (Phase 3a, 구독)

**Request:**

```json
{
  "query": "프론트엔드 상태 관리",
  "limit": 20
}
```

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "results": [
      {"capture_id": "uuid-1", "score": 0.95, "snippet": "React 상태 관리 패턴..."},
      {"capture_id": "uuid-2", "score": 0.82, "snippet": "Vue vs React 비교..."}
    ]
  }
}
```

### 7.5 GET /analytics/dashboard — 분석 대시보드 (Phase 3b, 구독)

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "total_captures": 142,
    "captures_by_type": {"TODO": 45, "SCHEDULE": 30, "NOTES": 60, "TEMP": 7},
    "captures_by_day": {"2026-02-08": 5, "2026-02-09": 8, "2026-02-10": 3},
    "avg_classification_time_ms": 1200,
    "top_tags": [{"tag": "개발", "count": 25}, {"tag": "회의", "count": 18}]
  }
}
```

---

## 8. OCR API (Phase 3b, 구독)

### 8.1 POST /ocr/extract — 이미지 텍스트 추출

이미지를 Base64로 인코딩하여 전송, 서버에서 OCR 처리 후 텍스트를 반환한다.

**Request:**

```json
{
  "image_data": "base64-encoded-image-data...",
  "image_type": "jpeg",
  "language_hint": "ko",
  "extract_structure": false
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| image_data | string | ✓ | Base64 인코딩된 이미지 데이터 |
| image_type | string | | 이미지 형식 (jpeg, png, webp). 기본값: jpeg |
| language_hint | string | | 언어 힌트 (ko, en 등) |
| extract_structure | boolean | | 구조화된 콘텐츠 추출 여부. 기본값: false |

**Response (200):**

```json
{
  "status": "ok",
  "data": {
    "success": true,
    "text": "추출된 텍스트 내용...",
    "confidence": 0.95,
    "language": "ko",
    "word_count": 42,
    "has_structure": false,
    "structured_content": null
  }
}
```

---

## 9. 서버 구현 구조 (FastAPI)

### 9.1 프로젝트 구조

```
kairos-server/
├── app/
│   ├── main.py                 # FastAPI app 진입점
│   ├── config.py               # 환경 설정
│   ├── dependencies.py         # 의존성 주입
│   ├── routers/
│   │   ├── classify.py         # /classify, /classify/batch
│   │   ├── calendar.py         # /calendar/*
│   │   ├── analytics.py        # /analytics/*
│   │   ├── auth.py             # /auth/* (Phase 3a)
│   │   ├── subscription.py     # /subscription/* (Phase 3a)
│   │   └── notes.py            # /notes/* (Phase 2b~3a)
│   ├── services/
│   │   ├── ai_classifier.py    # AI 분류 로직
│   │   ├── google_calendar.py  # Google Calendar 연동
│   │   ├── note_grouping.py    # 노트 그룹화·분류 로직
│   │   └── analytics.py        # 분석 이벤트 처리
│   ├── models/
│   │   ├── requests.py         # Pydantic 요청 모델
│   │   └── responses.py        # Pydantic 응답 모델
│   └── db/
│       ├── database.py         # DB 연결
│       └── models.py           # SQLAlchemy 모델
├── tests/
├── requirements.txt
└── Dockerfile
```

### 9.2 AI 분류 서비스 흐름

```
classify 요청 수신
  → 텍스트 전처리 (공백 정규화, 길이 검증)
  → AI 모델 호출 (Gemini API)
    → 프롬프트 구성: 시스템 프롬프트 + 사용자 텍스트 + 컨텍스트
    → JSON 응답 파싱
  → 응답 검증:
    - classified_type이 유효한 값인지 (SCHEDULE/TODO/NOTES/TEMP)
    - classified_type=NOTES이면 note_sub_type 필수 (INBOX/IDEA/BOOKMARK)
    - classified_type≠NOTES이면 note_sub_type=null
    - 엔티티·태그 유효성
  → 다중 의도 감지 시 split_items 생성 (Phase 2b)
  → 클라이언트 응답 반환
```

### 9.3 Rate Limiting

| 엔드포인트 | 제한 | 기준 |
| --- | --- | --- |
| POST /classify | 60회/분 | device_id |
| POST /classify/batch | 10회/분 | device_id |
| POST /analytics/events | 30회/분 | device_id |
| POST /calendar/* | 30회/분 | device_id (Android 미사용, 레거시) |
| POST /notes/* | 10회/분 | device_id |
| POST /sync/* | 30회/분 | device_id |

---

## 10. Phase별 엔드포인트 요약

| 엔드포인트 | 메서드 | Phase | 설명 |
| --- | --- | --- | --- |
| /classify | POST | 1 | 단건 AI 분류 |
| /classify/batch | POST | 1 | 배치 AI 분류 (Temp 재분류) |
| /calendar/token/exchange | POST | 2a | OAuth 코드 교환 + 디바이스 토큰 저장 (Android 미사용, 레거시) |
| /calendar/token | POST | 2a | 디바이스별 Google OAuth 토큰 저장 (Android 미사용, 레거시) |
| /calendar/events | POST | 2a | 캘린더 이벤트 생성 (Android 미사용, 레거시) |
| /calendar/events/{id} | DELETE | 2a | 캘린더 이벤트 삭제 (Android 미사용, 레거시) |
| /calendar/events | GET | 2a | 캘린더 이벤트 조회 (Android 미사용, 레거시) |
| /analytics/events | POST | 2a | 분석 이벤트 배치 전송 |
| /sync/push | POST | 3a | 로컬 변경분 서버 업로드 |
| /sync/pull | POST | 3a | 서버 변경분 조회 |
| /notes/group | POST | 3a | AI 통합 그룹화 (구독) |
| /notes/reorganize | POST | 3a | 전체 재정리 (구독) |
| /notes/inbox-classify | POST | 3a | Inbox AI 자동 분류 (구독) |
| /notes/search-semantic | POST | 3a | 시맨틱 검색 (구독) |
| /analytics/dashboard | GET | 3b | 분석 대시보드 (구독) |
| /ocr/extract | POST | 3b | 이미지 OCR 텍스트 추출 (구독) |
| /auth/google | POST | 3a | Google Sign-In(ID Token) 로그인 |
| /auth/refresh | POST | 3a | 토큰 갱신 |
| /auth/me | GET | 3a | 현재 사용자 정보 |
| /subscription | GET | 3a | 구독 상태 조회 |
| /subscription/verify | POST | 3a | 구독 검증 |

---

*Document Version: 2.6 | Last Updated: 2026-02-12*
