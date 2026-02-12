# CalendarProvider 전환 및 비로그인 우선 플로우 작업 정리

작성일: 2026-02-12

## 1) 작업 목표
- 서버 Google Calendar 연동 의존을 제거하고, Android CalendarProvider 기반 로컬 캘린더 연동으로 전환
- 온보딩/설정에서 OAuth 대신 런타임 캘린더 권한 기반 UX로 변경
- 로그아웃 시 로컬 데이터 유지 정책 적용
- 가입 직후 초기 동기화를 위한 Sync 스캐폴딩 추가

## 2) 완료된 핵심 변경

### A. CalendarProvider 전환 (Phase 1 핵심)
- `CalendarRepository` 인터페이스를 로컬 캘린더 중심으로 변경
- `CalendarRepositoryImpl`을 ContentResolver + CalendarContract 기반으로 전면 재작성
  - 일정 insert
  - 권한 체크
  - 사용 가능한 캘린더 목록 조회
  - 대상 캘린더 선택/저장
- 캘린더 에러 모델을 서버/OAuth 중심에서 로컬 권한 중심으로 변경
  - `CalendarApiException` 삭제
  - `CalendarException` 추가

### B. 서버 캘린더 API 의존 제거
- 클라이언트의 캘린더 API endpoint 제거
- 관련 DTO/모델 제거
- OAuth URL builder 제거
- MainActivity의 OAuth callback/datalink 처리 제거
- AndroidManifest에서 OAuth intent-filter 제거

### C. 스케줄 필드명 리네이밍
- Kotlin 필드: `googleEventId` -> `calendarEventId`
- DB 컬럼명은 `google_event_id` 유지 (`@ColumnInfo(name = "google_event_id")`)
- Mapper/DAO/Test fixture 동기화

### D. Worker 단순화
- `CalendarSyncWorker`에서 원격 pull/merge 로직 제거
- `SYNC_FAILED` 재시도 로직만 유지

### E. 설정/온보딩 UX 변경 (Phase 2)
- 설정 화면
  - "Google Calendar" -> "캘린더 연동"
  - OAuth 액션 제거
  - 캘린더 권한 요청 버튼 추가
  - 캘린더 목록 드롭다운/대상 캘린더 선택 추가
- 온보딩 화면
  - Google 연결 mock 흐름 제거
  - 캘린더 권한 요청 기반으로 변경
  - 권한 거부 시 앱 진행 가능(나중에 설정에서 활성화)

### F. 로그인/동기화 기반 작업 (Phase 3 일부)
- Credential Manager + Google ID 라이브러리 의존성 추가
- 로그인 화면/뷰모델에 Google ID token 획득 및 처리 연결
- 로그인 성공 시 `InitialSyncUseCase` 호출
- `SyncDto`, `SyncRepository`, `SyncRepositoryImpl`, `SyncResult`, `InitialSyncUseCase` 추가
- 계정 전환 시 자동 push/pull 방지 가드 추가 (`last_sync_user_id` 기준)

### G. 로그아웃 정책 변경
- `AuthRepositoryImpl.logout()`에서 `database.clearAllTables()` 제거
- 인증 토큰/계정 캐시만 제거, 로컬 데이터는 유지

## 3) 검증 결과
- `./gradlew :app:compileDebugKotlin` 성공
- `./gradlew testDebugUnitTest` 성공

## 4) 남은 TODO / 주의사항
1. `SyncRepositoryImpl.pullServerData()`는 현재 서버 변경사항 "수신/커서 저장"까지만 구현됨
- 로컬 DB 반영(upsert/delete)은 서버 sync 스키마 최종 확정 후 구현 필요

2. Credential Manager 로그인 동작을 실사용하려면 `GOOGLE_WEB_CLIENT_ID` 설정 필요
- buildConfig 필드로 추가되어 있으나 현재 기본값은 빈 문자열

3. 계정 전환 UX 정책 확정 필요
- 현재는 데이터 오염 방지를 위해 계정 변경 감지 시 자동 동기화를 건너뛰도록 가드 적용
- 제품 정책에 맞춘 사용자 안내/수동 선택 UX 보완 권장

## 5) 주요 변경 파일(대표)
- `app/src/main/java/com/example/kairos_mobile/domain/repository/CalendarRepository.kt`
- `app/src/main/java/com/example/kairos_mobile/data/repository/CalendarRepositoryImpl.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/settings/SettingsViewModel.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/onboarding/OnboardingViewModel.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/auth/LoginScreen.kt`
- `app/src/main/java/com/example/kairos_mobile/presentation/auth/LoginViewModel.kt`
- `app/src/main/java/com/example/kairos_mobile/data/repository/SyncRepositoryImpl.kt`
- `app/src/main/java/com/example/kairos_mobile/domain/usecase/sync/InitialSyncUseCase.kt`

