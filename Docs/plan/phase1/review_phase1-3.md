# Phase 1-3 코드 리뷰 리포트

> **리뷰 범위:** Mapper, Repository 구현체, DTO, API, DI Module
> **기준 문서:** 데이터 모델 명세서 v2.0, 기능명세서 v2.1
> **리뷰 일자:** 2026-02-06

---

## 1. 전체 요약

| 구분 | 결과 |
|------|------|
| 스펙 불일치 | 2건 (MEDIUM 1, LOW 1) |
| 코드 컨벤션 위반 | 0건 |
| 잠재적 이슈 | 4건 (HIGH 1, MEDIUM 2, INFO 1) |
| 이전 리뷰 수정사항 반영 | 1건 재확인, 1건 하향조정, 1건 유지 |

전체적으로 양호한 구현. Mapper는 패턴이 일관되고, Repository 구현체도 인터페이스에 충실함. DTO/API 정리도 깔끔. 다만, FolderRepositoryImpl의 폴더 삭제 시 note_sub_type 미반영 건과, CaptureRepositoryImpl의 트랜잭션 미사용 건은 수정 권장.

---

## 2. 이전 리뷰 수정사항 반영 확인

### 2.1 [하향조정] ScheduleEntity `calendar_sync_status` 필드 누락

- **이전 등급:** MEDIUM
- **조정 등급:** N/A (Phase 1 범위 외)
- **파일:** `data/local/database/entities/ScheduleEntity.kt`
- **재확인 결과:** 데이터 모델 명세서 2.3 테이블에서 `calendar_sync_status`는 **Phase 2a** 컬럼으로 명시. Phase 1 구현 범위에서 제외하는 것이 올바른 판단.
- **이전 리포트 오류 정정:** 이전 리포트에서 "Phase 1부터 기본값으로 포함하도록 명시"라고 기술했으나, 스펙 테이블의 Phase 컬럼은 명확히 2a로 표기. 따라서 이 항목은 **Phase 1 결함이 아님**.

### 2.2 [유지 — MEDIUM] NoteDao.moveAllToInbox에서 Capture의 note_sub_type 미변경

- **파일:** `data/repository/FolderRepositoryImpl.kt:36-39`, `data/local/database/dao/NoteDao.kt:70-75`
- **스펙:** 데이터 모델 명세서 2.5 — "삭제 시: 소속 노트의 folder_id → system-inbox, **note_sub_type → INBOX**로 변경"
- **현재:** `FolderRepositoryImpl.deleteFolder()`는 `noteDao.moveAllToInbox()`로 `folder_id`만 system-inbox로 변경. 해당 Capture의 `note_sub_type`은 변경하지 않음.
- **영향:** 사용자 폴더에 속한 노트가 USER_FOLDER 서브타입을 유지한 채 Inbox로 이동됨. UI 필터링에 영향 가능.
- **권장:** `CaptureDao`에 `updateNoteSubTypeByFolderId(folderId, "INBOX")` 쿼리를 추가하고, `FolderRepositoryImpl.deleteFolder()`에서 호출.

### 2.3 [유지 — LOW] CaptureDao.getUnconfirmedCaptures에 24시간 필터 미적용

- **파일:** `data/local/database/dao/CaptureDao.kt:60-67`
- **스펙:** 기능명세서 3.2 — "최근 24시간 이내 AI 분류가 완료되고, 사용자가 아직 확인하지 않은 항목만 표시"
- **현재:** `WHERE is_confirmed = 0 AND classified_type != 'TEMP' AND is_deleted = 0` — 시간 조건 없음.
- **참고:** 이 필터링은 Presentation Layer(ViewModel)에서 처리할 수도 있으나, DAO 쿼리에 `AND classification_completed_at > :threshold24h` 조건을 추가하는 것이 더 효율적.
- **결정 필요:** UseCase/ViewModel에서 처리할 것인지, DAO에서 처리할 것인지 명확히 결정 필요.

---

## 3. 신규 발견사항

### 3.1 [HIGH] CaptureRepositoryImpl.updateClassification — 트랜잭션 미사용

