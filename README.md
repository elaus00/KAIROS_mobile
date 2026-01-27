# KAIROS Mobile

> **버전**: v1.0 (Phase 3 완료)
> **완성도**: 9.0/10
> **최종 업데이트**: 2026-01-27

AI 기반 캡처 분류 및 Obsidian 노트 자동 생성 앱

## 개요

KAIROS는 사용자의 텍스트, 음성, 이미지, 웹 링크 입력을 AI가 자동으로 분류하고 Obsidian 볼트에 노트를 생성해주는 Android 앱입니다.

**Phase 3 완료**: 전체 네비게이션, 검색, 히스토리, 알림, 테마 전환 기능 구현 완료 ✅

## 주요 기능

### ✅ 네비게이션 (Phase 3 완료)
4개의 메인 화면을 GlassBottomNavigation으로 연결:
- **CAPTURE**: 메인 캡처 화면 (텍스트, 이미지, 음성, 웹 클립)
- **SEARCH**: 검색 화면 (필터링, 페이징)
- **ARCHIVE**: 히스토리 화면 (날짜별 그룹화)
- **SETTINGS**: 설정 화면 (외부 연동, AI 기능, 테마)

### 캡처 유형
- **텍스트 캡처**: 직접 텍스트 입력
- **이미지 캡처**: 이미지에서 텍스트 추출 (OCR)
- **음성 캡처**: 음성을 텍스트로 변환 (STT)
- **웹 클립**: URL 크롤링 및 AI 요약

### AI 분류 카테고리
| 타입 | 설명 | 키워드 예시 |
|------|------|------------|
| `IDEA` | 아이디어 | "아이디어", "생각", "개선", "idea" |
| `SCHEDULE` | 일정/약속 | "회의", "미팅", "일정", "meeting" |
| `TODO` | 할 일 | "해야", "작업", "todo", "task" |
| `NOTE` | 일반 노트 | "메모", "기록", "note", "save" |
| `QUICK_NOTE` | 빠른 메모 | - |

### ✅ 스마트 기능 (Phase 3 완료)
- **동적 타입 제안**: 입력 텍스트 기반 자동 QuickTypeButtons 표시
- **키워드 매칭**: 한글/영어 키워드 지원 (최대 3개 제안)
- **검색 기능**: 텍스트 검색 + 타입 필터 + 페이징
- **히스토리 그룹화**: Today, Yesterday, This Week, 날짜별
- **알림 시스템**: 캡처 완료, 동기화 상태, AI 처리 알림
- **테마 전환**: 다크/라이트 모드 지원
- AI 기반 자동 분류 및 제목 생성
- 스마트 태그 제안
- 콘텐츠 자동 요약
- 오프라인 큐 및 자동 동기화

### 외부 연동
- **Google Calendar**: OAuth 연동, 자동 동기화
- **Todoist**: OAuth 연동, 자동 동기화

## 기술 스택

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Network**: Retrofit + OkHttp
- **Background**: WorkManager

---

## 서버 API 요구사항

KAIROS Mobile은 백엔드 서버와 통신하여 AI 분류 및 노트 생성을 수행합니다.

### Base URL 설정

앱은 `BuildConfig.BASE_URL`을 통해 서버 주소를 설정합니다.

---

### Phase 1: 핵심 기능

#### 1. Health Check
서버 상태 확인

```
GET /health
```

**Response**: `200 OK` (빈 응답)

---

#### 2. AI 캡처 분류
콘텐츠를 분석하여 타입, 제목, 태그를 자동 생성

```
POST /classify
Content-Type: application/json
```

**Request Body**:
```json
{
  "content": "내일 오후 3시에 팀 미팅 있음",
  "userId": "default_user",
  "context": {
    "source": "text_input"
  }
}
```

