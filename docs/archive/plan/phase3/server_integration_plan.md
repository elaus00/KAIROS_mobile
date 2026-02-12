# Phase 3: 서버 연동 계획 (Mock → 실제 API)

> **작성일**: 2026-02-07 | **갱신일**: 2026-02-09
> **상태**: 구현 진행 중 (3-0 완료, 3-1 완료, 3-6 완료)
> **선행 조건**: Phase 2b 완료 ✅, 서버 배포 완료 ✅
> **서버 스택**: FastAPI (Python 3.11+), Gemini API

---

## 1. 개요

MockKairosApi는 이미 Phase 2b QA 과정에서 제거됨(3-6 선행 완료). Phase 3에서는 에러 핸들링 레이어를 정비하고, 실제 서버와 연동하여 AI 분류, Calendar 동기화, Analytics 이벤트 전송을 실서버로 전환한다.

### 작업 범위

| 단계 | 설명 | 상태 |
|------|------|------|
| 3-0 | 서버 배포 + Health Check 확인 | ✅ 완료 |
| 3-1 | 네트워크 레이어 정비 (에러 핸들링, 인터셉터) | ✅ 완료 |
| 3-2 | `/classify` 실서버 연동 + 테스트 | ✅ 클라이언트 준비 완료 |
| 3-3 | `/classify` split_items 실서버 연동 | ✅ 클라이언트 준비 완료 (3-2에 포함) |
| 3-4 | `/calendar/events` 실서버 연동 (Google OAuth) | ✅ 클라이언트 준비 완료 |
| 3-5 | `/analytics/events` 실서버 연동 | ✅ 클라이언트 준비 완료 |
| 3-6 | MockKairosApi 제거 + 빌드 설정 정리 | ✅ 완료 (선행) |
| 3-7 | 통합 테스트 + 에뮬레이터 검증 | ⬜ 실서버 E2E 대기 |

---

## 2. 현재 네트워크 구조

### 2.1 API 연결 (`NetworkModule.kt`)

```kotlin
// Retrofit만 사용 (Mock 제거됨)
OkHttpClient.Builder()
    .addInterceptor(errorInterceptor)      // 네트워크 에러 → ApiException.NetworkError
    .addInterceptor(deviceIdInterceptor)   // X-Device-ID 헤더 자동 추가
    .addInterceptor(loggingInterceptor)    // 디버그 로깅
    .build()
```

### 2.2 빌드 설정 (`build.gradle.kts`)

| 빌드 타입 | API_BASE_URL |
|-----------|-------------|
| debug | ngrok 백엔드 URL (변경 가능) |
| release | `http://your-production-server.com` |

> `USE_MOCK_API` 플래그는 이미 삭제됨

### 2.3 인증 방식

| Phase | 인증 |
|-------|------|
| 현재 (1~3) | `X-Device-ID` 헤더 (`DeviceIdInterceptor`) |
| 향후 (3a+) | JWT Bearer Token |

### 2.4 에러 처리 레이어 (3-1 구현 완료)

| 파일 | 역할 |
|------|------|
| `ErrorInterceptor` | IOException → `ApiException.NetworkError` 변환 |
| `ApiResponseHandler` | `Response<ApiEnvelope<T>>` → `T` 추출, 실패 시 `ApiException` throw |
| `ApiException` | sealed class 도메인 예외 계층 (6종) |

---

## 3. 서브페이즈 상세

### 3-0: 서버 배포 + Health Check ✅ 완료

**목표**: FastAPI 서버 배포 및 클라이언트에서 접근 가능 확인

| 작업 | 내용 | 상태 |
|------|------|------|
| 서버 배포 | FastAPI 서버를 ngrok 스테이징 환경에 배포 | ✅ |
| Base URL 설정 | `build.gradle.kts`에서 debug Base URL을 ngrok URL로 업데이트 | ✅ |
| Health Check | 서버 연결 확인 | ✅ |
| 네트워크 권한 | `AndroidManifest.xml`에 `INTERNET` 퍼미션 확인 | ✅ |

---

### 3-1: 네트워크 레이어 정비 ✅ 완료

**목표**: 실서버 연동에 필요한 에러 핸들링/재시도/인터셉터 추가

| 파일 | 변경 | 내용 | 상태 |
|------|------|------|------|
| `di/NetworkModule.kt` | 수정 | ErrorInterceptor 등록 | ✅ |
| `data/remote/interceptor/DeviceIdInterceptor.kt` | 기존 | `X-Device-ID` 헤더 자동 추가 (이미 존재) | ✅ |
| `data/remote/interceptor/ErrorInterceptor.kt` | 신규 | IOException → `ApiException.NetworkError` 변환 | ✅ |
| `data/remote/ApiResponseHandler.kt` | 신규 | `Response<ApiEnvelope<T>>` → `T` 추출 + 에러 매핑 | ✅ |
| `domain/model/ApiException.kt` | 신규 | sealed class 도메인 예외 계층 (6종 + `isRetryable`) | ✅ |
| `data/worker/ClassifyCaptureWorker.kt` | 수정 | ApiResponseHandler + ApiException 기반 에러 처리 | ✅ |
| `data/worker/AnalyticsBatchWorker.kt` | 수정 | ApiResponseHandler 기반 응답 검증 | ✅ |
| `data/repository/CalendarRepositoryImpl.kt` | 수정 | ApiResponseHandler.safeCall + CalendarApiException 매핑 | ✅ |

