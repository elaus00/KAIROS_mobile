# KAIROS Mobile

> **"떠오른 순간, 바로 던지면 끝"**

떠오르는 생각을 즉시 기록하면, AI가 자동으로 일정·할 일·아이디어·노트로 분류하여 정리해주는 Android 개인 생산성 앱.

## 핵심 철학: Just Capture

| 원칙 | 설명 |
|------|------|
| **Just Capture** | 앱 실행 → 즉시 입력. 분류 고민 없이, 그냥 적으면 된다 |
| **Auto Organize** | 분류·태깅·목적지 결정은 AI가 처리한다 |
| **Straight to Action** | 일정은 캘린더로, 할 일은 투두로 자동 연결된다 |

## 주요 기능

### 캡처
- 텍스트 직접 입력 (앱 진입 시 즉시 접근)
- 이미지 첨부 (갤러리/카메라)
- 외부 앱 공유 인텐트 수신
- 입력 위젯 (홈 화면에서 바로 캡처)
- 로컬 우선 저장 (오프라인 지원)
- 임시 저장 (앱 이탈 시 자동 보존)

### AI 자동 분류
캡처된 내용을 AI가 분석하여 유형과 목적지를 자동 결정:

| 내부 유형 | 사용자 표시 | 목적지 |
|-----------|------------|--------|
| SCHEDULE | 일정 | 캘린더 탭 → Google Calendar 동기화 |
| TODO | 할 일 | 캘린더 탭 할 일 섹션 |
| NOTES (IDEA) | 아이디어 | 노트 / Ideas 폴더 |
| NOTES (INBOX) | 노트 | 노트 / Inbox 폴더 |
| TEMP | — | 미분류 임시 상태 (AI 자동 재분류 대기) |

- AI 제목 생성 (원문 기반 요약, 원본 텍스트 보존)
- 자동 태깅 및 핵심 엔티티 추출 (사람, 장소, 날짜, 금액)
- 신뢰도 기반 처리 (High/Medium/Low)
- 사후 분류 수정 + 파생 객체 자동 전환
- 분류 수정 이력 로깅

### 일정 관리
- Google Calendar 동기화 (신뢰도 기반 조건부 쓰기)
- 일정 제안 승인/거부 플로우
- 일정 추가 모드 설정 (자동/제안/수동)

### 할 일 관리
- AI 마감일 제안
- 완료 토글 및 완료 항목 보기
- 드래그 순서 변경

### 노트
- 폴더 기반 관리 (Inbox, Ideas, 사용자 폴더)
- 폴더 CRUD (생성/이름 변경/삭제)
- 노트 폴더 이동

### 삭제 & 휴지통
- 3단계 삭제 모델: Active → SoftDelete(3초 실행 취소) → Trash(30일 보관) → 영구 삭제
- 휴지통 복원 및 비우기

### 검색 & 히스토리
- FTS 기반 텍스트 검색
- 분류 유형 필터, 날짜 범위 필터
- 날짜별 그룹화 히스토리 (Today, Yesterday, This Week, ...)

### 알림
- 일정 제안/자동추가 로컬 푸시 알림
- AI 분류 현황 뱃지 및 확인/수정 전용 화면

### 기타
- 다크/라이트 테마 전환
- 온보딩 (앱 소개, AI 분류 안내, Google Calendar 연결)
- 분류 품질 관측 지표 수집 (분류 수정률, 일정 제안 승인율 등)

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture (단방향 의존) + MVVM |
| DI | Hilt |
| Database | Room (v12, 14 Entity, 14 DAO) |
| Network | Retrofit + OkHttp |
| Background | WorkManager (5 Worker) |
| Widget | Glance AppWidget |
| Local Storage | EncryptedSharedPreferences, DataStore |

## 아키텍처

```
presentation/ → domain/ → data/ (단방향 의존)
```

- **Domain Layer**: 순수 Kotlin (Android 의존성 없음), 43 UseCase
- **Data Layer**: Room DB, Retrofit, WorkManager, Repository 구현체
- **Presentation Layer**: Compose UI, ViewModel, Navigation

