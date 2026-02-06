# Phase 1-1, 1-2 코드 리뷰 리포트

> **리뷰 범위:** Data Layer (Entity/DAO/Database) + Domain Layer (Model/Enum/Repository Interface)
> **기준 문서:** 데이터 모델 명세서 v2.0, 기능명세서 v2.1
> **리뷰 일자:** 2026-02-06

---

## 1. 전체 요약

| 구분 | 결과 |
|------|------|
| 스펙 불일치 | 3건 (MEDIUM 2, LOW 1) |
| 코드 컨벤션 위반 | 0건 |
| 잠재적 이슈 | 4건 |
| 레거시 잔여물 | 2건 (별도 태스크로 처리 예정) |

전체적으로 스펙을 충실하게 구현하였으며, 심각한 문제는 없음. 아래 항목들은 Phase 1-3 (Mapper/Repository 구현) 진행 시 함께 수정 권장.

---

## 2. 스펙 불일치 항목

### 2.1 [MEDIUM] ScheduleEntity에 `calendar_sync_status` 필드 누락

- **파일:** `data/local/database/entities/ScheduleEntity.kt`
- **스펙:** 데이터 모델 명세서 2.3 — `calendar_sync_status` (Enum, 필수, Phase 1 기본값 NOT_LINKED)
- **현재:** 해당 필드 없음
- **영향:** Phase 2a에서 Google Calendar 동기화 추가 시 마이그레이션 필요. 스펙에는 Phase 1부터 기본값으로 포함하도록 명시.
- **권장:** `calendar_sync_status: String = "NOT_LINKED"` 필드 추가. Phase 1에서는 사용하지 않지만, 마이그레이션 비용 절감을 위해 스키마에 미리 포함하는 것이 좋음. 단, 구현 계획서(implementation_plan.md)에서 Phase 1 scope를 명시적으로 제외한 것인지 확인 필요.

### 2.2 [MEDIUM] NoteDao.moveAllToInbox — Capture의 note_sub_type 미변경

- **파일:** `data/local/database/dao/NoteDao.kt:70-75`
- **스펙:** 데이터 모델 명세서 2.5 — "삭제 시 소속 노트의 folder_id → system-inbox, note_sub_type → INBOX로 변경"
- **현재:** `moveAllToInbox()`는 notes 테이블의 `folder_id`만 `system-inbox`로 변경. captures 테이블의 `note_sub_type`은 변경하지 않음.
- **영향:** 폴더 삭제 후 캡처의 `note_sub_type`이 `USER_FOLDER`로 남아, UI 표시 및 필터링에 불일치 발생 가능.
- **권장:** DAO 단독으로는 처리 불가 (다른 테이블). Phase 1-3 Repository 구현 또는 Phase 1-4 UseCase에서 `@Transaction`으로 captures.note_sub_type도 함께 INBOX로 변경해야 함.

### 2.3 [LOW] CaptureDao.getUnconfirmedCaptures — 24시간 필터 미적용

- **파일:** `data/local/database/dao/CaptureDao.kt:60-67`
- **스펙:** 기능명세서 3.2 — "최근 24시간 이내 AI 분류가 완료되고, 사용자가 아직 확인하지 않은 항목만 표시"
- **현재:** `is_confirmed = 0 AND classified_type != 'TEMP' AND is_deleted = 0`만 필터. `classification_completed_at`으로 24시간 범위 필터 없음.
- **영향:** AI Status Sheet에 24시간 이전 항목도 표시될 수 있음. 이 필터는 presentation 레이어 또는 UseCase에서도 적용 가능하므 심각도 LOW.
- **권장:** DAO 레벨에서 `AND classification_completed_at >= :since24h` 조건 추가하거나, UseCase에서 필터링.

---

## 3. 잠재적 이슈

### 3.1 [INFO] CaptureEntity — Enum을 String으로 저장

- **파일:** `data/local/database/entities/CaptureEntity.kt:36-46`
- **현재:** `classifiedType`, `noteSubType`, `confidence`, `source` 모두 `String` 타입으로 저장.
- **분석:** TypeConverter 없이 String으로 직접 저장하는 방식. Domain 모델은 Enum을 사용하므로 Mapper에서 변환 필요. 유효하지 않은 String이 들어갈 위험이 있지만, 현재 구조에서는 Mapper가 검증 역할을 하므로 허용 가능.
- **권장:** Phase 1-3 Mapper 구현 시 `toEntity()` / `toDomain()`에서 올바른 Enum 변환 + 예외 처리 필수.

### 3.2 [INFO] CaptureSearchFts — contentEntity 미사용

