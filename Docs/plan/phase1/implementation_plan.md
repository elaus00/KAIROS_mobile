# Phase 1 (MVP) 구현 계획

## 진행 방식

**서브페이즈별 순차 진행** — 각 서브페이즈 완료 후 컴파일 검증, 필요 시 피드백/수정 후 다음 단계로 진행.

## Context

PRD v10.0의 핵심 가설 검증을 위한 MVP 구현. "사용자가 캡처하면 AI가 자동 분류한다"는 핵심 루프를 완성하는 것이 목표.

현재 코드베이스는 **PRD v4.0 기반**(Obsidian 연동, 멀티모달 캡처, 다른 엔티티 구조)으로 구축되어 있어 **데이터/도메인 레이어 전면 교체**가 필요하다. 프레젠테이션 레이어 스캐폴딩(테마, 네비게이션, HorizontalPager, Hilt DI)은 재사용한다.

## 마이그레이션 전략

**Destructive Migration (v8 → v9)** — 전체 테이블 DROP 후 새 스키마 생성

근거:
- 기존 6개 테이블 → 새 11개 테이블, 컬럼 구조 완전히 다름
- 프리릴리즈 단계, Mock 데이터만 존재
- `fallbackToDestructiveMigration()` 이미 설정됨 (`DatabaseModule.kt:48`)

---

## 서브페이즈 구조 및 의존 관계

```
1-1 (Data: Entity/DAO/DB)
 ↓
1-2 (Domain: Model/Enum/Repository Interface)
 ↓
1-3 (Data: Mapper/RepoImpl/DTO/DI)
 ↓
1-4 (Domain: UseCase)
 ↓
 ├─ 1-5 (WorkManager: SyncQueue, 재분류)
 ├─ 1-6 (UI: Home + AI Status Sheet)  →  1-7 (UI: Full History)
 ├─ 1-8 (UI: Calendar Tab)
 ├─ 1-9 (UI: Notes Tab + Search)
 ├─ 1-10 (UI: Settings + Detail)
 ├─ 1-11 (Share Intent)
 └─ 1-12 (Onboarding)
         ↓
      1-13 (통합 테스트 & 폴리시)
```

---

## Phase 1-1: Data Layer — Entity, DAO, Database

**목표:** Data Model Spec v2.0 기반 Room 스키마 구축

### 삭제 대상
| 파일 | 이유 |
|------|------|
| `entities/CaptureQueueEntity.kt` | → `CaptureEntity.kt`로 대체 |
| `entities/BookmarkEntity.kt` | 별도 테이블 불필요 (Note로 통합) |
| `entities/NotificationEntity.kt` | AI Status Sheet로 대체 |
| `dao/CaptureQueueDao.kt` | → `CaptureDao.kt`로 대체 |
| `dao/BookmarkDao.kt` | 삭제 |
| `dao/NotificationDao.kt` | 삭제 |

### 신규 생성
| 파일 | 설명 |
|------|------|
| `entities/CaptureEntity.kt` | 핵심 테이블 (original_text, classified_type, note_sub_type, confidence, source, is_confirmed, is_deleted 등) |
| `entities/FolderEntity.kt` | 폴더 (system-inbox/ideas/bookmarks + user) |
| `entities/TagEntity.kt` | 태그 (name UNIQUE) |
| `entities/CaptureTagEntity.kt` | 캡처-태그 M:N 관계 |
| `entities/ExtractedEntityEntity.kt` | NER 추출 (PERSON/PLACE/DATE/TIME/AMOUNT) |
| `entities/SyncQueueEntity.kt` | 오프라인 분류 큐 |
| `entities/UserPreferenceEntity.kt` | 설정 (dark_mode 등) |
| `entities/CaptureSearchFts.kt` | FTS4 가상 테이블 |
| `dao/CaptureDao.kt` | 페이징, 미확인 조회, 소프트삭제, FTS 검색 |
| `dao/FolderDao.kt` | CRUD + 시스템 폴더 |
| `dao/TagDao.kt` | 생성/조회 |
| `dao/CaptureTagDao.kt` | 관계 관리 |
| `dao/ExtractedEntityDao.kt` | 캡처별 엔티티 조회 |
| `dao/SyncQueueDao.kt` | 큐 CRUD + 상태 관리 |
| `dao/UserPreferenceDao.kt` | K-V 저장 |
| `dao/CaptureSearchDao.kt` | FTS 검색 쿼리 |

