# 테스트 계획 템플릿: ViewModel & Worker

> Phase 1에서 사용한 테스트 계획 문서를 템플릿으로 전환한 버전. Phase 2 작업 시 범위/케이스를 현재 스펙에 맞게 갱신해서 사용한다.
>
> **Version**: template-1.0
> **작성일**: 2026-02-07
> **기준**: 기능명세서, 데이터모델 명세서, 해당 Phase 구현 계획
> **범위**: ViewModel 8개 + Worker 2개

---

## 개요

### 목표
Phase 1 MVP의 Presentation/Worker 레이어 테스트 커버리지 확보. UseCase 테스트(100% 완료)와 함께 핵심 비즈니스 로직의 양 끝단을 검증한다.

### 테스트 전략
- **ViewModel**: UseCase/Repository를 MockK로 대체, StateFlow/SharedFlow 출력을 Turbine으로 검증
- **Worker**: Repository/API를 MockK로 대체, doWork() 결과와 부수효과 검증
- **기존 컨벤션 준수**: `runTest`, `MainDispatcherRule`, `coEvery`/`coVerify`, `@After unmockkAll()`

### 테스트 인프라 (이미 존재)
```
app/src/test/java/com/example/kairos_mobile/util/MainDispatcherRule.kt
```

### 추가 필요 의존성
```kotlin
// app/build.gradle.kts — 이미 존재하는지 확인 필요
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7+")
testImplementation("io.mockk:mockk:1.13+")
testImplementation("app.cash.turbine:turbine:1.0+")
testImplementation("androidx.work:work-testing:2.9+")  // Worker 테스트용
```

---

## 파일 구조

```
app/src/test/java/com/example/kairos_mobile/
├── util/
│   └── MainDispatcherRule.kt          # 기존
├── domain/usecase/                    # 기존 UseCase 테스트들
├── presentation/
│   ├── capture/CaptureViewModelTest.kt
│   ├── calendar/CalendarViewModelTest.kt
│   ├── notes/NotesViewModelTest.kt
│   ├── history/HistoryViewModelTest.kt
│   ├── search/SearchViewModelTest.kt
│   ├── detail/CaptureDetailViewModelTest.kt
│   ├── settings/SettingsViewModelTest.kt
│   └── onboarding/OnboardingViewModelTest.kt
└── data/worker/
    ├── ClassifyCaptureWorkerTest.kt
    └── ReclassifyTempWorkerTest.kt
```

---

## 1. CaptureViewModelTest

**의존성 Mock**: `SubmitCaptureUseCase`, `SaveDraftUseCase`, `GetDraftUseCase`, `DeleteDraftUseCase`, `CaptureRepository`

### 테스트 케이스 (11개)

#### 초기화
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_draft_into_input_text` | getDraftUseCase가 "임시 텍스트" 반환 → uiState.inputText == "임시 텍스트", characterCount 갱신 |
| 2 | `init_observes_unconfirmed_count_from_repository` | captureRepository.getUnconfirmedCount()가 Flow(3) 방출 → uiState.unconfirmedCount == 3 |

#### 입력
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 3 | `updateInput_updates_text_and_character_count` | updateInput("안녕") → inputText == "안녕", characterCount == 2 |
| 4 | `updateInput_enforces_max_character_limit` | MAX_LENGTH 초과 입력 → inputText가 MAX_LENGTH로 잘림 |

#### 제출
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 5 | `submit_rejects_blank_input` | inputText가 공백 → submit() 호출 → submitCaptureUseCase 호출 안 됨, errorMessage 설정 |
| 6 | `submit_success_clears_input_and_emits_event` | submitCaptureUseCase 정상 → inputText == "", deleteDraftUseCase 호출, events에 SubmitSuccess 방출 |
| 7 | `submit_sets_isSubmitting_during_execution` | submit() 실행 중 isSubmitting == true → 완료 후 false |
| 8 | `submit_handles_exception_with_error_message` | submitCaptureUseCase가 예외 → errorMessage 설정, isSubmitting == false |

#### 드래프트
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 9 | `saveDraft_saves_current_text` | inputText 설정 후 saveDraft() → saveDraftUseCase 호출 확인 |
| 10 | `saveDraft_skips_blank_text` | inputText가 공백 → saveDraft() → saveDraftUseCase 호출 안 됨 |

#### AI 상태 시트
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 11 | `toggleStatusSheet_toggles_visibility` | toggleStatusSheet() → isStatusSheetVisible == true → 다시 호출 → false |

---

## 2. CalendarViewModelTest

**의존성 Mock**: `GetSchedulesByDateUseCase`, `GetDatesWithSchedulesUseCase`, `GetActiveTodosUseCase`, `ToggleTodoCompletionUseCase`, `SoftDeleteCaptureUseCase`, `UndoDeleteCaptureUseCase`, `CaptureRepository`

### 테스트 케이스 (12개)

#### 초기화
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_today_schedules_and_todos` | 초기화 시 오늘 날짜의 일정 + 할 일 로드 → uiState.schedules, tasks 채워짐 |
| 2 | `init_loads_dates_with_schedules_for_dots` | datesWithSchedules에 마커 날짜들 포함 |