- **파일:** `data/local/database/entities/CaptureSearchFts.kt:12`
- **현재:** `@Fts4` 사용, `contentEntity` 미지정 (독립 FTS 테이블).
- **분석:** 캡처 생성/수정/삭제 시 FTS 테이블을 수동으로 동기화해야 함. 주석에도 명시되어 있음.
- **권장:** Phase 1-3 Repository에서 캡처 CUD 시 `CaptureSearchDao.insert/delete` 호출을 빠뜨리지 않도록 주의.

### 3.3 [INFO] CaptureDao.getActiveCapturesPaged — Flow로 반환

- **파일:** `data/local/database/dao/CaptureDao.kt:38-44`
- **현재:** 페이징 쿼리가 `Flow<List<CaptureEntity>>`를 반환.
- **분석:** Paging3 라이브러리를 사용하지 않는 수동 페이징. offset/limit 기반이므로 리스트가 업데이트되면 Flow가 재발행되어 데이터 중복/누락이 발생할 수 있음.
- **권장:** Phase 1에서는 이 방식으로 충분하나, 목록이 길어지면 Paging3 도입 검토.

### 3.4 [INFO] 레거시 파일 잔존

- **파일:** `entities/BookmarkEntity.kt`, `entities/NotificationEntity.kt`, `dao/BookmarkDao.kt`, `dao/NotificationDao.kt`
- **현재:** 파일은 존재하지만 KairosDatabase에 등록되지 않아 사용되지 않음.
- **영향:** 컴파일에 영향 없음. 정리 대상이나 별도 태스크로 처리 예정.

---

## 4. 코드 컨벤션 확인

| 항목 | 결과 |
|------|------|
| 한글 주석 | PASS — 모든 Entity, DAO, Domain Model, Repository Interface에 한글 주석 |
| 네이밍 컨벤션 | PASS — Entity: `~Entity`, DAO: `~Dao`, Domain: 접미사 없음 |
| Domain 순수 Kotlin | PASS — `android.*`, `androidx.*`, Room, Hilt import 없음 |
| Room 어노테이션 | PASS — `@Entity`, `@PrimaryKey`, `@ForeignKey`, `@Index`, `@Fts4` 올바르게 사용 |
| Column 네이밍 | PASS — snake_case 일관 적용 (`@ColumnInfo`) |
| import 정리 | PASS — 미사용 import 없음 |

---

## 5. 스펙 일치 검증 결과

### 5.1 Entity 필드 검증

| Entity | 스펙 대비 | 비고 |
|--------|----------|------|
| CaptureEntity | **일치** | 모든 Phase 1 필드 존재. Phase 2a 필드(is_trashed, trashed_at, image_uri) 올바르게 미포함 |
| TodoEntity | **일치** | capture_id UNIQUE FK, deadline, is_completed, sort_order 등 |
| ScheduleEntity | **부분 일치** | calendar_sync_status 누락 (2.1 참조) |
| NoteEntity | **일치** | capture_id UNIQUE FK, folder_id FK(SET_NULL) |
| FolderEntity | **일치** | id, name, type, sort_order, created_at |
| TagEntity | **일치** | name UNIQUE Index |
| CaptureTagEntity | **일치** | 복합 PK (capture_id, tag_id), 양쪽 FK CASCADE |
| ExtractedEntityEntity | **일치** | capture_id FK CASCADE, type, value, normalized_value |
| SyncQueueEntity | **일치** | action, payload, retry_count(기본 0), max_retries(기본 3), status |
| UserPreferenceEntity | **일치** | key PK, value |
| CaptureSearchFts | **일치** | capture_id, title_text, original_text, tag_text, entity_text |

### 5.2 Enum 검증

| Enum | 스펙 값 | 코드 값 | 결과 |
|------|---------|---------|------|
| ClassifiedType | TEMP, SCHEDULE, TODO, NOTES | 동일 | PASS |
| NoteSubType | INBOX, IDEA, BOOKMARK, USER_FOLDER | 동일 | PASS |
| ConfidenceLevel | HIGH, MEDIUM, LOW | 동일 | PASS |
| CaptureSource | APP, SHARE_INTENT, WIDGET, SPLIT | 동일 | PASS |
| FolderType | INBOX, IDEAS, BOOKMARKS, AI_GROUP, USER | 동일 | PASS |
| EntityType | PERSON, PLACE, DATE, TIME, AMOUNT, OTHER | 동일 | PASS |
| SyncAction | CLASSIFY, RECLASSIFY, CALENDAR_CREATE, CALENDAR_DELETE, ANALYTICS_BATCH | 동일 | PASS |
| SyncQueueStatus | PENDING, PROCESSING, COMPLETED, FAILED | 동일 | PASS |

### 5.3 인덱스 검증