### 수정 대상
| 파일 | 변경 내용 |
|------|-----------|
| `entities/TodoEntity.kt` | 전면 재작성 (capture_id UNIQUE FK, deadline, sort_order 등) |
| `entities/ScheduleEntity.kt` | 전면 재작성 (capture_id UNIQUE FK, start_time, end_time, is_all_day 등) |
| `entities/NoteEntity.kt` | 전면 재작성 (capture_id UNIQUE FK, folder_id FK) |
| `dao/TodoDao.kt` | 전면 재작성 |
| `dao/ScheduleDao.kt` | 전면 재작성 |
| `dao/NoteDao.kt` | 전면 재작성 |
| `KairosDatabase.kt` | v9, 새 엔티티 목록, 마이그레이션 삭제, 시스템 폴더 시딩 Callback |

### 검증
```bash
./gradlew :app:compileDebugKotlin  # data layer 컴파일 확인 (presentation 에러 예상됨)
```

---

## Phase 1-2: Domain Layer — Model, Enum, Repository Interface

**목표:** 새 스펙에 맞는 도메인 모델 정의

### 삭제 대상
| 파일 | 이유 |
|------|------|
| `domain/model/Bookmark.kt` | Note로 통합 |
| `domain/model/Destination.kt` | Obsidian 라우팅 제거 |
| `domain/model/WebMetadata.kt` | Phase 1 불필요 |
| `domain/model/TodoPriority.kt` | 스펙에서 제거됨 |
| `domain/model/TodoMetadata.kt` | 스펙에서 제거됨 |
| `domain/model/Notification.kt` | AI Status Sheet로 대체 |
| `domain/model/SearchQuery.kt` | FTS로 대체 |
| `domain/repository/BookmarkRepository.kt` | 삭제 |
| `domain/repository/NotificationRepository.kt` | 삭제 |

### 수정 대상 (전면 재작성)
| 파일 | 변경 |
|------|------|
| `domain/model/CaptureType.kt` | → `ClassifiedType.kt` (TEMP, TODO, SCHEDULE, NOTES) |
| `domain/model/CaptureSource.kt` | APP, SHARE_INTENT, WIDGET, SPLIT |
| `domain/model/Capture.kt` | original_text 중심, 새 필드 구조 |
| `domain/model/Classification.kt` | AI 응답 매핑 (type, subType, tags, entities) |
| `domain/model/Todo.kt` | capture_id, deadline, is_completed, sort_order |
| `domain/model/Schedule.kt` | capture_id, start/end_time, location, is_all_day |
| `domain/model/Note.kt` | capture_id, folder_id |
| `domain/repository/CaptureRepository.kt` | 새 인터페이스 |
| `domain/repository/TodoRepository.kt` | 단순화 |
| `domain/repository/ScheduleRepository.kt` | 단순화 |
| `domain/repository/NoteRepository.kt` | 단순화 |

### 신규 생성
| 파일 | 설명 |
|------|------|
| `domain/model/NoteSubType.kt` | INBOX, IDEA, BOOKMARK |
| `domain/model/Folder.kt` | 폴더 도메인 모델 |
| `domain/model/FolderType.kt` | INBOX, IDEAS, BOOKMARKS, USER |
| `domain/model/Tag.kt` | 태그 |
| `domain/model/ExtractedEntity.kt` | NER 추출 엔티티 |
| `domain/model/EntityType.kt` | PERSON, PLACE, DATE, TIME, AMOUNT, OTHER |
| `domain/model/SyncQueueItem.kt` | 큐 아이템 |
| `domain/repository/FolderRepository.kt` | 폴더 인터페이스 |
| `domain/repository/TagRepository.kt` | 태그 인터페이스 |
| `domain/repository/SyncQueueRepository.kt` | 큐 인터페이스 |
| `domain/repository/UserPreferenceRepository.kt` | 설정 인터페이스 |