#### 날짜 선택
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 3 | `selectDate_updates_selected_and_reloads_schedules` | SelectDate 이벤트 → selectedDate 변경, 해당 날짜 일정 로드 |
| 4 | `changeMonth_updates_current_month` | ChangeMonth(+1) → currentMonth 다음 달로 변경 |

#### 일정/투두
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 5 | `schedules_sorted_by_start_time` | 여러 일정 반환 → startTime 순으로 정렬 확인 |
| 6 | `schedule_without_matching_capture_uses_empty_title` | Capture 못 찾음 → 일정 제목 빈 문자열 |
| 7 | `toggleTaskComplete_calls_usecase_and_reloads` | ToggleTaskComplete → toggleTodoCompletion 호출, 투두 목록 리로드 |

#### 삭제/실행취소
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 8 | `deleteCaptureId_soft_deletes_and_emits_event` | DeleteSchedule/DeleteTask → softDeleteCaptureUseCase 호출, DeleteSuccess 이벤트 방출 |
| 9 | `undoDelete_restores_and_emits_event` | undoDelete() → undoDeleteCaptureUseCase 호출, UndoSuccess 이벤트, 목록 리로드 |

#### 에러 처리
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 10 | `schedule_loading_error_sets_error_state` | 일정 로드 예외 → uiState.error 설정 |
| 11 | `todo_loading_error_caught_silently` | 투두 로드 예외 → 에러가 UI에 노출 안 됨 (비핵심 스트림) |

#### 월 확장
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 12 | `toggleMonthExpand_flips_state` | ToggleMonthExpand → isMonthExpanded 토글 |

---

## 3. NotesViewModelTest

**의존성 Mock**: `GetAllFoldersUseCase`, `CreateFolderUseCase`, `DeleteFolderUseCase`, `RenameFolderUseCase`, `NoteRepository`

### 테스트 케이스 (11개)

#### 초기화 & 폴더 로딩
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_folders_with_note_counts` | 초기화 시 폴더 목록 + 각 폴더 노트 수 로드 |
| 2 | `inbox_hidden_when_note_count_is_zero` | Inbox noteCount == 0 → 폴더 목록에서 제외 |
| 3 | `inbox_shown_when_note_count_is_positive` | Inbox noteCount > 0 → 폴더 목록에 포함 |
| 4 | `ideas_and_bookmarks_always_visible` | noteCount == 0이어도 Ideas, Bookmarks 항상 표시 |

#### 폴더 탐색
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 5 | `selectFolder_loads_notes_for_folder` | SelectFolder → selectedFolder 설정, 해당 폴더 노트 로드 |
| 6 | `backToFolders_clears_selection_and_notes` | BackToFolders → selectedFolder null, notes 빈 리스트 |

#### CRUD
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 7 | `createFolder_success_reloads_folders` | CreateFolder 이벤트 → createFolderUseCase 호출, 폴더 목록 리로드 |
| 8 | `createFolder_failure_sets_error` | createFolderUseCase 예외 → errorMessage 설정 |
| 9 | `renameFolder_success_reloads_folders` | RenameFolder → renameFolderUseCase 호출, 리로드 |
| 10 | `deleteFolder_delegates_and_reloads` | DeleteFolder → deleteFolderUseCase 호출, 리로드 |

#### 다이얼로그
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 11 | `dialog_state_management` | ShowCreateDialog → 상태 true, DismissDialog → false |

---

## 4. HistoryViewModelTest

**의존성 Mock**: `GetAllCapturesUseCase`, `SoftDeleteCaptureUseCase`, `HardDeleteCaptureUseCase`, `UndoDeleteCaptureUseCase`, `ChangeClassificationUseCase`

### 테스트 케이스 (11개)

#### 페이지네이션
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_first_page` | 초기화 시 page 0, PAGE_SIZE(20) 로드 |
| 2 | `loadMore_appends_next_page` | loadMore() → page 1 로드, 기존 목록에 추가 |
| 3 | `loadMore_sets_hasMore_false_when_last_page_small` | 마지막 페이지 < PAGE_SIZE → hasMore == false |
| 4 | `loadMore_prevents_duplicate_requests` | 이미 로딩 중 → 중복 loadMore() 무시 |
| 5 | `captures_merged_distinct_by_id` | 중복 ID 필터링 확인 |