- **파일:** `data/repository/CaptureRepositoryImpl.kt:47-78`
- **현재:** `updateClassification()`이 5개의 개별 DAO 호출을 순차 실행:
  1. `captureDao.updateClassification()`
  2. `captureDao.updateAiTitle()`
  3. `captureDao.updateConfidence()`
  4. `captureSearchDao.deleteForUpdate()`
  5. `captureSearchDao.insert()`
- **문제:** 중간에 예외 발생 시 부분 업데이트 상태가 됨 (예: 분류 유형은 변경되었으나 제목은 미변경).
- **권장:** `@Transaction` 어노테이션을 사용하거나, `withTransaction` 블록으로 원자성 보장.

### 3.2 [MEDIUM] FolderRepositoryImpl.deleteFolder — 트랜잭션 미사용

- **파일:** `data/repository/FolderRepositoryImpl.kt:36-39`
- **현재:** `deleteFolder()`가 2개의 개별 DAO 호출을 순차 실행:
  1. `noteDao.moveAllToInbox(folderId, ...)`
  2. `folderDao.deleteById(folderId)`
- **문제:** 1번 성공, 2번 실패 시 노트가 Inbox로 이동되었으나 폴더는 삭제되지 않은 상태가 됨.
- **권장:** `@Transaction` 또는 `withTransaction` 적용.

### 3.3 [MEDIUM] Mapper의 valueOf() crash 위험 — 불일치한 에러 처리 패턴

- **파일:**
  - `CaptureMapper.kt:26-29` — `ClassifiedType.valueOf()`, `NoteSubType.valueOf()`, `ConfidenceLevel.valueOf()`, `CaptureSource.valueOf()` 모두 try-catch 없음
  - `ScheduleMapper.kt:23` — `ConfidenceLevel.valueOf()` try-catch 없음
  - `FolderMapper.kt:19` — `FolderType.valueOf()` try-catch 없음
  - `EntityMapper.kt:19` — `EntityType.valueOf()` try-catch 없음
  - `SyncQueueMapper.kt:19,23` — `SyncAction.valueOf()`, `SyncQueueStatus.valueOf()` try-catch 없음
- **비교:** `ClassificationMapper.kt:62-84`는 모든 enum 변환에 try-catch + fallback을 적용하고 있음
- **문제:** Room DB에 잘못된 enum 값이 저장되었을 경우(마이그레이션 오류 등) `IllegalArgumentException`으로 앱 crash 발생.
- **권장:** 현재 Phase 1에서는 DB를 destructive migration(v8→v9)으로 관리하므로 실질적 위험은 낮음. 다만, 향후 마이그레이션이 도입되면 Entity→Domain Mapper에도 방어적 변환을 적용하는 것을 권장. **현재는 INFO 수준으로 재분류 가능하나, 일관성 관점에서 MEDIUM으로 유지.**

### 3.4 [INFO] ClassificationMapper의 ExtractedEntity 변환 시 captureId 빈 문자열

- **파일:** `data/mapper/ClassificationMapper.kt:42`
- **현재:** `captureId = ""` (빈 문자열)로 설정, 주석에 "호출자가 설정"으로 명시
- **문제:** DTO→Domain 변환 시 captureId를 알 수 없으므로 빈 문자열은 합리적. 다만, 호출자(UseCase)가 반드시 captureId를 설정해야 하므로, 잊을 경우 빈 문자열이 DB에 저장될 수 있음.
- **영향:** UseCase 구현 리뷰에서 확인 필요. 현재로서는 정보 수준.

---

## 4. 리뷰 대상 파일별 상세

### 4.1 Mapper (9개)