---

## Phase 1-3: Data Layer — Mapper, Repository 구현, DTO, DI

**목표:** Entity ↔ Domain 매핑, Repository 구현, API DTO 갱신

### 주요 작업
- **Mapper 전면 재작성:** CaptureMapper, TodoMapper, ScheduleMapper, NoteMapper, ClassificationMapper
- **Mapper 신규:** FolderMapper, TagMapper, EntityMapper, SyncQueueMapper
- **Repository 전면 재작성:** CaptureRepositoryImpl, TodoRepositoryImpl, ScheduleRepositoryImpl, NoteRepositoryImpl
- **Repository 신규:** FolderRepositoryImpl, TagRepositoryImpl, SyncQueueRepositoryImpl, UserPreferenceRepositoryImpl
- **DTO 재작성:** ClassifyRequest/Response (새 스펙), MockKairosApi (새 응답 형식)
- **API 정리:** /ocr, /stt, /clip, /notes 엔드포인트 제거. /classify, /health만 유지
- **DI 갱신:** DatabaseModule (새 DAO 제공), RepositoryModule (새 바인딩)

### 삭제 대상
| 파일/디렉토리 | 이유 |
|---------------|------|
| `data/repository/BookmarkRepositoryImpl.kt` | 삭제 |
| `data/repository/NotificationRepositoryImpl.kt` | 삭제 |
| `data/mapper/BookmarkMapper.kt` | 삭제 |
| `data/processor/OcrProcessor.kt` | Phase 1 불필요 |
| `data/processor/WebClipper.kt` | Phase 1 불필요 |
| `data/processor/VoiceRecognizer.kt` | Phase 1 불필요 |
| `data/remote/dto/ai/` | 전체 삭제 |
| DTO: NoteCreate*, Ocr*, Stt*, Clip* | 사용 안 함 |

### 검증
```bash
./gradlew :app:compileDebugKotlin  # data + domain 컴파일
```

---

## Phase 1-4: Domain Layer — Use Case

**목표:** 비즈니스 로직 구현

### 캡처 관련
| UseCase | 동작 |
|---------|------|
| `SubmitCaptureUseCase` | 재작성: TEMP 저장 → SyncQueue에 CLASSIFY 등록 |
| `SoftDeleteCaptureUseCase` | 신규: is_deleted=true |
| `UndoDeleteCaptureUseCase` | 신규: is_deleted=false |
| `HardDeleteCaptureUseCase` | 신규: Capture + 파생 엔티티 완전 삭제 |
| `GetAllCapturesUseCase` | 재작성: 페이지네이션 |
| `SaveDraftUseCase` / `GetDraftUseCase` / `DeleteDraftUseCase` | 재작성: EncryptedSharedPreferences |

### 분류 관련 (신규 패키지: `domain/usecase/classification/`)
| UseCase | 동작 |
|---------|------|
| `ProcessClassificationResultUseCase` | AI 결과 → Capture 업데이트 + 파생 엔티티 생성 |
| `ChangeClassificationUseCase` | **@Transaction**: 기존 파생 삭제 → type 변경 → 새 파생 생성 |
| `GetUnconfirmedClassificationsUseCase` | AI Status Sheet용 24시간 미확인 목록 |
| `ConfirmClassificationUseCase` | 개별 확인 |
| `ConfirmAllClassificationsUseCase` | 전체 확인 |

### Todo/Schedule/Note/Folder/Search
- Todo: GetActiveTodos, ToggleCompletion
- Schedule: GetByDate, GetDatesWithSchedules
- Note: GetByFolder, MoveToFolder
- Folder: GetAll, Create, Delete, Rename
- Search: SearchCaptures (FTS 재작성)

### 삭제 대상
| UseCase | 이유 |
|---------|------|
| `SubmitImageCaptureUseCase` | Phase 1 불필요 |
| `SubmitVoiceCaptureUseCase` | Phase 1 불필요 |
| `SubmitWebClipUseCase` | Phase 1 불필요 |
| `domain/usecase/notifications/` 전체 | 삭제 |

---

## Phase 1-5: WorkManager — SyncQueue & 재분류

