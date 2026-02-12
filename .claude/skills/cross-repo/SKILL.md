# Cross-Repo Skill

Flit. 모바일 클라이언트와 서버를 동시에 다루는 크로스 레포 작업 스킬.
두 레포 사이의 API 계약, 스키마, 인증 흐름 등을 일관성 있게 유지합니다.

## 사용법

```
/cross-repo                          # 양쪽 레포 현황 요약
/cross-repo api-check                # API 계약 일치 여부 점검
/cross-repo sync-schema <endpoint>   # 특정 엔드포인트의 요청/응답 스키마 동기화
/cross-repo diff                     # 양쪽 미커밋 변경사항 비교
/cross-repo server <작업 설명>        # 서버 레포에서 작업 수행
```

## 레포지토리 맵

| 레포 | 역할 | 경로 | 언어/프레임워크 | 빌드 |
|------|------|------|----------------|------|
| Flit | Android 클라이언트 | `/Users/elaus/AndroidStudioProjects/Flit` | Kotlin / Jetpack Compose | `./gradlew` |
| KAIROS_Server | 백엔드 API + AI | `/Users/elaus/PycharmProjects/KAIROS_Server` | Python 3.12+ / FastAPI | `uv run` |

## API 계약 포인트 (Source of Truth)

서버의 `kairos-core/schemas/`가 API 계약의 정의 원본이다.

### 엔드포인트 ↔ 클라이언트 매핑

| 서버 라우터 | 서버 경로 | 클라이언트 대응 |
|------------|----------|----------------|
| `capture.py` | `/api/v1/classify`, `/api/v1/classify/batch` | `FlitApi.classify()`, `classifyBatch()` |
| `calendar.py` | `/api/v1/calendar/*` | `FlitApi.syncCalendar()` 등 |
| `analytics.py` | `/api/v1/analytics/*` | `FlitApi.getAnalytics()` 등 |
| `auth.py` | `/api/v1/auth/*` | `FlitApi.exchangeToken()`, `refreshToken()` |
| `subscription.py` | `/api/v1/subscription/*` | `FlitApi.getSubscription()`, `verifyPurchase()` |
| `notes.py` | `/api/v1/notes/*` | `FlitApi.groupNotes()`, `reorganizeNotes()` 등 |
| `ocr.py` | `/api/v1/ocr/*` | `FlitApi.extractText()` |

### 에러 코드 매핑

| 서버 에러 코드 | HTTP | 클라이언트 ApiException |
|---------------|------|----------------------|
| `INVALID_REQUEST` | 400 | `ApiException.InvalidRequest` |
| `UNAUTHORIZED` | 401 | `ApiException.Unauthorized` |
| `SUBSCRIPTION_REQUIRED` | 403 | `ApiException.SubscriptionRequired` |
| `RATE_LIMITED` | 429 | `ApiException.RateLimited` |
| `CLASSIFICATION_TIMEOUT` | 504 | `ApiException.ClassificationTimeout` |
| `AI_SERVICE_UNAVAILABLE` | 503 | `ApiException.ServiceUnavailable` |
| `INTERNAL_ERROR` | 500 | `ApiException.ServerError` |

### 인증 흐름

```
[Mobile] Google OAuth → idToken 획득
    ↓
[Server] POST /api/v1/auth/exchange → JWT 발급
    ↓
[Mobile] JWT를 EncryptedSharedPreferences에 저장
    ↓
[Mobile] AuthInterceptor가 매 요청에 Bearer 토큰 첨부
    ↓
[Server] kairos-auth JWT 검증 → 사용자 식별
```

### 공통 헤더

```
X-Device-ID: <device-uuid>     # 필수 (classify, calendar, analytics)
Authorization: Bearer <jwt>     # 인증 필요 엔드포인트
Content-Type: application/json
```

## 핵심 파일 위치

### 서버 (KAIROS_Server)

| 구분 | 경로 |
|------|------|
| API 라우터 | `apps/mobile-server/src/mobile_server/routers/` |
| 서비스 로직 | `apps/mobile-server/src/mobile_server/services/` |
| 의존성 주입 | `apps/mobile-server/src/mobile_server/dependencies.py` |
| 설정 | `apps/mobile-server/src/mobile_server/config.py` |
| API 스키마 | `packages/kairos-core/src/kairos_core/schemas/` |
| AI 서비스 | `packages/kairos-ai/src/kairos_ai/services/` |
| DB 스키마 | `packages/kairos-core/src/kairos_core/database/postgres/schema.sql` |
| 테스트 | `tests/unit/mobile_server/` |

### 클라이언트 (Flit)