| 파일 | 결과 | 비고 |
|------|------|------|
| CaptureMapper.kt | PASS (주의사항 있음) | valueOf() crash 위험 (3.3) |
| TodoMapper.kt | PASS | 1:1 필드 매핑, 문제 없음 |
| ScheduleMapper.kt | PASS (주의사항 있음) | valueOf() crash 위험 (3.3) |
| NoteMapper.kt | PASS | 1:1 필드 매핑, 문제 없음 |
| ClassificationMapper.kt | PASS | 방어적 enum 변환 적용, captureId="" (3.4) |
| FolderMapper.kt | PASS (주의사항 있음) | valueOf() crash 위험 (3.3) |
| TagMapper.kt | PASS | 1:1 필드 매핑, 문제 없음 |
| EntityMapper.kt | PASS (주의사항 있음) | valueOf() crash 위험 (3.3) |
| SyncQueueMapper.kt | PASS (주의사항 있음) | valueOf() crash 위험 (3.3) |

### 4.2 Repository 구현체 (8개)

| 파일 | 결과 | 비고 |
|------|------|------|
| CaptureRepositoryImpl.kt | FAIL — 수정 필요 | updateClassification 트랜잭션 미사용 (3.1) |
| TodoRepositoryImpl.kt | PASS | 인터페이스 충실 구현 |
| ScheduleRepositoryImpl.kt | PASS | 인터페이스 충실 구현 |
| NoteRepositoryImpl.kt | PASS | moveToFolder에서 Capture note_sub_type 미변경은 Presentation에서 처리 가능 |
| FolderRepositoryImpl.kt | FAIL — 수정 필요 | 트랜잭션 미사용 (3.2), note_sub_type 미반영 (2.2) |
| TagRepositoryImpl.kt | PASS | getOrCreate 패턴 적절, UUID 생성 방식 적절 |
| SyncQueueRepositoryImpl.kt | PASS | 인터페이스 충실 구현 |
| UserPreferenceRepositoryImpl.kt | PASS | ThemePreference 변환 시 try-catch 적용 (모범 사례) |

### 4.3 DTO (v2)

| 파일 | 결과 | 비고 |
|------|------|------|
| ClassifyRequest.kt | PASS | 단순, @SerializedName 적용 |
| ClassifyResponse.kt | PASS | 중첩 DTO(EntityDto, ScheduleInfoDto, TodoInfoDto) 포함. 스펙 일치 |
| HealthResponse.kt | PASS | API v2.1 건강 체크 응답. nullable 필드 적절 |

### 4.4 API

| 파일 | 결과 | 비고 |
|------|------|------|
| KairosApi.kt | PASS | Retrofit 인터페이스. /classify (POST), /health (GET). 깔끔 |
| MockKairosApi.kt | PASS | KairosApi 구현체. 키워드 기반 분류, 태그/엔티티 추출 로직 포함. Mock으로서 충분 |

### 4.5 DI Module

| 파일 | 결과 | 비고 |
|------|------|------|
| RepositoryModule.kt | PASS | 8개 Repository 바인딩 완료. @Binds @Singleton 적용 |

---

## 5. 코드 컨벤션 점검

| 항목 | 결과 |
|------|------|
| 한글 주석 | PASS — 모든 파일에 한글 주석 사용 |
| @Singleton @Inject constructor | PASS — 모든 Mapper/RepoImpl에 적용 |
| Domain Layer 순수성 | PASS — Mapper는 data 레이어에 위치 (적절) |
| Android 의존성 격리 | PASS — Mapper에 Android import 없음 |
| Enum ↔ String 변환 패턴 | PASS — Entity는 String 저장, Mapper에서 변환 (TypeConverter 미사용) |
| @Binds 사용 | PASS — RepositoryModule에서 @Provides 대신 @Binds 사용 (적절) |
| 네이밍 | PASS — XxxMapper, XxxRepositoryImpl 패턴 일관 |

---

## 6. 수정 권장 우선순위

| 순위 | 항목 | 등급 | 파일 |
|------|------|------|------|
| 1 | CaptureRepositoryImpl.updateClassification 트랜잭션 | HIGH | CaptureRepositoryImpl.kt |
| 2 | FolderRepositoryImpl.deleteFolder 트랜잭션 + note_sub_type 반영 | MEDIUM | FolderRepositoryImpl.kt, CaptureDao.kt, NoteDao.kt |
| 3 | CaptureDao.getUnconfirmedCaptures 24시간 필터 (DAO vs UseCase 결정) | LOW | CaptureDao.kt 또는 UseCase |

