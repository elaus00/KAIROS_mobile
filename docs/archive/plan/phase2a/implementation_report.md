# Phase 2a 구현 완료 보고서

> 작성일: 2026-02-07
> 브랜치: `feature/phase2`
> 상태: **구현 완료** (컴파일 통과 + 96개 유닛 테스트 전부 통과)

---

## 1. 개요

Phase 2a는 Phase 1 MVP의 핵심 루프를 일상 사용 가능 수준으로 완성하는 단계이다. DB v11 → v12 마이그레이션, 이미지 첨부, 휴지통(30일 보존), Google Calendar 동기화(Mock), 할일 고도화, 분류 로깅/분석 이벤트, 입력 위젯, 알림, 온보딩 확장을 포함한다.

**총 81개 파일** (36 신규, 44 수정, 1 삭제)에 걸쳐 구현되었다.

---

## 2. 서브페이즈별 구현 현황

| # | 서브페이즈 | 설명 | 상태 |
|---|-----------|------|------|
| 2a-0 | 레거시 정리 | DatabaseMigrations.kt 삭제, DI 정리 | **완료** |
| 2a-1 | DB v12 + 전 레이어 기반 | 스키마 변경 5테이블, 신규 2테이블, Mapper/Repository/DI | **완료** |
| 2a-2 | 휴지통 (30일 보존) | 3단계 삭제 모델, TrashScreen, TrashCleanupWorker | **완료** |
| 2a-3 | 이미지 첨부 | ImageRepository, 갤러리/카메라 첨부, 미리보기 | **완료** |
| 2a-4 | 할일 고도화 | 드래그 순서 변경, 완료 항목 토글, AI 마감일 배지 | **완료** |
| 2a-5 | 분류 로깅 + 분석 이벤트 | ClassificationLog, AnalyticsEvent, TrackEventUseCase | **완료** |
| 2a-6 | Google Calendar 동기화 | MockAPI, confidence 기반 AUTO/SUGGEST, 승인/거부 | **완료** |
| 2a-7 | 알림 | NotificationHelper, 제안/자동추가 알림 | **완료** |
| 2a-8 | 설정 확장 + 온보딩 | 캘린더 설정, 온보딩 Google 연결 페이지 | **완료** |
| 2a-9 | 입력 위젯 | Glance AppWidget, 4×1 홈 화면 위젯 | **완료** |

---

## 3. 아키텍처 변경 요약

### 3.1 DB 스키마 (v11 → v12)

| 테이블 | 변경 |
|--------|------|
| `captures` | `is_trashed`, `trashed_at`, `image_uri` 컬럼 추가 |
| `todos` | `deadline_source`, `sort_source` 컬럼 추가 |
| `schedules` | `calendar_sync_status`, `google_event_id` 컬럼 추가 + 인덱스 |
| `classification_logs` | **신규** — 분류 변경 이력 |
| `analytics_events` | **신규** — 분석 이벤트 |

### 3.2 Domain 모델 변경

- `Capture`: `isTrashed`, `trashedAt`, `imageUri` 추가
- `Todo`: `deadlineSource` (DeadlineSource enum), `sortSource` (SortSource enum) 추가
- `Schedule`: `calendarSyncStatus` (CalendarSyncStatus enum), `googleEventId` 추가
- **신규 모델**: `ClassificationLog`, `AnalyticsEvent`, `CalendarSyncStatus`, `DeadlineSource`, `SortSource`

### 3.3 UseCase 추가 (17개)

| 카테고리 | UseCase |
|---------|---------|
| 휴지통 | `MoveToTrashUseCase`, `RestoreFromTrashUseCase`, `GetTrashItemsUseCase`, `EmptyTrashUseCase` |
| 할일 | `ReorderTodoUseCase`, `GetCompletedTodosUseCase` |
| 분석 | `TrackEventUseCase` |
| 캘린더 | `SyncScheduleToCalendarUseCase`, `ApproveCalendarSuggestionUseCase`, `RejectCalendarSuggestionUseCase`, `DeleteCalendarEventUseCase` |
| 설정 | `GetCalendarSettingsUseCase`, `SetCalendarSettingsUseCase` |

### 3.4 Worker 추가 (3개)

| Worker | 주기 | 역할 |
|--------|------|------|
| `TrashCleanupWorker` | 1일 | 30일 경과 휴지통 항목 hard delete |
| `AnalyticsBatchWorker` | 1시간 | 미전송 분석 이벤트 배치 업로드 |
| `CalendarSyncWorker` | 1시간 | SYNC_FAILED 상태 일정 재시도 |