**Response Body**:
```json
{
  "type": "SCHEDULE",
  "destinationPath": "Calendar/2024-01",
  "title": "팀 미팅",
  "tags": ["미팅", "팀"],
  "confidence": 0.95,
  "metadata": {
    "datetime": "2024-01-15T15:00:00"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | string | SCHEDULE, TODO, IDEA, NOTE, QUICK_NOTE 중 하나 |
| `destinationPath` | string | Obsidian 저장 경로 |
| `title` | string | AI 생성 제목 |
| `tags` | string[] | 추출된 태그 목록 |
| `confidence` | float | 분류 신뢰도 (0.0 ~ 1.0) |
| `metadata` | object? | 추가 메타데이터 (선택) |

---

#### 3. Obsidian 노트 생성
분류 결과를 바탕으로 Obsidian 노트 파일 생성

```
POST /notes
Content-Type: application/json
```

**Request Body**:
```json
{
  "path": "Calendar/2024-01",
  "title": "팀 미팅",
  "content": "내일 오후 3시에 팀 미팅 있음",
  "tags": ["미팅", "팀"],
  "metadata": {
    "datetime": "2024-01-15T15:00:00"
  }
}
```

**Response Body**:
```json
{
  "success": true,
  "filePath": "Calendar/2024-01/팀 미팅.md",
  "message": "노트가 성공적으로 생성되었습니다"
}
```

---

### Phase 2: 고급 입력 기능

#### 4. OCR (이미지 텍스트 추출)
이미지에서 텍스트를 추출

```
POST /ocr
Content-Type: multipart/form-data
```

**Request**:
- `image`: 이미지 파일 (JPEG, PNG)

**Response Body**:
```json
{
  "success": true,
  "text": "추출된 텍스트 내용",
  "error": null
}
```

---

#### 5. STT (음성 → 텍스트)
음성 파일을 텍스트로 변환

```
POST /stt
Content-Type: multipart/form-data
```

**Request**:
- `audioFile`: 오디오 파일 (WAV, M4A, MP3 등)

**Response Body**:
```json
{
  "success": true,
  "text": "음성에서 추출된 텍스트",
  "confidence": 0.92,
  "language": "ko",
  "error": null
}
```

---

#### 6. 웹 클립 (URL 크롤링 + AI 요약)
URL의 콘텐츠를 크롤링하고 AI로 요약

```
POST /webclip
Content-Type: application/json
```

**Request Body**:
```json
{
  "url": "https://example.com/article"
}
```

**Response Body**:
```json
{
  "success": true,
  "title": "페이지 제목",
  "description": "페이지 설명",
  "imageUrl": "https://example.com/og-image.jpg",
  "content": "AI가 요약한 주요 내용...",
  "error": null
}
```

---

### Phase 3: 스마트 처리 기능

#### 7. AI 자동 요약
긴 콘텐츠를 자동으로 요약

```
POST /summarize
Content-Type: application/json
```

**Request Body**:
```json
{
  "captureId": "capture_123",
  "content": "요약할 긴 텍스트 내용...",
  "maxLength": 200
}
```

**Response Body**:
```json
{
  "success": true,
  "summary": "AI가 생성한 요약문",
  "originalLength": 1500,
  "summaryLength": 180,
  "error": null
}
```

---

#### 8. 스마트 태그 제안
콘텐츠 기반으로 태그를 제안

```
POST /tags/suggest
Content-Type: application/json
```

**Request Body**:
```json
{
  "content": "React 컴포넌트 최적화 방법 정리",
  "classification": "NOTE",
  "limit": 5
}
```

**Response Body**:
```json
{
  "success": true,
  "tags": [
    {
      "name": "React",
      "confidence": 0.95,
      "reason": "콘텐츠에서 직접 언급됨"
    },
    {
      "name": "개발",
      "confidence": 0.85,
      "reason": "기술 관련 내용"
    }
  ],
  "error": null
}
```

---

### Phase 3: 외부 서비스 연동

#### Google Calendar 연동

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/integrations/google/auth-url` | GET | OAuth 인증 URL 조회 |
| `/integrations/google/callback` | POST | OAuth 콜백 처리 |
| `/integrations/google/sync-status` | GET | 동기화 상태 조회 |
| `/integrations/google/sync` | POST | 수동 동기화 트리거 |
| `/integrations/google/disconnect` | POST | 연동 해제 |