**목표:** 오프라인 분류 큐 처리 및 TEMP 주기적 재분류

### 신규 생성
| 파일 | 설명 |
|------|------|
| `data/worker/ClassifyCaptureWorker.kt` | SyncQueue PENDING 항목 처리 → API 호출 → 결과 적용 |
| `data/worker/ReclassifyTempWorker.kt` | 15분 주기, TEMP 캡처 배치 재분류 |

### 수정
- `KairosApplication.kt`: ReclassifyTempWorker 등록, MockDataInitializer 업데이트

### 재시도 정책
- CLASSIFY: 최대 3회, 지수 백오프 (5s → 15s → 45s)
- 네트워크 복구 시: 즉시 1회 재시도

---

## Phase 1-6: UI — Home Tab (캡처 + AI Status Sheet)

**목표:** 핵심 캡처 화면 + 분류 현황 바텀시트

### 수정 대상
| 파일 | 변경 |
|------|------|
| `presentation/capture/CaptureContent.kt` | 전면 재작성: 상단바(Kairos + 벨 + 설정), 날짜 표시, 빈 상태, 하단 입력바 |
| `presentation/capture/QuickCaptureViewModel.kt` | → `CaptureViewModel.kt`로 재작성 |

### 신규 생성
| 파일 | 설명 |
|------|------|
| `presentation/capture/CaptureUiState.kt` | inputText, unconfirmedCount, isSubmitting 등 |
| `presentation/classification/AIStatusSheet.kt` | 바텀시트: 미확인 목록, 확인 버튼, 전체 확인 |
| `presentation/classification/AIStatusSheetViewModel.kt` | 미확인 데이터 관리 |
| `presentation/classification/ClassificationDropdown.kt` | 4옵션 드롭다운 (일정/할 일/노트/아이디어) |

### 삭제 대상
| 파일 | 이유 |
|------|------|
| `presentation/capture/QuickCaptureOverlay.kt` | Phase 1 불필요 |
| `presentation/capture/QuickCapturePopup.kt` | Phase 1 불필요 |
| `presentation/notifications/` 전체 | AI Status Sheet로 대체 |

### 검증
텍스트 입력 → 저장 → TEMP → Mock 분류 → 벨 뱃지 업데이트 → Status Sheet에 항목 표시

---

## Phase 1-7: UI — Full History

**목표:** 전체 기록 화면 (무한 스크롤)

### 신규 생성
| 파일 | 설명 |
|------|------|
| `presentation/history/HistoryScreen.kt` | 역시간순, 20개씩 페이지네이션 |
| `presentation/history/HistoryViewModel.kt` | 페이징 + 삭제 |
| `presentation/history/HistoryUiState.kt` | 상태 |
| `presentation/history/components/HistoryItem.kt` | ai_title + 시간 + 미리보기 + 분류 칩 + 태그 |

### 수정
- `NavGraph.kt`: `HISTORY` 라우트 추가

---

## Phase 1-8: UI — Calendar Tab

**목표:** 주간 스트립 + 일정 타임라인 + 투두 리스트

### 수정 대상 (전면 재작성)
- `presentation/calendar/` 내 ViewModel, UiState, Screen
- `presentation/calendar/components/` 내 WeekPicker, ScheduleTimeline, TaskList

### 핵심 구현
- 주간 스트립: 이벤트 도트, 날짜 선택, 좌우 스와이프
- 일정 섹션: 타임라인 형식 (시간 + 도트 + 카드)
- 투두 섹션: 체크박스, 마감일 표시, 완료 시 슬라이드아웃

---

## Phase 1-9: UI — Notes Tab + Search

**목표:** 폴더 구조 노트 화면 + FTS 검색

### 수정 대상 (전면 재작성)
- `presentation/notes/NotesScreen.kt`, `NotesViewModel.kt`, `NotesUiState.kt`
- `presentation/search/SearchScreen.kt`, `SearchViewModel.kt`, `SearchUiState.kt`

