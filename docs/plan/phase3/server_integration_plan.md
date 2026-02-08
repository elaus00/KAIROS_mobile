# Phase 3: 서버 연동 계획 (Mock → 실제 API)

> **작성일**: 2026-02-07
> **상태**: 계획 수립 (Phase 2b 완료 후 착수)
> **선행 조건**: Phase 2b 완료, 서버 배포 완료
> **서버 스택**: FastAPI (Python 3.11+), Gemini API

---

## 1. 개요

Phase 2b까지 모든 외부 API 호출은 `MockKairosApi`를 통해 로컬에서 처리된다. Phase 3에서는 Mock을 제거하고 실제 서버와 연동하여 AI 분류, Calendar 동기화, Analytics 이벤트 전송을 실서버로 전환한다.

### 작업 범위

| 단계 | 설명 |
|------|------|
| 3-0 | 서버 배포 + Health Check 확인 |
| 3-1 | 네트워크 레이어 정비 (에러 핸들링, 인터셉터) |
| 3-2 | `/classify` 실서버 연동 + 테스트 |
| 3-3 | `/classify` split_items 실서버 연동 |
| 3-4 | `/calendar/events` 실서버 연동 (Google OAuth) |
| 3-5 | `/analytics/events` 실서버 연동 |
| 3-6 | MockKairosApi 제거 + 빌드 설정 정리 |
| 3-7 | 통합 테스트 + 에뮬레이터 검증 |

---

## 2. 현재 Mock/Real 전환 구조

### 2.1 전환 메커니즘 (`NetworkModule.kt`)

```kotlin
// BuildConfig 플래그로 Mock/Real 선택
if (BuildConfig.USE_MOCK_API) {
    MockKairosApi()
} else {
    retrofit.create(KairosApi::class.java)
}
```

### 2.2 빌드 설정 (`build.gradle.kts`)

| 빌드 타입 | API_BASE_URL | USE_MOCK_API |
|-----------|-------------|-------------|
| debug | `http://10.0.2.2:8000` | `true` |
| release | `http://your-production-server.com` | `false` |
| benchmark | `http://10.0.2.2:8000` | `true` |

### 2.3 인증 방식

| Phase | 인증 |
|-------|------|
| 1 ~ 2b | `X-Device-ID` 헤더 (디바이스 ID) |
| 3a+ | JWT Bearer Token |

### 2.4 Mock이 처리하는 엔드포인트 (5개)

| 엔드포인트 | 메서드 | Mock 동작 |
|-----------|--------|----------|
| `/classify` | POST | 키워드 기반 분류 (500ms 딜레이) |
| `/health` | GET | 하드코딩 응답 (100ms) |
| `/analytics/events` | POST | 무조건 성공 (200ms) |
| `/calendar/events` | POST | Mock event ID 생성 (300ms) |
| `/calendar/events/{id}` | DELETE | 무조건 성공 (200ms) |

---

## 3. 서브페이즈 상세

### 3-0: 서버 배포 + Health Check

**목표**: FastAPI 서버 배포 및 클라이언트에서 접근 가능 확인

| 작업 | 내용 |
|------|------|
| 서버 배포 | FastAPI 서버를 스테이징 환경에 배포 |
| Base URL 설정 | `build.gradle.kts`에서 debug/release Base URL 업데이트 |
| Health Check | `/health` 엔드포인트 호출 성공 확인 |
| 네트워크 권한 | `AndroidManifest.xml`에 `INTERNET` 퍼미션 확인 (이미 있음) |

**검증**: 에뮬레이터에서 `HealthResponse` 수신 성공

---

### 3-1: 네트워크 레이어 정비

**목표**: 실서버 연동에 필요한 에러 핸들링/재시도/인터셉터 추가

| 파일 | 변경 | 내용 |
|------|------|------|
| `di/NetworkModule.kt` | 수정 | Device-ID 인터셉터 추가, 타임아웃 조정 |
| `data/remote/interceptor/DeviceIdInterceptor.kt` | 신규 | `X-Device-ID` 헤더 자동 추가 |
| `data/remote/interceptor/ErrorInterceptor.kt` | 신규 | API 에러 응답 파싱 → 도메인 예외 변환 |
| `domain/model/ApiError.kt` | 신규 | API 에러 도메인 모델 (code, message) |
| `domain/model/ApiResult.kt` | 신규 | `sealed class ApiResult<T>` (Success/Error/NetworkError) |