#### 삭제
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 6 | `deleteCaptureById_soft_deletes_and_emits_event` | softDeleteCaptureUseCase 호출, DeleteSuccess 이벤트 |
| 7 | `deleteCaptureById_schedules_hard_delete_after_3_seconds` | 3초 후 hardDeleteCaptureUseCase 호출 |
| 8 | `undoDelete_cancels_hard_delete_and_restores` | undoDelete() → 스케줄된 하드삭제 취소, undoDeleteCaptureUseCase 호출, 목록 리로드 |

#### 분류 변경
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 9 | `changeClassification_delegates_and_reloads` | changeClassification() → changeClassificationUseCase 호출, 목록 리로드 |
| 10 | `changeClassification_error_sets_error_message` | 예외 → errorMessage 설정 |

#### 정리
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 11 | `onCleared_executes_pending_hard_deletes` | ViewModel 정리 시 미완료 하드삭제 즉시 실행 (runBlocking) |

---

## 5. SearchViewModelTest

**의존성 Mock**: `SearchCapturesUseCase`

### 테스트 케이스 (5개)

| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `initial_state_is_empty` | 초기 상태: searchText == "", results 빈 리스트, hasSearched == false |
| 2 | `search_with_text_returns_results_after_debounce` | "검색어" 입력 → 300ms 후 검색 실행 → 결과 반영 |
| 3 | `blank_text_clears_results_and_resets_hasSearched` | 빈 문자열 입력 → results 빈 리스트, hasSearched == false |
| 4 | `rapid_typing_debounces_search` | 빠른 연속 입력 → 마지막 입력만 검색 실행 |
| 5 | `search_error_sets_error_message` | searchCapturesUseCase 에러 → errorMessage 설정 |

---

## 6. CaptureDetailViewModelTest

**의존성 Mock**: `SavedStateHandle`, `CaptureRepository`, `ChangeClassificationUseCase`

### 테스트 케이스 (5개)

| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_capture_by_id_from_saved_state` | SavedStateHandle에 captureId → captureRepository.getCaptureById() 호출, uiState 채워짐 |
| 2 | `capture_not_found_sets_error` | getCaptureById() null 반환 → errorMessage 설정 |
| 3 | `changeClassification_success_updates_local_state` | onChangeClassification(SCHEDULE) → changeClassificationUseCase 호출, uiState.classifiedType 갱신 |
| 4 | `changeClassification_failure_sets_error` | 예외 → errorMessage 설정 |
| 5 | `dismissError_clears_error_message` | dismissError() → errorMessage == null |

---

## 7. SettingsViewModelTest

**의존성 Mock**: `GetThemePreferenceUseCase`, `SetThemePreferenceUseCase`

### 테스트 케이스 (3개)

| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `init_loads_theme_preference` | getThemePreferenceUseCase Flow(DARK) → uiState.themePreference == DARK |
| 2 | `setTheme_delegates_to_usecase` | setTheme(LIGHT) → setThemePreferenceUseCase(LIGHT) 호출 |
| 3 | `setTheme_error_sets_error_message` | 예외 → errorMessage 설정 |

---

## 8. OnboardingViewModelTest

**의존성 Mock**: `UserPreferenceRepository`, `SubmitCaptureUseCase`

### 테스트 케이스 (6개)

| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `updateInput_updates_text` | updateInput("테스트") → uiState.inputText == "테스트" |
| 2 | `completeOnboarding_submits_capture_and_navigates` | 텍스트 입력 → completeOnboarding() → submitCaptureUseCase 호출, onboarding_completed 설정, NavigateToHome 이벤트 |
| 3 | `completeOnboarding_blank_text_skips_capture` | 빈 텍스트 → completeOnboarding() → submitCaptureUseCase 호출 안 됨, 온보딩 완료 처리 |
| 4 | `completeOnboarding_capture_failure_still_completes` | submitCaptureUseCase 예외 → 온보딩은 여전히 완료됨 (NavigateToHome 방출) |
| 5 | `skip_completes_without_capture` | skip() → submitCaptureUseCase 호출 안 됨, onboarding_completed 설정, NavigateToHome 이벤트 |
| 6 | `isSubmitting_true_during_completion` | completeOnboarding() 실행 중 isSubmitting == true → 완료 후 false |

---

## 9. ClassifyCaptureWorkerTest

**의존성 Mock**: `SyncQueueRepository`, `CaptureRepository`, `KairosApi`, `ClassificationMapper`, `ProcessClassificationResultUseCase`

> Worker 테스트는 `TestListenableWorkerBuilder` (work-testing) 또는 직접 doWork() 호출 방식 사용.
> `@AssistedInject` 때문에 Worker 인스턴스를 직접 생성하거나 TestWorkerBuilder를 활용한다.

### 테스트 케이스 (9개)

#### 정상 플로우
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `doWork_processes_pending_items_and_returns_success` | PENDING 아이템 2개 → 각각 API 호출 + processClassificationResult + COMPLETED 처리 → Result.success() |
| 2 | `doWork_no_pending_items_returns_success` | PENDING 없음 → API 호출 없음, Result.success() |
| 3 | `doWork_updates_status_to_processing_before_api_call` | API 호출 전 status → PROCESSING 확인 |

#### 에러 처리
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 4 | `doWork_capture_not_found_marks_completed` | captureRepository.getCaptureById() null → 해당 아이템 COMPLETED (스킵), 에러 없이 계속 |
| 5 | `doWork_api_failure_triggers_retry_with_backoff` | API 예외 → handleRetry() 호출, retryCount 증가, nextRetryAt 설정 |
| 6 | `doWork_max_retries_exceeded_marks_failed` | retryCount >= MAX_RETRY → status FAILED |

#### 재시도 로직
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 7 | `retry_backoff_5s_15s_45s` | retry 0 → 5초, retry 1 → 15초, retry 2 → 45초 (3x 지수 백오프) |

#### 완료 정리
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 8 | `completed_items_deleted_after_processing` | COMPLETED 상태 아이템들 syncQueueRepository에서 삭제 확인 |

#### 부분 실패
| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 9 | `partial_failure_continues_processing_remaining` | 첫 번째 아이템 실패 → 두 번째 아이템 정상 처리 (전체 중단 안 됨) |

---

## 10. ReclassifyTempWorkerTest

**의존성 Mock**: `CaptureRepository`, `SyncQueueRepository`

### 테스트 케이스 (5개)

| # | 테스트명 | 검증 내용 |
|---|---------|----------|
| 1 | `doWork_enqueues_classify_for_all_temp_captures` | TEMP 캡처 3개 → SyncQueue에 CLASSIFY 아이템 3개 등록 |
| 2 | `doWork_no_temp_captures_does_nothing` | TEMP 없음 → SyncQueue 추가 없음, ClassifyCaptureWorker 미호출 |
| 3 | `doWork_triggers_classify_worker_after_enqueue` | 아이템 등록 후 ClassifyCaptureWorker.enqueue() 호출 확인 |
| 4 | `doWork_individual_enqueue_failure_continues` | 하나 실패 → 나머지 계속 처리, Result.success() 반환 |
| 5 | `doWork_returns_success_regardless_of_outcome` | 성공/실패 무관하게 항상 Result.success() |

---

## 구현 순서

순서는 의존성과 복잡도 기반:

```
Step 1: SettingsViewModelTest (3개, 가장 단순 → 테스트 인프라 검증)
  → verify: 컴파일 + 테스트 통과