### 신규 생성
| 파일 | 설명 |
|------|------|
| `presentation/notes/components/FolderItem.kt` | 폴더 행 (이름 + 노트 수) |
| `presentation/notes/components/FolderNoteList.kt` | 폴더 내 노트 목록 |
| `presentation/notes/CreateFolderDialog.kt` | 폴더 생성 다이얼로그 |
| `presentation/notes/RenameFolderDialog.kt` | 폴더 이름 변경 |

### 핵심 구현
- Inbox: 노트가 있을 때만 표시
- Ideas/Bookmarks: 항상 표시
- User 폴더: 생성순
- FTS 검색: ai_title > tags > entities > original_text 우선순위

---

## Phase 1-10: UI — Settings + Detail

**목표:** 설정 단순화, 상세 화면

### 수정 대상
- `presentation/settings/SettingsScreen.kt`: Obsidian/스마트 처리/프로필 제거. 다크모드 3옵션 + 개인정보처리방침 + 이용약관 + 앱 버전만
- `presentation/settings/SettingsViewModel.kt`: 단순화

### 삭제 대상
- `presentation/settings/ProfileScreen.kt`: Phase 1 불필요
- `presentation/result/` 전체: 새 Detail 화면으로 대체

### 신규 생성 (최소한)
- 캡처 상세 화면은 Phase 1에서는 분류 칩 변경 + 원문 확인 수준으로 최소 구현

---

## Phase 1-11: Share Intent

**목표:** 외부 앱에서 텍스트 공유 수신

### 수정 대상
- `AndroidManifest.xml`: `<intent-filter>` ACTION_SEND + text/plain
- `MainActivity.kt`: EXTRA_TEXT 추출 → 캡처 저장 → 토스트

### 검증
다른 앱에서 텍스트 공유 → Kairos 선택 → 저장 확인 → 토스트 "캡처 완료"

---

## Phase 1-12: Onboarding

**목표:** 첫 실행 시 3화면 온보딩

### 신규 생성
| 파일 | 설명 |
|------|------|
| `presentation/onboarding/OnboardingScreen.kt` | HorizontalPager 3페이지 |
| `presentation/onboarding/OnboardingViewModel.kt` | 완료 상태 관리 (UserPreference) |

### 수정
- `NavGraph.kt`: 온보딩 라우트 + 첫 실행 판단 로직

---

## Phase 1-13: 통합 테스트 & 폴리시

### 테스트 시나리오
1. **캡처 플로우:** 텍스트 입력 → TEMP 저장(<1초) → 분류 → 파생 엔티티 생성
2. **삭제 플로우:** 소프트 삭제 → Snackbar 3초 → 하드 삭제 / 실행 취소
3. **분류 변경:** TODO → SCHEDULE 트랜잭션 (기존 Todo 삭제 + Schedule 생성)
4. **오프라인:** 비행기 모드 → 캡처 → 네트워크 복구 → 분류
5. **Share Intent:** 외부 텍스트 → 캡처 저장

### 검증 명령
```bash
./gradlew testDebugUnitTest
./gradlew :app:compileDebugKotlin
./gradlew assembleDebug
```

---

## 리스크 영역

| 리스크 | 심각도 | 대응 |
|--------|--------|------|
| 분류 변경 트랜잭션 원자성 | HIGH | Room `@Transaction` + 통합 테스트 |
| FTS 인덱스 동기화 | MEDIUM | 분류 결과 적용 시 FTS 테이블 동시 업데이트 |
| 하드 삭제 타이밍 (3초) | MEDIUM | `viewModelScope` + delay 사용 (WorkManager 아닌) |
| SyncQueue 재시도 엣지케이스 | MEDIUM | PROCESSING 상태 앱 킬 → 앱 시작 시 PENDING으로 리셋 |
| Mock API ↔ 실제 API 불일치 | MEDIUM | MockKairosApi를 새 스펙에 정확히 맞춰 업데이트 |

---

## 보존 대상 (변경 없음 또는 최소 수정)

- `ui/theme/` (Color, Theme, Type)
- `presentation/components/common/KairosBottomNav.kt`
- `presentation/main/MainScreen.kt` (참조 업데이트만)
- `domain/model/Result.kt`
- `domain/model/ThemePreference.kt`
- `di/AppModule.kt`
- 빌드 설정 (`build.gradle.kts`)