---

## 7. 참고사항

- `TagRepositoryImpl`에서 `TagEntity` import가 있으나 직접 사용하지 않음 (TagMapper를 통해 간접 사용). IDE warning 수준이며 동작에 영향 없음.
- `MockKairosApi`는 `java.time.LocalDate`, `LocalTime`, `ZoneId` 등 Java 8 Time API를 직접 사용. Android min SDK 26+ 확인 필요 (desugaring 적용 시 무관).
- `UserPreferenceRepositoryImpl`의 `getThemePreference()`는 `ThemePreference.valueOf()` 변환 시 try-catch를 적용하고 있어 다른 Mapper 대비 방어적. 이 패턴을 모범 사례로 참고 가능.

---

## 8. 수정 완료 (QA 직접 패치)

> **수정일:** 2026-02-06
> **수정자:** QA

### 8.1 [HIGH] CaptureRepositoryImpl.updateClassification — 트랜잭션 적용

- **파일:** `data/repository/CaptureRepositoryImpl.kt`
- **수정 내용:**
  - `KairosDatabase` 의존성 추가 (생성자 주입)
  - `updateClassification()` 내부의 5개 DAO 호출을 `database.withTransaction {}` 블록으로 감싸서 원자성 보장
  - `withTransaction`은 CaptureDao + CaptureSearchDao 2개의 DAO를 걸치므로, DAO 레벨 `@Transaction` 대신 RoomDatabase 레벨 트랜잭션 사용

### 8.2 [MEDIUM] FolderRepositoryImpl.deleteFolder — 트랜잭션 + note_sub_type 반영

- **파일:** `data/repository/FolderRepositoryImpl.kt`, `data/local/database/dao/CaptureDao.kt`
- **수정 내용:**
  - `KairosDatabase`, `CaptureDao` 의존성 추가 (생성자 주입)
  - `CaptureDao`에 `updateNoteSubTypeByFolderId(folderId, noteSubType, updatedAt)` 쿼리 추가 — 하위 쿼리로 해당 폴더에 속한 노트들의 capture_id를 조회하여 note_sub_type 일괄 변경
  - `deleteFolder()` 내부를 `database.withTransaction {}` 블록으로 감싸서 3단계 원자성 보장: (1) 캡처 note_sub_type → INBOX 변경 (2) 노트 folder_id → system-inbox 이동 (3) 폴더 삭제
  - note_sub_type 변경을 먼저 수행 (폴더 삭제 전 notes 테이블의 folder_id 참조가 유효한 상태에서 쿼리 실행)

### 8.3 [MEDIUM] Mapper valueOf() 방어적 처리

- **수정 파일 및 fallback 기본값:**

| 파일 | 대상 Enum | fallback 기본값 |
|------|-----------|-----------------|
| `CaptureMapper.kt` | ClassifiedType | TEMP |
| `CaptureMapper.kt` | NoteSubType | INBOX |
| `CaptureMapper.kt` | ConfidenceLevel | MEDIUM |
| `CaptureMapper.kt` | CaptureSource | APP |
| `ScheduleMapper.kt` | ConfidenceLevel | MEDIUM |
| `FolderMapper.kt` | FolderType | USER |
| `EntityMapper.kt` | EntityType | DATE |
| `SyncQueueMapper.kt` | SyncAction | CLASSIFY |
| `SyncQueueMapper.kt` | SyncQueueStatus | PENDING |

- **패턴:** `ClassificationMapper`와 동일한 try-catch + fallback 패턴 적용. `IllegalArgumentException` 캐치 후 안전한 기본값 반환.
- **참고:** `toEntity()` 방향은 Domain enum → String(`name`) 변환이므로 실패 불가, 수정 불필요.

### 8.4 컴파일 확인

- `./gradlew :app:compileDebugKotlin` 실행
- 수정된 파일에서 컴파일 에러 없음
- 기존 빌드 에러(NonExistentClass in SettingsViewModel, ResultViewModel)는 다른 팀원의 in-progress UseCase에 의한 것으로, 본 패치와 무관
