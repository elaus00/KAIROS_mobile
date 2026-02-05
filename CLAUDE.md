# KAIROS Mobile - Development Guidelines

## 프로젝트 개요
- **앱 종류**: 멀티모달 캡처 앱 (텍스트/이미지/음성/웹클립)
- **아키텍처**: Clean Architecture + MVVM
- **UI**: Jetpack Compose + Material 3 + Glassmorphism
- **오프라인 지원**: Room + WorkManager 기반 Offline-First

## 앱 철학: Guilt-Free Capture

### 핵심 원칙
> **"떠오른 순간, 바로 던지면 끝"**

KAIROS의 진짜 경쟁자는 다른 메모앱이 아니라 **"나중에 적어야지" 하다가 잊어버리는 것**이다.

### 세 가지 Zero

1. **캡처의 Zero Friction**
   - 앱 실행 → 바로 입력 가능 (이 순간이 가장 frictionless해야 함)
   - 인지적 부담 최소화: "뭘 선택해야 하지?" 고민 없이 그냥 적으면 됨
   - 아이디어는 휘발성이 있다 - 캡처를 놓치면 아이디어 자체가 사라짐

2. **정리의 Zero Guilt**
   - 사용자는 던지기만 하면 됨
   - 분류/태깅은 AI가 알아서 처리
   - "나중에 정리해야지"라는 부채감 자체를 없앤다

3. **실행의 Zero Effort**
   - 일정 → 구글 캘린더, 할 일 → Todoist 등 실제 action까지 AI가 처리
   - 기록에서 멈추지 않고 행동까지 연결
   - 연동 대상은 추후 확장 가능

### 설계 판단 기준
기능을 추가하거나 UI를 변경할 때 항상 질문할 것:
- "이게 캡처까지의 단계를 늘리는가?"
- "사용자에게 선택을 강요하는가?"
- "정리 부담을 전가하는가?"

하나라도 Yes면 다시 생각하라.

## 빌드 및 실행 명령어

```bash
# 전체 빌드
./gradlew build

# 유닛 테스트
./gradlew testDebugUnitTest

# Kotlin 컴파일만
./gradlew :app:compileDebugKotlin

# Debug APK 빌드
./gradlew assembleDebug

# 클린 빌드
./gradlew clean build
```

## 아키텍처 레이어 규칙

```
presentation/     # UI Layer (Compose, ViewModel)
  ├── components/ # 재사용 Compose 컴포넌트
  └── *Screen.kt  # 화면 단위 Composable

domain/           # Business Logic Layer
  ├── model/      # 도메인 모델 (순수 Kotlin)
  ├── repository/ # Repository 인터페이스
  └── usecase/    # UseCase 클래스

data/             # Data Layer
  ├── local/      # Room DB, DataStore
  ├── remote/     # Retrofit API, DTO
  ├── repository/ # Repository 구현체
  └── mapper/     # Entity <-> Domain 변환
```

**반드시 지켜야 할 규칙:**
- Domain Layer는 Android 의존성 없음 (순수 Kotlin)
- Presentation -> Domain -> Data 순으로만 의존
- DTO는 data/remote/dto/, Entity는 data/local/database/entities/

## 코드 스타일

- **주석**: 한글로 작성 필수
- **네이밍**: Kotlin 컨벤션 (camelCase, PascalCase)
- **들여쓰기**: 4 spaces
- **한 줄 최대**: 120자

## Compose 최적화 필수사항

```kotlin
// ❌ 금지: 리컴포지션마다 새 람다 생성
onClick = { viewModel.doSomething(item) }

// ✅ 권장: remember로 람다 캐싱
val onClick = remember(item.id) { { viewModel.doSomething(item) } }
```

- LazyColumn/LazyRow에 반드시 `key` 지정
- `derivedStateOf` 적극 활용
- `remember { }` 블록 내에서만 람다 생성

## Room Database 규칙