Step 2: SearchViewModelTest (5개, 디바운스 로직)
  → verify: 컴파일 + 테스트 통과

Step 3: CaptureDetailViewModelTest (5개, SavedStateHandle 패턴)
  → verify: 컴파일 + 테스트 통과

Step 4: OnboardingViewModelTest (6개)
  → verify: 컴파일 + 테스트 통과

Step 5: CaptureViewModelTest (11개, 핵심 캡처 로직)
  → verify: 컴파일 + 테스트 통과

Step 6: NotesViewModelTest (11개, Inbox 필터링 로직)
  → verify: 컴파일 + 테스트 통과

Step 7: CalendarViewModelTest (12개, 가장 복잡한 상태 관리)
  → verify: 컴파일 + 테스트 통과

Step 8: HistoryViewModelTest (11개, 페이지네이션 + 지연 삭제)
  → verify: 컴파일 + 테스트 통과

Step 9: ClassifyCaptureWorkerTest (9개, 핵심 분류 파이프라인)
  → verify: 컴파일 + 테스트 통과

Step 10: ReclassifyTempWorkerTest (5개)
  → verify: 컴파일 + 전체 테스트 통과
```

**총 테스트 케이스: 79개** (ViewModel 64개 + Worker 15개)

---

## 검증 명령

```bash
# 각 Step 완료 후
./gradlew testDebugUnitTest --tests "com.example.kairos_mobile.presentation.*"
./gradlew testDebugUnitTest --tests "com.example.kairos_mobile.data.worker.*"

# 전체
./gradlew testDebugUnitTest
```

---

## 리스크

| 리스크 | 심각도 | 대응 |
|--------|--------|------|
| Worker `@AssistedInject` 테스트 셋업 | MEDIUM | TestListenableWorkerBuilder 또는 직접 인스턴스 생성 |
| HistoryViewModel `onCleared` + `runBlocking` 테스트 | MEDIUM | ReflectionUtil 또는 ViewModel.clear() 호출 |
| CalendarViewModel 날짜 변환 (epochDay) | LOW | 고정 날짜 사용 |
| SearchViewModel 디바운스 타이밍 | LOW | `advanceTimeBy(300)` 사용 |

---

*Document Version: 1.0 | Created: 2026-02-07*
