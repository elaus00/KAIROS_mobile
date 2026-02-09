# Phase 2b 구현 완료 보고서

> **작성일**: 2026-02-09
> **브랜치**: `phase2-qa1` (from `feature/phase2`)
> **상태**: **구현 완료** (컴파일 통과 + 112개 유닛 테스트 전부 통과)
> **선행 조건**: Phase 2a 완료 (81개 파일, 96개 테스트)

---

## 1. 개요

Phase 2b는 멀티 인텐트 분할, 노트 편집, 할일 위젯, 히스토리/검색 필터, 관측 지표 확장을 구현하여 최소 출시 기준을 달성하는 단계이다.

Phase 2a 완료 후 코드 구현이 진행되어, 계획서(2026-02-07 작성)에서 "미구현"으로 표기된 모든 기능이 DB v16 기준으로 이미 반영되었다.

---

## 2. 서브페이즈별 구현 현황

| # | 서브페이즈 | 설명 | 상태 |
|---|-----------|------|------|
| 2b-0 | DB 스키마 + 전 레이어 기반 | `parent_capture_id`, `note.body`, `SplitItem`, `SplitItemDto` | **완료** |
| 2b-1 | 멀티 인텐트 분할 | `ProcessClassificationResultUseCase`에서 splitItems 처리 | **완료** |
| 2b-2 | 노트 상세/편집 | `NoteDetailScreen` + `NoteDetailViewModel` + `UpdateNoteUseCase` | **완료** |
| 2b-3 | 폴더 이름 검증 | `CreateFolderUseCase`/`RenameFolderUseCase`에 30자 제한 | **완료** |
| 2b-4 | 히스토리 필터링 | 타입 필터 + `DateRangeChipRow` + `getFilteredCapturesPaged` | **완료** |
| 2b-5 | 검색 필터 확장 | `searchFiltered` DAO→Repo→UseCase→ViewModel 전체 | **완료** |
| 2b-6 | 할 일 위젯 (4×2) | `TodoWidgetProvider` + `TodoWidgetService` + `TodoWidgetViewsFactory` | **완료** |
| 2b-7 | 확장 관측 지표 | 4개 이벤트 (`capture_revisited`, `todo_completed`, `search_performed`, `split_capture_created`) | **완료** |
| 2b-8 | 테스트 | 기존 112개 통과, 추가 테스트 작성 중 | **진행 중** |

---

## 3. 계획서 대비 실제 구현 대조

계획서(2026-02-07)에서 "미존재"로 기록된 항목과 실제 코드 상태:

| 계획서 항목 | 계획서 상태 | 실제 상태 | 근거 |
|------------|:---:|:---:|------|
| `CaptureEntity.parent_capture_id` | ❌ | ✅ | 필드 + 인덱스 존재 (DB v16) |
| `NoteEntity.body` | ❌ | ✅ | `body: String? = null` 필드 존재 |
| `Capture.parentCaptureId` 도메인 모델 | ❌ | ✅ | 도메인 모델에 존재 |
| `Note.body` 도메인 모델 | ❌ | ✅ | 도메인 모델에 존재 |
| `Classification.splitItems` | ❌ | ✅ | `splitItems: List<SplitItem>?` 존재 |
| `SplitItem` / `SplitItemDto` | ❌ | ✅ | 둘 다 존재, Mapper 포함 |
| `CreateFolderUseCase` 30자 검증 | ❌ | ✅ | `require(name.length <= 30)` 존재 |
| `CaptureSearchDao.searchFiltered` | ❌ | ✅ | 필터 쿼리 구현 완료 |
| `TodoWidgetProvider` | ❌ | ✅ | 전체 구현 (Provider + Service + Factory) |
| `DateRangeChipRow` | ❌ | ✅ | HistoryScreen에 구현 |
| 4개 분석 이벤트 | ❌ | ✅ | 각 ViewModel/UseCase에 발행 코드 존재 |

---

## 4. 주요 구현 내역

### 4.1 멀티 인텐트 분할 (2b-1)

`ProcessClassificationResultUseCase`에서 `splitItems` 처리:
- splitItems가 존재하면 `processSplitItems()` 호출
- 각 splitItem에 대해 자식 Capture 생성 (source=SPLIT, parentCaptureId 설정)
- 자식별로 단일 의도 처리 재귀 호출
- `split_capture_created` 이벤트 발행