**에러 변환 매핑**:

| HTTP | 서버 코드 | 도메인 예외 |
|------|----------|-----------|
| 400 | TEXT_EMPTY / TEXT_TOO_LONG / INVALID_REQUEST | `InvalidRequestException` |
| 429 | RATE_LIMITED | `RateLimitedException` |
| 503 | AI_SERVICE_UNAVAILABLE | `ServiceUnavailableException` |
| 504 | CLASSIFICATION_TIMEOUT | `ClassificationTimeoutException` |
| 기타 | INTERNAL_ERROR | `ServerException` |
| 네트워크 불가 | — | `NetworkException` |

**재시도 정책**:
- 503/504: 최대 2회 재시도 (2초 → 5초 딜레이)
- 429: 재시도 안 함 (Rate Limit 존중)
- 네트워크 에러: SyncQueue를 통한 지연 재시도 (기존 메커니즘 활용)

**검증**: 네트워크 끊김 시 적절한 에러 표시, 503 시 재시도 동작

---

### 3-2: `/classify` 실서버 연동

**목표**: AI 분류를 실제 Gemini API 기반 서버로 전환

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/KairosApi.kt` | 확인 | `classify()` 시그니처가 서버 스펙과 일치하는지 검증 |
| `data/remote/dto/v2/ClassifyRequest.kt` | 수정 | `source`, `device_id`, `user_context` 필드 추가 (서버 스펙 맞춤) |
| `data/worker/ClassifyCaptureWorker.kt` | 수정 | ClassifyRequest에 source/device_id 포함 |
| `data/repository/CaptureRepositoryImpl.kt` | 수정 | 에러 핸들링 적용 (ApiResult 기반) |

**ClassifyRequest 변경**:
```kotlin
// 기존
data class ClassifyRequest(val text: String)

// 변경
data class ClassifyRequest(
    val text: String,
    val source: String,       // "APP" / "SHARE_INTENT" / "WIDGET"
    val device_id: String,    // UUID
    val user_context: UserContext? = null
)
```

**응답 검증 항목**:
- `classified_type`이 SCHEDULE/TODO/NOTES/TEMP 중 하나인지
- `note_sub_type`이 NOTES일 때만 존재하는지
- `confidence`가 HIGH/MEDIUM/LOW 중 하나인지
- `schedule_info`/`todo_info`가 해당 type일 때만 존재하는지

**검증**: 다양한 입력 텍스트 → 서버 분류 결과 정상 수신 + 파생 엔티티 생성

---

### 3-3: `/classify` split_items 실서버 연동

**목표**: 서버의 다중 의도 분리 응답을 클라이언트에서 올바르게 처리

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/dto/v2/SplitItemDto.kt` | 확인 | 서버 응답 구조와 DTO 일치 검증 |
| `data/worker/ClassifyCaptureWorker.kt` | 확인 | splitItems 파싱 로직이 실서버 응답과 호환되는지 |

**테스트 시나리오**:
| 입력 | 기대 결과 |
|------|----------|
| "보고서 쓰고 저녁 예약" | splitItems 2개 (TODO + SCHEDULE) |
| "내일 미팅" | splitItems null (단일 의도) |
| "카페 가고 책 사고 보고서 제출" | splitItems 3개 |

**검증**: 서버 split 응답 → 자식 Capture 생성 + 각각 올바른 파생 엔티티

---

### 3-4: `/calendar/events` 실서버 연동

