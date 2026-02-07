# Phase 1 (MVP) 진행 보고서

> 최종 업데이트: 2026-02-07

## 구현 완료 (코드 작성 완료, 컴파일 미확인)

### Data Layer
| 항목 | 파일 수 | 상태 |
|------|--------|------|
| Entity (11개) | CaptureEntity, TodoEntity, ScheduleEntity, NoteEntity, FolderEntity, TagEntity, CaptureTagEntity, ExtractedEntityEntity, SyncQueueEntity, UserPreferenceEntity, CaptureSearchFts | 완료 |
| DAO (11개) | 위 Entity 각각에 대응 | 완료 |
| KairosDatabase v9 | Destructive Migration, 시스템 폴더 시딩 | 완료 |
| Mapper (9개) | Capture, Todo, Schedule, Note, Classification, Folder, Tag, Entity, SyncQueue | 완료 |
| Repository 구현 (8개) | Capture, Todo, Schedule, Note, Folder, Tag, SyncQueue, UserPreference | 완료 |
| API/DTO | /classify, /health만 유지. ClassifyRequest/Response 재작성 | 완료 |
| DI | DatabaseModule (11 DAO), RepositoryModule (8 바인딩) | 완료 |
| Worker (2개) | ClassifyCaptureWorker, ReclassifyTempWorker | 완료 |
| MockDataInitializer | 새 스키마에 맞게 재작성 | 완료 |

### Domain Layer
| 항목 | 파일 수 | 상태 |
|------|--------|------|
| 모델/Enum | Capture, ClassifiedType, NoteSubType, FolderType, EntityType 등 15개 | 완료 |
| Repository Interface (8개) | Capture, Todo, Schedule, Note, Folder, Tag, SyncQueue, UserPreference | 완료 |
| UseCase (26개) | capture(8), classification(5), todo(2), schedule(2), note(2), folder(4), search(1), settings(2) | 완료 |

### Presentation Layer
| 항목 | 상태 | 비고 |
|------|------|------|
| Home Tab (CaptureContent) | 완료 | 상단바, 빈 상태, 하단 입력바 |
| AI Status Sheet | 완료 | 미확인 분류 바텀시트 + 드롭다운 |
| Full History | 완료 | 무한 스크롤 + 스와이프 삭제 + Snackbar 실행 취소 |
| Calendar Tab | 완료 | 주간 스트립 + 타임라인 + 투두 |
| Notes Tab | 완료 | 폴더 구조 (Inbox/Ideas/Bookmarks/User) + CRUD |
| Search | 완료 | FTS 검색, 300ms 디바운싱 |
| Settings | 완료 | 다크모드 + 개인정보처리방침 + 이용약관 + 앱 버전 |
| Capture Detail | 완료 | 분류 칩 변경 + 원문 확인 |
| Onboarding | 완료 | 3화면 HorizontalPager + 첫 캡처 저장 |
| Share Intent | 완료 | ACTION_SEND text/plain |
| NavGraph | 완료 | ONBOARDING, HOME, HISTORY, DETAIL 라우트 |
| MainActivity | 완료 | 온보딩 분기 + Share Intent 처리 |

### QA 리뷰 결과
| 리뷰 | 발견 이슈 | 수정 여부 |
|------|----------|----------|
| Phase 1-1 + 1-2 | LOW 3건 | 대부분 UseCase 레이어에서 처리 (의도적 유보) |
| Phase 1-3 | HIGH 1건 (트랜잭션 누락), MEDIUM 2건 | 전부 수정 완료 |

---

## 미완료 항목

### 컴파일 통과 미확인
- 전체 `./gradlew :app:compileDebugKotlin` 통과 확인 필요
- KSP 캐시 문제 해결 후 재빌드 필요 (`clean` 완료)

### Phase 1-13: 통합 테스트 & 폴리시
아래 5개 시나리오 검증 미완:
1. 캡처 플로우: 텍스트 입력 → TEMP → 분류 → 파생 엔티티
2. 삭제 플로우: 소프트 → Snackbar 3초 → 하드 / 실행 취소
3. 분류 변경: TODO → SCHEDULE 트랜잭션
4. 오프라인: 비행기 모드 → 캡처 → 복구 → 분류
5. Share Intent: 외부 텍스트 → 캡처 저장

### 레거시 잔존 파일
| 파일 | 상태 | 사유 |
|------|------|------|
| `entities/BookmarkEntity.kt` | 존재 | DB에서는 제거됨, 파일 삭제 필요 |
| `dao/BookmarkDao.kt` | 존재 | 위와 동일 |
| `dao/NotificationDao.kt` | 존재 | 위와 동일 |
| `entities/NotificationEntity.kt` | 존재 | 위와 동일 |
| `data/local/database/DatabaseMigrations.kt` | 존재 | 구 마이그레이션, 삭제 필요 |

---

## 테스트 현황

### Unit Test (작성 완료)
| 파일 | 범위 |
|------|------|
| `CaptureUseCasesTest.kt` | SubmitCapture, SoftDelete, UndoDelete, HardDelete, GetAllCaptures |
| `ClassificationUseCasesTest.kt` | ProcessResult, ChangeClassification, Confirm, ConfirmAll, GetUnconfirmed |
| `FolderUseCasesTest.kt` | GetAll, Create, Delete, Rename |
| `DelegatingUseCasesTest.kt` | GetActiveTodos, ToggleCompletion, SearchCaptures, 기타 위임 UseCase |

### 테스트 실행 결과
- `./gradlew testDebugUnitTest` — 미실행 (컴파일 에러 해결 후 실행 필요)

### 테스트 미작성 영역
- DAO 통합 테스트 (Room in-memory DB)
- ViewModel 테스트
- Mapper 테스트
- Worker 테스트
- UI 테스트 (Compose)