### CaptureEntity 중심 설계
모든 데이터의 시작점은 Capture. Todo/Schedule/Note는 capture_id FK로 연결된 파생 엔티티.
분류 변경 시 기존 파생 엔티티 삭제 → 새 파생 엔티티 생성 (@Transaction).

## 프로젝트 구조

```
app/src/main/java/com/example/kairos_mobile/
├── data/
│   ├── local/database/          # Room DB v12
│   │   ├── dao/                 # 14 DAO
│   │   └── entities/            # 14 Entity (Capture, Todo, Schedule, Note, Folder, Tag, ...)
│   ├── mapper/                  # Entity ↔ Domain 변환
│   ├── notification/            # CalendarNotifier 구현체
│   ├── remote/
│   │   ├── api/                 # KairosApi, MockKairosApi
│   │   └── dto/v2/             # Request/Response DTO
│   ├── repository/              # Repository 구현체 (9개)
│   └── worker/                  # 5 Worker (Classify, ReclassifyTemp, TrashCleanup, AnalyticsBatch, CalendarSync)
├── di/                          # Hilt 모듈 (App, Network, Repository)
├── domain/
│   ├── model/                   # 순수 Kotlin 모델 (Capture, Classification, Folder, Tag, ...)
│   ├── repository/              # Repository 인터페이스
│   └── usecase/                 # 43 UseCase (capture, classification, calendar, todo, note, folder, search, settings, analytics)
├── navigation/                  # NavGraph, NavRoutes
├── presentation/
│   ├── capture/                 # 메인 캡처 화면
│   ├── calendar/                # 캘린더 탭 (일정 + 할 일)
│   ├── classification/          # AI 분류 현황 바텀시트
│   ├── detail/                  # 캡처 상세
│   ├── history/                 # 히스토리 화면
│   ├── main/                    # MainScreen (하단 네비게이션)
│   ├── notes/                   # 노트 탭 (폴더 + 노트 목록)
│   ├── onboarding/              # 온보딩 4페이지
│   ├── search/                  # 검색 화면
│   ├── settings/                # 설정 화면
│   ├── trash/                   # 휴지통 화면
│   ├── widget/                  # 입력 위젯
│   └── components/              # 공통 Compose 컴포넌트
└── ui/
    ├── components/              # Glassmorphism 모디파이어
    └── theme/                   # 다크/라이트 테마
```

## 개발 현황

| Phase | 목표 | 상태 |
|-------|------|------|
| **Phase 1: MVP** | 캡처 → AI 분류 → 관리 핵심 루프 | **완료** |
| **Phase 2a: 코어 루프 완성** | 휴지통, 이미지 첨부, 할일 고도화, 캘린더 동기화, 위젯, 알림 | **완료** |
| **Phase 2b: 사용성 확장** | 멀티 인텐트 분할, 노트 편집, 필터링, 할일 위젯 | 다음 단계 |
| Phase 3: 서버 연동 | Mock → 실제 FastAPI 서버 전환 | 예정 |
| Phase 3a+: 고도화 | 사용자 인증, 구독, AI 개인화 | 예정 |

- **빌드**: `compileDebugKotlin` 통과
- **테스트**: 96개 유닛 테스트 전부 PASSED
- **구현 규모**: 120+ Kotlin 소스 파일, 16 테스트 파일

## 빌드 및 실행

### 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17
- Android SDK 34

### 빌드
```bash
./gradlew assembleDebug
```

### 컴파일 체크
```bash
./gradlew :app:compileDebugKotlin
```

### 테스트
```bash
./gradlew testDebugUnitTest
```

## 문서

| 문서 | 위치 |
|------|------|
| PRD (최상위) | `Docs/direction/kairos_prd_v10.md` |
| 기능명세서 | `Docs/specs/functional_spec.md` |
| 화면정의서 | `Docs/specs/screen-spec.md` |
| 데이터모델 명세서 | `Docs/specs/data_model_spec.md` |
| API 명세서 | `Docs/specs/api_spec.md` |
| 개발 로드맵 | `Docs/ROADMAP.md` |
| 문서 인덱스 | `Docs/INDEX.md` |

## 라이선스

MIT License