> 계획 대비 변경: `ApiResult<T>` 래퍼 대신 `ApiResponseHandler`를 직접 사용하여 단순화.
> `ApiException.isRetryable` 속성으로 재시도 판단을 통합.

**에러 변환 매핑 (구현됨)**:

| HTTP | 서버 코드 | 도메인 예외 |
|------|----------|-----------|
| 400 | TEXT_EMPTY / TEXT_TOO_LONG / INVALID_REQUEST | `ApiException.InvalidRequest` |
| 429 | RATE_LIMITED | `ApiException.RateLimited` |
| 503 | AI_SERVICE_UNAVAILABLE | `ApiException.ServiceUnavailable` |
| 504 | CLASSIFICATION_TIMEOUT | `ApiException.ClassificationTimeout` |
| 500+ | 기타 | `ApiException.ServerError` |
| 네트워크 불가 | — | `ApiException.NetworkError` |

**재시도 정책**:
- `isRetryable = true`: ServiceUnavailable, ClassificationTimeout, ServerError, NetworkError → SyncQueue 재시도
- `isRetryable = false`: InvalidRequest, RateLimited → 즉시 FAILED 처리

---

### 3-2: `/classify` 실서버 연동 ✅ 클라이언트 준비 완료

**목표**: AI 분류를 실제 Gemini API 기반 서버로 전환

**스펙 호환성 검증 완료 (2026-02-10)**:

| 검증 항목 | 상태 | 비고 |
|----------|------|------|
| `ClassifyRequest` (text, source, device_id, user_context) | ✅ | @SerializedName 일치 |
| `ClassifyResponse` (14개 필드) | ✅ | 모든 필드 정확히 매핑 |
| `ClassifyBatchRequest/Response` | ✅ | items, results, failed 구조 일치 |
| `ClassificationMapper` DTO→Domain | ✅ | 26개 테스트로 검증 완료 |
| `ApiResponseHandler` 에러 매핑 | ✅ | 23개 테스트로 검증 완료 |
| `ProcessClassificationResultUseCase` | ✅ | 단일 + split 의도 처리 기존 12개 테스트 통과 |
| enum 폴백 (미지 값 안전 처리) | ✅ | TEMP/INBOX/MEDIUM/OTHER로 폴백 |

**남은 작업**: 실서버 E2E 검증 (서버 준비 후)

---

### 3-3: `/classify` split_items 실서버 연동 ✅ 클라이언트 준비 완료

**목표**: 서버의 다중 의도 분리 응답을 클라이언트에서 올바르게 처리

> `SplitItemDto` 9개 필드 스펙 일치 확인, `ClassificationMapper`에서 split_items 변환 테스트 통과,
> `ProcessClassificationResultUseCase` split 처리 기존 테스트 통과.

**E2E 테스트 시나리오** (서버 준비 후):
| 입력 | 기대 결과 |
|------|----------|
| "보고서 쓰고 저녁 예약" | splitItems 2개 (TODO + SCHEDULE) |
| "내일 미팅" | splitItems null (단일 의도) |
| "카페 가고 책 사고 보고서 제출" | splitItems 3개 |

---

### 3-4: `/calendar/events` 실서버 연동

**목표**: Google Calendar API 프록시를 통한 실제 캘린더 이벤트 생성/삭제

> `CalendarRepositoryImpl`은 3-1에서 `ApiResponseHandler.safeCall` 기반으로 리팩토링 완료.
> `CalendarApiException` 매핑(GOOGLE_AUTH_REQUIRED 등)도 `toCalendarException()`으로 통합 완료.

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/KairosApi.kt` | 확인 | calendar 엔드포인트 시그니처 검증 |
| `data/remote/dto/v2/CalendarEventRequest.kt` | 확인 | `captureId` 필드 이미 존재 |
| `data/repository/CalendarRepositoryImpl.kt` | 완료 | ApiResponseHandler + CalendarApiException 매핑 (3-1에서 완료) |

**Google OAuth 연동 사전 조건**:
- 서버에서 Google OAuth 토큰 관리
- 클라이언트는 `X-Device-ID`로 디바이스 식별
- `GOOGLE_AUTH_REQUIRED` 에러 시 Google 로그인 UI로 안내

**에러 처리**:
| 에러 코드 | 클라이언트 동작 |
|----------|--------------|
| `GOOGLE_AUTH_REQUIRED` | 설정 → Google Calendar 연결 화면으로 안내 |
| `GOOGLE_TOKEN_EXPIRED` | 토큰 갱신 요청 후 재시도 |
| `GOOGLE_API_ERROR` | 사용자에게 에러 표시 + SyncQueue에 재시도 등록 |

**검증**: 일정 생성 → 실제 Google Calendar에 이벤트 표시, 삭제 → Calendar에서 제거

---

### 3-5: `/analytics/events` 실서버 연동

**목표**: 분석 이벤트 배치 전송을 실서버로 전환

> `AnalyticsBatchWorker`는 3-1에서 `ApiResponseHandler.unwrap()` 기반으로 리팩토링 완료.

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/KairosApi.kt` | 확인 | analytics 엔드포인트 시그니처 검증 |
| `data/remote/dto/v2/AnalyticsEventDto.kt` | 확인 | timestamp 포맷 ISO 8601 확인 |
| `data/worker/AnalyticsBatchWorker.kt` | 완료 | ApiResponseHandler 기반 에러 핸들링 (3-1에서 완료) |

