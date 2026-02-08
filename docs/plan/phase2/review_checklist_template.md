# 코드 리뷰 체크리스트 템플릿

> Phase 1 리뷰 체크리스트를 템플릿으로 전환한 버전. Phase 2 이후에는 해당 Phase 스펙에 맞는 항목으로 수정해서 사용한다.
> 기준 문서: 데이터 모델 명세서, 기능명세서, PRD 최신 버전

---

## 1. 공통 (모든 레이어)

### 1.1 코드 컨벤션
- [ ] 주석이 한글로 작성되었는가
- [ ] 불필요한 import가 남아있지 않은가
- [ ] 삭제된 레거시 코드에 대한 참조가 남아있지 않은가
- [ ] API 키, 시크릿 등이 하드코딩되어 있지 않은가
- [ ] 네이밍 컨벤션 준수 (클래스: PascalCase, 함수/변수: camelCase, 상수: UPPER_SNAKE_CASE)
- [ ] Entity 클래스명: `~Entity`, DAO 클래스명: `~Dao`, Domain Model: Entity 접미사 없음

### 1.2 아키텍처 준수
- [ ] 단방향 의존 (presentation → domain → data) 위반이 없는가
- [ ] Domain 레이어에 Android 프레임워크 의존성이 없는가 (순수 Kotlin)
- [ ] DTO는 `data/remote/dto/`에, Entity는 `data/local/database/entities/`에 위치하는가

---

## 2. Data Layer — Entity, DAO, Database (Phase 1-1)

### 2.1 Entity 스펙 일치
- [ ] **CaptureEntity**: 필수 필드 (id, original_text, classified_type, source, is_confirmed, is_deleted, created_at, updated_at) 존재
- [ ] **CaptureEntity**: nullable 필드 (ai_title, note_sub_type, confidence, confirmed_at, deleted_at, draft_text, classification_completed_at) 정확
- [ ] **CaptureEntity**: Phase 2a 필드 (is_trashed, trashed_at, image_uri, parent_capture_id)는 Phase 1에서 미포함 또는 기본값 처리
- [ ] **TodoEntity**: capture_id UNIQUE FK, deadline, is_completed(기본 false), completed_at, sort_order, created_at, updated_at
- [ ] **ScheduleEntity**: capture_id UNIQUE FK, start_time, end_time, location, is_all_day(기본 false), confidence, created_at, updated_at
- [ ] **ScheduleEntity**: calendar_sync_status Phase 1 기본값 NOT_LINKED
- [ ] **NoteEntity**: capture_id UNIQUE FK, folder_id FK, created_at, updated_at
- [ ] **FolderEntity**: id, name, type(Enum), sort_order, created_at
- [ ] **TagEntity**: id, name(UNIQUE), created_at
- [ ] **CaptureTagEntity**: 복합 PK (capture_id, tag_id)
- [ ] **ExtractedEntityEntity**: id, capture_id, type(Enum), value, normalized_value
- [ ] **SyncQueueEntity**: id, action(Enum), payload, retry_count(기본 0), max_retries(기본 3), status(Enum), created_at, next_retry_at
- [ ] **UserPreferenceEntity**: key(PK), value

### 2.2 Enum 값 정확성
- [ ] classified_type: TEMP, SCHEDULE, TODO, NOTES (4개)
- [ ] note_sub_type: INBOX, IDEA, BOOKMARK, USER_FOLDER (4개)
- [ ] confidence: HIGH, MEDIUM, LOW (3개)
- [ ] source: APP, SHARE_INTENT, WIDGET, SPLIT (4개)
- [ ] FolderType: INBOX, IDEAS, BOOKMARKS, AI_GROUP, USER (5개)
- [ ] EntityType: PERSON, PLACE, DATE, TIME, AMOUNT, OTHER (6개)
- [ ] SyncQueue action: CLASSIFY, RECLASSIFY, CALENDAR_CREATE, CALENDAR_DELETE, ANALYTICS_BATCH
- [ ] SyncQueue status: PENDING, PROCESSING, COMPLETED, FAILED

### 2.3 Room 어노테이션
- [ ] `@Entity` tableName 지정
- [ ] `@PrimaryKey` 올바른 필드에 지정
- [ ] FK 관계 `@ForeignKey` 올바르게 설정 (CASCADE 등 onDelete 정책 확인)
- [ ] 인덱스 `@Index` 스펙 문서 기준 적용:
  - Capture: classified_type, note_sub_type, created_at, is_deleted, is_confirmed
  - Todo: capture_id(UNIQUE), is_completed, deadline, sort_order
  - Schedule: capture_id(UNIQUE), start_time
  - Note: capture_id(UNIQUE), folder_id
  - Entity: capture_id, type
  - SyncQueue: status, next_retry_at
  - Tag: name(UNIQUE)
- [ ] TypeConverter 누락 없는가 (Enum ↔ String 등)