### 4.2 노트 상세/편집 (2b-2)

- `NoteDetailScreen`: 제목/본문 편집, 폴더 변경, 분류 칩, 저장 버튼
- `NoteDetailViewModel`: hasChanges 감지, onSave() 시 title/body/folder 각각 업데이트
- `UpdateNoteUseCase`: updateTitle, updateBody, moveToFolder 3개 메서드
- `capture_revisited` 이벤트 첫 로드 시 발행

### 4.3 히스토리/검색 필터 (2b-4, 2b-5)

- `CaptureDao.getFilteredCapturesPaged`: nullable 파라미터 조건부 쿼리
- `CaptureSearchDao.searchFiltered`: FTS + 타입/날짜 필터 조합
- `HistoryScreen`: FilterChipRow (타입) + DateRangeChipRow (전체/오늘/이번주/이번달)
- `SearchViewModel`: setTypeFilter 시 searchFiltered 호출

### 4.4 할 일 위젯 (2b-6)

- `TodoWidgetProvider`: AppWidgetProvider + BroadcastReceiver (체크박스 토글)
- `TodoWidgetService`: RemoteViewsService
- `TodoWidgetViewsFactory`: Room에서 오늘 마감 미완료 할일 조회, Hilt EntryPoint 사용
- 레이아웃: `widget_todo.xml` (4×2), `widget_todo_item.xml` (체크박스 + 제목 + 마감시간)

### 4.5 확장 관측 지표 (2b-7)

| 이벤트 | 발행 위치 | event_data |
|--------|----------|-----------|
| `capture_revisited` | CaptureDetailViewModel, NoteDetailViewModel | time_since_creation_ms |
| `todo_completed` | ToggleTodoCompletionUseCase | time_since_creation_ms |
| `search_performed` | SearchViewModel | result_count |
| `split_capture_created` | ProcessClassificationResultUseCase | parent_capture_id, split_count |

---

## 5. 테스트 현황 (2b-8)

### 5.1 기존 테스트 (112개 통과)

| 테스트 파일 | 테스트 수 |
|------------|:---:|
| CaptureUseCasesTest | 3 |
| ClassificationUseCasesTest | 12 |
| SyncScheduleToCalendarUseCaseTest | 4 |
| FolderUseCasesTest | 9 |
| CalendarViewModelTest | 11 |
| CaptureViewModelTest | 11 |
| CaptureDetailViewModelTest | 5 |
| HistoryViewModelTest | 15 |
| NotesViewModelTest | 12 |
| NoteDetailViewModelTest | 9 |
| SearchViewModelTest | 6 |
| SettingsViewModelTest | 7 |
| OnboardingViewModelTest | 7 |
| ExampleUnitTest | 1 |
| **합계** | **112** |

### 5.2 테스트 갭 (추가 작성 대상)

| 대상 | 필요 테스트 | 우선순위 |
|------|-----------|:---:|
| `TrashViewModel` | 항목 로드, 복원, 개별 삭제, 전체 비우기, 에러 | **P0** |
| `AIStatusSheetViewModel` | 미확인 목록 로드, 개별 확인, 전체 확인, 분류 변경, 에러 | **P0** |
| `ToggleTodoCompletionUseCase` | 완료 토글 + 이벤트 추적, 이미 완료된 항목 재토글 | **P0** |
| `ReorderTodoUseCase` | 순서 변경 + SortSource.USER 설정 | **P1** |

---

## 6. DB 스키마 현황

현재 DB 버전: **v16** (Destructive Migration)

Phase 2b에서 추가된 필드:
- `captures.parent_capture_id` (String?, 인덱스 포함)
- `notes.body` (String?)

---

## 7. 아키텍처 참고사항

- `UpdateNoteUseCase`가 `CaptureDao`를 직접 주입하여 title 업데이트 — Repository 우회 (기존 설계)
- `TodoWidgetProvider`는 Hilt EntryPoint + runBlocking으로 Room 접근 (위젯 제약)
- 멀티 인텐트 분할은 도메인 로직만 존재, UI에서 자식 캡처 표시는 미구현 (히스토리에서 개별 표시)

---

*Phase 2b 완료 → Phase 3 서버 연동 (Mock → 실제 API 전환)*