- 스키마 변경 시 반드시 Migration 작성
- Entity 변경 후 버전 번호 증가 필수
- `ksp { arg("room.schemaLocation", ...) }` 유지

## 보안 필수사항

**절대 금지:**
- `local.properties`, `.env`, `google-services.json` 직접 수정 금지
- SharedPreferences에 토큰 저장 → EncryptedSharedPreferences 사용
- 코드에 API 키 하드코딩 금지
- Production에서 Log.d/Log.v 사용 금지

## 오프라인 처리

```kotlin
// 캡처 저장 시 반드시 SyncStatus 체크
when (networkStatus) {
    Available -> api.submit() // 서버 전송
    Unavailable -> localDb.save(status = PENDING) // 로컬 저장
}
```

- 네트워크 없으면 `SyncStatus.PENDING`으로 로컬 저장
- WorkManager가 자동으로 백그라운드 동기화

## 테스트 전략

- 새로운 기능 추가 시 최소 ViewModel/UseCase 레이어 테스트 필요
- Mock 대신 실제 in-memory DB 사용 권장
- 테스트 파일: `*Test.kt` (Unit), `*AndroidTest.kt` (Instrumented)

## Phase 3 구현 완료 (2026-01-27)

### 추가된 기능
1. **네비게이션 시스템**
   - 4개 탭: CAPTURE, SEARCH, ARCHIVE, SETTINGS
   - GlassBottomNavigation 모든 화면에 적용
   - 백스택 관리 (popUpTo, saveState, restoreState)

2. **검색 기능** (`presentation/search/`)
   - 텍스트 검색, 타입 필터링
   - 페이징 (20개씩 무한 스크롤)
   - SearchScreen, SearchViewModel, SearchUiState

3. **히스토리 기능** (`presentation/archive/`)
   - 날짜별 그룹화 (Today, Yesterday, This Week)
   - 확장/축소 가능한 캡처 카드
   - ArchiveScreen, ArchiveViewModel, ArchiveUiState

4. **알림 기능** (`presentation/notifications/`)
   - 필터 탭 (전체/읽지 않음/읽음)
   - 타입별 아이콘/색상
   - NotificationsScreen, NotificationsViewModel

5. **테마 전환**
   - 다크/라이트 모드 지원
   - DataStore 기반 설정 저장
   - GlassmorphismLightColorScheme 추가

6. **동적 UI**
   - KeywordMatcher: 입력 텍스트 기반 타입 제안
   - QuickTypeButtons 동적 표시/숨김
   - 한글/영어 키워드 지원

### Database 변경
- **버전**: v3 → v4
- **추가 테이블**: `notifications`
- **마이그레이션**: MIGRATION_3_4 작성 완료

### 새로운 UseCase (10개)
```kotlin
SearchCapturesUseCase
GetAllCapturesUseCase
MatchKeywordsUseCase
GetNotificationsUseCase
AddNotificationUseCase
MarkNotificationAsReadUseCase
GetThemePreferenceUseCase
SetThemePreferenceUseCase
ConnectGoogleCalendarUseCase
ConnectTodoistUseCase
```

### 키워드 매칭 사용법
```kotlin
// 자동 타입 제안
val suggestedTypes = KeywordMatcher.matchTypes("팀 회의 일정")
// Result: [SCHEDULE]

val suggestedTypes = KeywordMatcher.matchTypes("해야 할 작업")
// Result: [TODO]
```

### 날짜 그룹화 로직
```kotlin
// getCapturesGroupedByDate()
// Returns: Map<String, List<Capture>>
// Keys: "Today", "Yesterday", "This Week", "2026-01-27"
```

## 참조 문서
- 아키텍처 상세: `@Docs/Architecture_Analysis_Report.md`
- Phase 3 구현 보고서: `@Docs/Phase3_Implementation_Summary.md`
- 모바일 디자인 스킬: `/mobile-design`