**프라이버시 검증**:
- event_data에 원문 텍스트가 포함되지 않는지 확인
- device_id만 전송, 개인정보 미포함

**검증**: 이벤트 발생 → 1시간 내 서버 수신 확인, 네트워크 끊김 → 복구 후 재전송

---

### 3-6: MockKairosApi 제거 + 빌드 설정 정리 ✅ 완료 (선행)

> Phase 2b QA 과정에서 이미 완료됨 (2026-02-08)

| 작업 | 상태 |
|------|------|
| `MockKairosApi.kt` 삭제 | ✅ |
| `USE_MOCK_API` BuildConfig 필드 삭제 | ✅ |
| `NetworkModule.kt` — Retrofit 직접 사용 | ✅ |
| `MockDataInitializer.kt` 유지 (샘플 데이터) | ✅ |

**검증**: `compileDebugKotlin` 통과, Mock 참조 코드 없음 ✅

---

### 3-7: 통합 테스트 + 에뮬레이터 검증

**목표**: 전체 API 연동 E2E 검증

| 테스트 시나리오 | 검증 항목 |
|---------------|----------|
| 캡처 생성 → AI 분류 | 서버 분류 결과 → 파생 엔티티 생성 |
| 멀티 인텐트 캡처 | 서버 split 응답 → 자식 캡처 + 파생 엔티티 |
| 일정 캘린더 동기화 | 서버 프록시 → Google Calendar 이벤트 생성/삭제 |
| 분석 이벤트 배치 전송 | AnalyticsBatchWorker → 서버 수신 확인 |
| 네트워크 끊김 복구 | 오프라인 캡처 → 온라인 복구 → 분류 완료 |
| Temp 재분류 | `/classify/batch` 배치 분류 동작 |
| Rate Limit | 60회/분 초과 시 429 에러 핸들링 |

**기존 유닛 테스트 호환성**:
- Mock 이미 제거됨. 기존 테스트에서 MockKairosApi 참조 없음 (MockK mockk() 사용)
- 137개 유닛 테스트 전체 통과 확인 (2026-02-09)

**검증**: `./gradlew testDebugUnitTest` 전체 통과 + 에뮬레이터 E2E 시나리오 통과

---

## 4. 서브페이즈 의존성

```
3-0 (서버 배포)     ✅
 └→ 3-1 (네트워크 정비) ✅
     ├→ 3-2 (/classify 연동)   ✅ 클라이언트 준비 완료
     │   └→ 3-3 (split_items)  ✅ 클라이언트 준비 완료
     ├→ 3-4 (/calendar 연동)   ✅ 클라이언트 준비 완료
     └→ 3-5 (/analytics 연동)  ✅ 클라이언트 준비 완료

3-6 (Mock 제거) ✅ (선행 완료)

→ 3-7 (통합 테스트) ⬜ 실서버 E2E 대기
```

---

## 5. 파일 변경 요약

### 완료된 파일 (3-0, 3-1, 3-6)

| 파일 | 변경 |
|------|------|
| `domain/model/ApiException.kt` | 신규 |
| `data/remote/ApiResponseHandler.kt` | 신규 |
| `data/remote/interceptor/ErrorInterceptor.kt` | 신규 |
| `di/NetworkModule.kt` | 수정 (ErrorInterceptor 등록) |
| `data/worker/ClassifyCaptureWorker.kt` | 수정 (ApiResponseHandler 적용) |
| `data/worker/AnalyticsBatchWorker.kt` | 수정 (ApiResponseHandler 적용) |
| `data/repository/CalendarRepositoryImpl.kt` | 수정 (ApiResponseHandler.safeCall 적용) |

### 남은 파일 (3-2 ~ 3-5, 3-7)

| 구분 | 예상 변경 |
|------|----------|
| DTO 필드 검증 | 서버 스펙과 비교 확인 |
| E2E 테스트 | 에뮬레이터에서 실서버 연동 검증 |

---

## 6. 주의사항

- `X-Device-ID` 헤더는 Phase 3a(인증)까지 유지, JWT 전환은 별도 단계
- Rate Limit 테스트 시 서버 부하 주의
- Google Calendar 연동은 실제 Google 계정 필요
- 프라이버시: Analytics event_data에 원문 텍스트 절대 포함 금지

---

*Document Version: 2.0 | Last Updated: 2026-02-09*