### 3.5 UI 변경

| 화면 | 변경 |
|------|------|
| **TrashScreen** (신규) | 휴지통 목록, 복원, 비우기 |
| **CalendarScreen** | 일정에 동기화 상태 배지 + 제안 승인/거부 버튼 |
| **CaptureContent** | 이미지 첨부 버튼(📎), 미리보기, 삭제 |
| **CaptureDetailScreen** | 첨부 이미지 표시 + 캘린더 동기화 섹션 |
| **SettingsScreen** | 휴지통 항목, 캘린더 설정 |
| **OnboardingScreen** | 4페이지 (기존 3 + Google Calendar 연결) |
| **ScheduleTimeline** | 동기화 상태 배지 (동기화됨/제안/실패/거부) |
| **TaskList** | 완료 항목 토글, 드래그 핸들 |
| **CaptureWidget** (신규) | 4×1 홈 화면 위젯 |

---

## 4. 핵심 설계 결정

### 4.1 CalendarNotifier 인터페이스

Domain 계층에서 Android NotificationHelper를 직접 참조하지 않기 위해 `CalendarNotifier` 인터페이스를 Domain 레이어에 정의하고 `NotificationHelper`가 구현. DI `RepositoryModule`에서 바인딩.

```
Domain: CalendarNotifier (interface)
Data: NotificationHelper implements CalendarNotifier
```

### 4.2 3단계 삭제 모델

```
Active → SoftDelete (3초 Snackbar) → Trash (30일 보존) → HardDelete
           ↑ Undo                      ↑ Restore
```

- `isDeleted=true`: Snackbar 3초 동안 실행 취소 가능
- `isTrashed=true, trashedAt=now`: 휴지통 진입, 목록에서 숨김
- `hardDelete`: 캡처 + 파생 엔티티 완전 삭제

### 4.3 Calendar Sync 상태 머신

```
NOT_LINKED
  ├─ HIGH confidence → SYNCED (자동) + 완료 알림
  ├─ MEDIUM/LOW → SUGGESTION_PENDING + 제안 알림
  │   ├─ 승인 → SYNCED
  │   └─ 거부 → REJECTED
  └─ 동기화 실패 → SYNC_FAILED (CalendarSyncWorker가 재시도)
```

### 4.4 Destructive Migration 유지

Phase 1과 동일하게 Room `fallbackToDestructiveMigration()` 사용. 프로덕션 배포 시 Migration 코드로 교체 필요.

---

## 5. 파일 규모

| 계층 | 신규 | 수정 | 삭제 |
|------|------|------|------|
| Data (Entity/DAO/Mapper/Repo/Worker/API) | 13 | 15 | 1 |
| Domain (Model/Repository/UseCase) | 14 | 7 | — |
| Presentation (Screen/ViewModel/UiState) | 5 | 13 | — |
| Widget/Navigation | 4 | 1 | — |
| Config/Tests | — | 8 | — |
| **합계** | **36** | **44** | **1** |

---

## 6. 테스트 현황

- **컴파일**: `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL
- **유닛 테스트**: `./gradlew testDebugUnitTest` — **96개 전부 PASSED**
- 기존 Phase 1 테스트 모두 호환성 유지 (mock 파라미터 추가로 해결)

---

## 7. 미완성/후속 작업

| 항목 | 상태 | 비고 |
|------|------|------|
| 실제 Google OAuth 연동 | 미구현 | MockFlitApi 기반, 서버 준비 후 교체 |
| Room Migration 코드 | 미작성 | 현재 destructive migration, 프로덕션 배포 전 작성 필요 |
| 에뮬레이터 통합 테스트 | 미실행 | UI 시나리오 수동 검증 필요 |
| Widget 테스트 | 미작성 | Glance 위젯은 UI 테스트로 검증 필요 |
| 화면정의서 업데이트 | 미작성 | TrashScreen, 캘린더 sync UI 등 반영 필요 |

---

## 8. API 전략

현재 모든 외부 API 호출은 `MockFlitApi`를 통해 처리:
- `/calendar/events` POST/DELETE/GET — Mock 응답
- `/analytics/events` POST — Mock 응답 (성공)
- 실제 Google OAuth/Calendar API 연동은 Phase 3에서 진행

---

## 9. 결론

Phase 2a의 10개 서브페이즈 전체 구현 완료. 코어 루프(캡처 → AI 분류 → 파생 엔티티 → 일정 관리)를 일상 사용 가능 수준으로 확장했다. 다음 단계는 Phase 2b (사용성 확장: 노트 상세/편집, 할 일 위젯, AI 학습)이다.