### 2.4 DAO
- [ ] 각 DAO에 기본 CRUD 작업 존재
- [ ] CaptureDao: 페이징 쿼리, 미확인 조회(is_confirmed=false), 소프트삭제, FTS 검색
- [ ] FolderDao: 시스템 폴더 조회
- [ ] SyncQueueDao: 상태별 조회 + PENDING 항목 처리
- [ ] `@Transaction` 필요한 곳에 적용 (분류 변경 등)
- [ ] `suspend` 키워드 적절 사용 (Room 비동기)
- [ ] `Flow` 반환 타입 적절 사용 (실시간 관찰 필요 쿼리)

### 2.5 Database
- [ ] KairosDatabase 버전 올바른가 (v9 또는 새 시작)
- [ ] 모든 신규 Entity가 `@Database(entities = [...])` 에 등록
- [ ] 시스템 폴더 시딩 Callback 존재 (system-inbox, system-ideas, system-bookmarks)
- [ ] FTS 가상 테이블 (CaptureSearchFts) 설정

---

## 3. Domain Layer — Model, Enum, Repository Interface (Phase 1-2)

### 3.1 Domain Model 스펙 일치
- [ ] Capture: original_text 중심, classified_type, note_sub_type, confidence, source, is_confirmed 등
- [ ] Todo: capture_id, deadline, is_completed, sort_order
- [ ] Schedule: capture_id, start_time, end_time, location, is_all_day, confidence
- [ ] Note: capture_id, folder_id
- [ ] Folder: id, name, type, sort_order
- [ ] Tag: id, name
- [ ] ExtractedEntity: id, capture_id, type, value, normalized_value
- [ ] SyncQueueItem: 큐 아이템 모델

### 3.2 순수 Kotlin 확인
- [ ] `android.*`, `androidx.*` import 없는가
- [ ] Room 어노테이션 없는가 (`@Entity`, `@Dao` 등)
- [ ] Hilt 어노테이션 없는가 (`@Inject` 등)

### 3.3 Repository Interface
- [ ] CaptureRepository: 캡처 CRUD + 페이징 + 소프트삭제
- [ ] TodoRepository: Todo 조회/완료 토글
- [ ] ScheduleRepository: 날짜별 조회
- [ ] NoteRepository: 폴더별 조회/이동
- [ ] FolderRepository: CRUD + 시스템 폴더
- [ ] TagRepository: 생성/조회
- [ ] SyncQueueRepository: 큐 관리
- [ ] UserPreferenceRepository: K-V 설정
- [ ] `suspend` / `Flow` 적절 사용

---

## 4. 스펙 일치 검증 (기능명세서 기준)

### 4.1 분류 체계
- [ ] classified_type 4개: TEMP, SCHEDULE, TODO, NOTES
- [ ] note_sub_type 4개: INBOX, IDEA, BOOKMARK, USER_FOLDER
- [ ] classified_type=NOTES일 때만 note_sub_type 존재 (그 외는 null)
- [ ] 사용자 분류 수정 UI 옵션 매핑 정확: 일정→SCHEDULE, 할 일→TODO, 노트→NOTES/INBOX, 아이디어→NOTES/IDEA

### 4.2 삭제 모델 (Phase 1)
- [ ] is_deleted=true → Soft Delete (UI 제거)
- [ ] Snackbar 3초 → 실행 취소 또는 Hard Delete
- [ ] Hard Delete = DB 완전 삭제 + 파생 엔티티 삭제

### 4.3 파생 엔티티 관계
- [ ] Capture 1개당 Todo/Schedule/Note 중 최대 1개
- [ ] TODO → Todo 존재, SCHEDULE → Schedule 존재, NOTES → Note 존재, TEMP → 없음
- [ ] 분류 변경 시: 기존 파생 삭제 → Capture 업데이트 → 새 파생 생성 (트랜잭션)

### 4.4 폴더 시스템
- [ ] 시스템 폴더 3개: system-inbox(INBOX), system-ideas(IDEAS), system-bookmarks(BOOKMARKS)
- [ ] 시스템 폴더 삭제 불가
- [ ] 사용자 폴더 삭제 시 → 소속 노트 Inbox 이동 (folder_id=system-inbox, note_sub_type=INBOX)
- [ ] 폴더명: 1~30자, 중복 불가

### 4.5 SyncQueue
- [ ] CLASSIFY: 최대 3회 재시도, 지수 백오프 (5s → 15s → 45s)
- [ ] 큐 상태: PENDING → PROCESSING → COMPLETED/FAILED

---

## 5. 빌드 & 테스트

- [ ] `./gradlew :app:compileDebugKotlin` 성공
- [ ] `./gradlew testDebugUnitTest` 성공
- [ ] 미사용 import/변수 경고 없음

---

*작성일: 2026-02-06 | QA 담당*