| Entity | 스펙 인덱스 | 코드 인덱스 | 결과 |
|--------|------------|------------|------|
| Capture | classified_type, note_sub_type, created_at, is_deleted, is_confirmed | 동일 (5개) | PASS |
| Todo | capture_id(UNIQUE), is_completed, deadline, sort_order | 동일 (4개) | PASS |
| Schedule | capture_id(UNIQUE), start_time | 동일 (2개) | PASS |
| Note | capture_id(UNIQUE), folder_id | 동일 (2개) | PASS |
| ExtractedEntity | capture_id, type | 동일 (2개) | PASS |
| SyncQueue | status, next_retry_at | 동일 (2개) | PASS |
| Tag | name(UNIQUE) | 동일 (1개) | PASS |
| CaptureTag | capture_id, tag_id | 동일 (2개) | PASS |

### 5.4 FK & onDelete 정책

| FK 관계 | onDelete | 스펙 부합 | 비고 |
|---------|----------|----------|------|
| Todo → Capture | CASCADE | PASS | 캡처 삭제 시 할 일 삭제 |
| Schedule → Capture | CASCADE | PASS | 캡처 삭제 시 일정 삭제 |
| Note → Capture | CASCADE | PASS | 캡처 삭제 시 노트 삭제 |
| Note → Folder | SET_NULL | PASS | 폴더 삭제 시 folder_id NULL |
| CaptureTag → Capture | CASCADE | PASS | 캡처 삭제 시 태그 관계 삭제 |
| CaptureTag → Tag | CASCADE | PASS | 태그 삭제 시 관계 삭제 |
| ExtractedEntity → Capture | CASCADE | PASS | 캡처 삭제 시 엔티티 삭제 |

### 5.5 Database 검증

| 항목 | 결과 |
|------|------|
| version = 9 | PASS |
| 11개 Entity 등록 | PASS |
| exportSchema = true | PASS |
| 시스템 폴더 시딩 Callback | PASS (system-inbox, system-ideas, system-bookmarks) |
| DatabaseModule DI | PASS (모든 DAO Provides) |

### 5.6 Domain Model 검증

| Model | Entity 대응 | 필드 일치 | 비고 |
|-------|------------|----------|------|
| Capture | CaptureEntity | PASS | Enum 타입 사용 (Entity는 String) |
| Todo | TodoEntity | PASS | |
| Schedule | ScheduleEntity | PASS | |
| Note | NoteEntity | PASS | |
| Folder | FolderEntity | PASS | companion에 시스템 폴더 ID 상수 |
| Tag | TagEntity | PASS | |
| ExtractedEntity | ExtractedEntityEntity | PASS | EntityType Enum 사용 |
| SyncQueueItem | SyncQueueEntity | PASS | SyncAction, SyncQueueStatus Enum 사용 |
| Classification | - | N/A | AI 응답 매핑 전용 |

### 5.7 Repository Interface 검증

| Interface | 필요 메서드 | 비고 |
|-----------|-----------|------|
| CaptureRepository | PASS | saveCapture, updateClassification, confirmClassification, softDelete/undo/hardDelete, getUnconfirmed, getAllCaptures(paged), getTempCaptures, searchCaptures |
| TodoRepository | PASS | createTodo, getActiveTodos, toggleCompletion, deleteByCaptureId |
| ScheduleRepository | PASS | createSchedule, getSchedulesByDate, getDatesWithSchedules, deleteByCaptureId |
| NoteRepository | PASS | createNote, getNotesByFolderId, moveToFolder, deleteByCaptureId, getNoteCountByFolderId |
| FolderRepository | PASS | getAllFolders, createFolder, renameFolder, deleteFolder, existsByName |
| TagRepository | PASS | getOrCreate, linkTagToCapture, getTagsByCaptureId, deleteTagsByCaptureId |
| SyncQueueRepository | PASS | enqueue, getPendingItems, updateStatus, incrementRetry, deleteCompleted, resetProcessingToPending |
| UserPreferenceRepository | PASS | getThemePreference, setThemePreference, isOnboardingCompleted, getString/setString |

---

## 6. Phase 1-3 진행 시 주의사항

1. **Mapper 구현 시** — Entity의 String 타입 Enum 필드 ↔ Domain Enum 변환에서 예외 처리 필수
2. **CaptureRepositoryImpl 구현 시** — hardDelete에서 FTS 테이블 동기 삭제 (`CaptureSearchDao.deleteByCaptureId`) 포함
3. **FolderRepositoryImpl.deleteFolder 구현 시** — notes.folder_id 이동 + captures.note_sub_type=INBOX 변경을 트랜잭션으로 처리
4. **AI 분류 결과 적용 시** — Capture 업데이트 + 파생 엔티티 생성 + FTS 인덱스 갱신 + 태그/엔티티 삽입을 단일 트랜잭션으로

---

*리뷰 담당: QA | 2026-02-06*