| 구분 | 경로 |
|------|------|
| API 인터페이스 | `app/src/main/java/com/flit/app/data/remote/api/FlitApi.kt` |
| DTO (v2) | `app/src/main/java/com/flit/app/data/remote/dto/v2/` |
| Interceptor | `app/src/main/java/com/flit/app/data/remote/interceptor/` |
| Repository (인터페이스) | `app/src/main/java/com/flit/app/domain/repository/` |
| Repository (구현) | `app/src/main/java/com/flit/app/data/repository/` |
| UseCase | `app/src/main/java/com/flit/app/domain/usecase/` |
| ViewModel | `app/src/main/java/com/flit/app/presentation/viewmodels/` |
| Domain Model | `app/src/main/java/com/flit/app/domain/model/` |
| DB (Room) | `app/src/main/java/com/flit/app/data/local/database/` |
| Mapper | `app/src/main/java/com/flit/app/data/mapper/` |

### 클라이언트 주요 DTO 파일

| 파일 | 용도 |
|------|------|
| `ClassifyRequest.kt` | 분류 요청 (단일/배치) |
| `ClassifyResponse.kt` | 분류 응답 (엔티티, 스케줄, 할일 정보) |
| `AuthDto.kt` | 인증 요청/응답 (OAuth, 토큰 갱신, 사용자 정보) |
| `CalendarDto.kt` | 캘린더 관련 DTO |
| `SubscriptionDto.kt` | 구독 검증/정보 DTO |
| `NoteAiDto.kt` | AI 노트 작업 (그룹화, 재정리, 시맨틱 검색) |
| `AnalyticsDto.kt` | 분석 이벤트 DTO |
| `ApiEnvelope.kt` | 공통 API 응답 래퍼 (status, data, error) |

### 클라이언트 네트워크 인터셉터

| 인터셉터 | 역할 |
|---------|------|
| `AuthInterceptor.kt` | SharedPreferences에서 JWT 꺼내 Authorization 헤더 첨부 |
| `DeviceIdInterceptor.kt` | classify, calendar, analytics 엔드포인트에 X-Device-ID 헤더 첨부 |
| `ErrorInterceptor.kt` | IOException/SocketTimeoutException → ApiException.NetworkError 변환 |

## 실행 규칙

### 크로스 레포 작업 시 원칙

1. **스키마 변경은 서버 먼저** → 서버 스키마 수정 → 클라이언트 DTO 동기화 순서
2. **한쪽만 수정하지 않기** → API 계약 변경 시 반드시 양쪽 확인
3. **git 명령은 레포 경로 명시** → `cd /path/to/repo && git ...` 형태로 실행
4. **테스트는 각 레포에서 독립 실행**
   - 서버: `cd /Users/elaus/PycharmProjects/KAIROS_Server && uv run pytest`
   - 클라이언트: `./gradlew testDebugUnitTest`

### API 계약 점검 절차 (/cross-repo api-check)

1. 서버의 `kairos_core/schemas/*.py`에서 Request/Response 모델 읽기
2. 클라이언트의 `dto/v2/*.kt`에서 대응하는 데이터 클래스 읽기
3. 필드명, 타입, nullable 여부, 기본값 대조
4. 불일치 항목을 테이블로 보고

### 양쪽 현황 요약 (/cross-repo)

1. 양쪽 `git status`, `git log --oneline -5` 실행
2. 현재 브랜치, 미커밋 변경, 최근 작업 요약
3. API 계약 관련 변경이 있으면 경고 표시

## 주의사항

- 서버 레포에서 `uv run` 명령 시 `.venv`가 있는 서버 디렉토리에서 실행
- 클라이언트 DTO는 Gson `@SerializedName` 어노테이션으로 JSON 키 매핑
- 클라이언트는 `ApiEnvelope<T>` 래퍼로 모든 응답을 감싸서 처리

### 타입 매핑 참조

| Python (서버) | Kotlin (클라이언트) | 비고 |
|--------------|-------------------|------|
| `str` | `String` | |
| `int` | `Int` / `Long` | |
| `float` | `Double` | |
| `bool` | `Boolean` | |
| `list[T]` | `List<T>` | |
| `dict[K, V]` | `Map<K, V>` | |
| `Optional[T]` | `T?` | nullable |
| `datetime` | `String` (ISO 8601) | 서버에서 직렬화 |
| `UUID` | `String` | |
| `Enum` | `String` / `enum class` | `@SerializedName` 사용 |
- 서버 커밋 시에도 `/commit` 스킬 규칙 동일 적용 (한글 본문, 타입 영문)
- 양쪽 레포의 CLAUDE.md 설계 원칙("Just Capture")은 동일 — 항상 존중