**목표**: Google Calendar API 프록시를 통한 실제 캘린더 이벤트 생성/삭제

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/KairosApi.kt` | 확인 | calendar 엔드포인트 시그니처 검증 |
| `data/remote/dto/v2/CalendarEventRequest.kt` | 수정 | `capture_id` 필드 추가 (서버 스펙) |
| `data/repository/CalendarRepositoryImpl.kt` | 수정 | 에러 핸들링 (GOOGLE_AUTH_REQUIRED, GOOGLE_TOKEN_EXPIRED) |

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

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/KairosApi.kt` | 확인 | analytics 엔드포인트 시그니처 검증 |
| `data/remote/dto/v2/AnalyticsEventDto.kt` | 확인 | timestamp 포맷 ISO 8601 확인 |
| `data/worker/AnalyticsBatchWorker.kt` | 수정 | 에러 핸들링 + 실패 시 재시도 로직 |

**프라이버시 검증**:
- event_data에 원문 텍스트가 포함되지 않는지 확인
- device_id만 전송, 개인정보 미포함

**검증**: 이벤트 발생 → 1시간 내 서버 수신 확인, 네트워크 끊김 → 복구 후 재전송

---

### 3-6: MockKairosApi 제거 + 빌드 설정 정리

**목표**: Mock 코드 제거 및 빌드 설정 정리

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/MockKairosApi.kt` | **삭제** | Mock 구현체 제거 |
| `di/NetworkModule.kt` | 수정 | `USE_MOCK_API` 분기 제거, 항상 Retrofit 사용 |
| `app/build.gradle.kts` | 수정 | `USE_MOCK_API` BuildConfig 필드 제거, Base URL 정리 |

**빌드 설정 변경**:
```kotlin
// 변경 전
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000\"")
buildConfigField("Boolean", "USE_MOCK_API", "true")

// 변경 후
buildConfigField("String", "API_BASE_URL", "\"https://api.kairos.app\"")
// USE_MOCK_API 삭제
```

> `MockDataInitializer.kt`는 유지 (디버그 빌드에서 샘플 데이터 시딩 용도)

**검증**: `compileDebugKotlin` 통과, Mock 참조 코드 없음

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
- Mock 제거로 인한 테스트 깨짐 → 테스트용 Fake 구현체로 대체
- `FakeKairosApi` 생성 (테스트 전용, src/test 디렉토리)

| 파일 | 변경 | 내용 |
|------|------|------|
| `test/util/FakeKairosApi.kt` | 신규 | 테스트용 Fake API (기존 Mock 로직 축소판) |
| `test/**/*Test.kt` | 수정 | MockKairosApi → FakeKairosApi 교체 |

**검증**: `./gradlew testDebugUnitTest` 전체 통과 + 에뮬레이터 E2E 시나리오 통과

---

## 4. 서브페이즈 의존성

```
3-0 (서버 배포)
 └→ 3-1 (네트워크 정비)
     ├→ 3-2 (/classify 연동)
     │   └→ 3-3 (split_items 연동)
     ├→ 3-4 (/calendar 연동)
     └→ 3-5 (/analytics 연동)

3-2 + 3-3 + 3-4 + 3-5 → 3-6 (Mock 제거)
3-6 → 3-7 (통합 테스트)
```

---

## 5. 파일 변경 요약

| 구분 | 신규 | 수정 | 삭제 | 합계 |
|------|------|------|------|------|
| Data (API/DTO/Interceptor/Worker) | ~4 | ~8 | 1 | ~13 |
| Domain (Model) | ~2 | — | — | ~2 |
| DI | — | ~1 | — | ~1 |
| Config (build.gradle) | — | ~1 | — | ~1 |
| Test | ~1 | ~5 | — | ~6 |
| **합계** | **~7** | **~15** | **1** | **~23** |

---

## 6. 주의사항

- 서버 배포가 선행 조건: 서버 없이 3-2~3-5 진행 불가
- `X-Device-ID` 헤더는 Phase 3a(인증)까지 유지, JWT 전환은 별도 단계
- Rate Limit 테스트 시 서버 부하 주의
- Google Calendar 연동은 실제 Google 계정 필요
- Mock 제거 시 테스트용 FakeKairosApi 반드시 선행 작성
- 프라이버시: Analytics event_data에 원문 텍스트 절대 포함 금지

---

*Document Version: 1.0 | Last Updated: 2026-02-07*