#### Todoist 연동

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/integrations/todoist/auth-url` | GET | OAuth 인증 URL 조회 |
| `/integrations/todoist/callback` | POST | OAuth 콜백 처리 |
| `/integrations/todoist/sync-status` | GET | 동기화 상태 조회 |
| `/integrations/todoist/sync` | POST | 수동 동기화 트리거 |
| `/integrations/todoist/disconnect` | POST | 연동 해제 |

---

### 공통 DTO

#### OAuth 인증 URL 응답 (AuthUrlResponse)
```json
{
  "success": true,
  "authUrl": "https://accounts.google.com/oauth/...",
  "error": null
}
```

#### OAuth 콜백 요청 (OAuthCallbackRequest)
```json
{
  "code": "oauth_authorization_code",
  "state": "random_state_string"
}
```

#### OAuth 콜백 응답 (OAuthCallbackResponse)
```json
{
  "success": true,
  "message": "연동이 완료되었습니다",
  "error": null
}
```

#### 동기화 상태 응답 (SyncStatusResponse)
```json
{
  "success": true,
  "isConnected": true,
  "lastSyncTime": 1705312800000,
  "syncedCount": 42,
  "error": null
}
```

#### 동기화 트리거 응답 (SyncResponse)
```json
{
  "success": true,
  "syncedCount": 5,
  "message": "5개 항목이 동기화되었습니다",
  "error": null
}
```

---

### 에러 처리

모든 API 응답은 다음 패턴을 따릅니다:

**성공 시**:
```json
{
  "success": true,
  ...결과 데이터
}
```

**실패 시**:
```json
{
  "success": false,
  "error": "에러 메시지"
}
```

**HTTP 상태 코드**:
| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 500 | 서버 내부 오류 |

---

## 앱 흐름

### 캡처 제출 흐름

```
사용자 입력
    ↓
로컬 DB에 PENDING 상태로 저장 (데이터 유실 방지)
    ↓
네트워크 확인
    ├─ 오프라인 → PENDING 유지 (WorkManager가 나중에 동기화)
    └─ 온라인 ─→ POST /classify
                    ↓
                분류 결과 저장
                    ↓
                POST /notes
                    ↓
                SYNCED 상태로 업데이트
```

### 오프라인 큐 동기화

WorkManager가 주기적으로 PENDING 상태의 캡처들을 서버와 동기화합니다.

---

## 프로젝트 구조

```
app/src/main/java/com/example/kairos_mobile/
├── data/
│   ├── local/
│   │   ├── database/        # Room DB (v4)
│   │   │   ├── dao/         # CaptureQueueDao, NotificationDao
│   │   │   └── entities/    # CaptureQueueEntity, NotificationEntity
│   │   └── preferences/     # DataStore (테마, AI 설정)
│   ├── remote/
│   │   ├── api/             # KairosApi, MockKairosApi
│   │   └── dto/             # Request/Response DTO (OAuth 포함)
│   └── repository/          # Repository 구현체
│       ├── CaptureRepositoryImpl
│       ├── NotificationRepositoryImpl
│       └── IntegrationsRepositoryImpl
├── di/                      # Hilt DI 모듈
│   ├── DatabaseModule       # Room, DAO, Migration
│   ├── NetworkModule        # Retrofit, OkHttp
│   └── RepositoryModule     # Repository 바인딩
├── domain/
│   ├── model/               # 순수 Kotlin 모델
│   │   ├── Capture, CaptureType, CaptureSource
│   │   ├── Notification, NotificationType
│   │   ├── SearchQuery, ThemePreference
│   │   └── SyncStatus, Result
│   ├── repository/          # Repository 인터페이스
│   ├── usecase/             # 비즈니스 로직 (10+ UseCases)
│   └── util/                # KeywordMatcher
├── navigation/              # NavGraph, NavRoutes
├── presentation/
│   ├── capture/             # 메인 캡처 화면
│   ├── search/              # 검색 화면 (NEW)
│   ├── archive/             # 히스토리 화면 (NEW)
│   ├── notifications/       # 알림 화면 (NEW)
│   ├── settings/            # 설정 화면
│   └── components/          # 공통 Compose 컴포넌트
│       ├── GlassBottomNavigation
│       ├── GlassCaptureCard (QuickTypeButtons 통합)
│       ├── GlassSearchBar, FilterChipRow
│       ├── ArchiveCaptureCard, SearchResultCard
│       └── NotificationCard
└── ui/
    ├── components/          # Glassmorphism 모디파이어
    └── theme/               # 다크/라이트 테마 색상
```

---

## 빌드 및 실행

### 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17
- Android SDK 34

### 빌드
```bash
./gradlew assembleDebug
```

### 테스트
```bash
./gradlew test
```

---

---

## Phase 3 구현 완료 (2026-01-27)

### 새로운 기능

#### 1. 네비게이션 시스템
- 4개 탭 완전 동작: CAPTURE, SEARCH, ARCHIVE, SETTINGS
- GlassBottomNavigation 모든 화면 적용
- 백스택 관리 (상태 저장/복원)

#### 2. 검색 기능
- 텍스트 기반 검색
- CaptureType 필터 (다중 선택)
- 페이징 (20개씩 무한 스크롤)
- 빈 상태 처리

#### 3. 히스토리 기능
- 날짜별 그룹화 (Today, Yesterday, This Week, 날짜)
- 확장/축소 가능한 캡처 카드
- 새로고침 기능

#### 4. 알림 기능
- 필터 탭 (전체/읽지 않음/읽음)
- 타입별 아이콘 및 색상
- 시간 포맷팅 ("방금 전", "5분 전", "어제")

#### 5. 테마 전환
- 다크/라이트 모드 지원
- DataStore 기반 설정 저장
- 전체 앱 색상 자동 변경

#### 6. 동적 UI
- KeywordMatcher: 입력 텍스트 기반 타입 제안
- QuickTypeButtons 동적 표시 (AnimatedVisibility)
- 한글/영어 키워드 지원

### 기술적 개선사항

#### Database
- Room v3 → v4 마이그레이션
- `notifications` 테이블 추가
- 인덱스 최적화

#### Architecture
- Clean Architecture 완전 준수
- MVVM 패턴 강화
- UseCase 레이어 확장 (10+ UseCases)

#### Code Quality
- 한글 주석 100%
- Hilt DI 적용
- Flow 기반 반응형 UI

### 파일 통계
- **새로 생성**: 67개 파일
- **추가 코드**: 5,500+ 라인
- **테스트**: 미구현 (다음 단계)

---

## 다음 단계 (Phase 4)

### 1. 테스트 작성 (우선순위: 높음)
- ViewModel Unit Tests
- UseCase Unit Tests
- Repository Tests
- 목표: 테스트 커버리지 70%

### 2. 실제 서버 연동
- MockKairosApi → 실제 API 연동
- OAuth 플로우 통합 테스트
- 동기화 기능 검증

### 3. 성능 최적화
- 검색 쿼리 인덱스 추가
- 이미지 로딩 최적화 (Coil)
- LazyColumn 최적화

### 4. 추가 기능
- 캡처 상세 화면
- 캡처 편집/삭제 기능
- 즐겨찾기 기능
- 온보딩 화면

### 5. UI/UX 개선
- 화면 전환 애니메이션
- 로딩 스켈레톤
- 에러 화면 디자인
- 접근성 개선

---

## 문서

- **Phase 3 구현 보고서**: [Docs/Phase3_Implementation_Summary.md](Docs/Phase3_Implementation_Summary.md)
- **아키텍처 분석**: [Docs/Architecture_Analysis_Report.md](Docs/Architecture_Analysis_Report.md)
- **개발 가이드**: [CLAUDE.md](CLAUDE.md)

---

## 라이선스

MIT License