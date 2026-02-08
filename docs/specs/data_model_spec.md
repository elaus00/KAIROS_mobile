# KAIROS — 데이터 모델 명세서

> **Version**: 2.0
**작성일**: 2026-02-06
**기준**: PRD v10.0, 기능명세서 v2.0
>

---

## 변경 이력

| 버전 | 날짜 | 주요 변경 내용 |
| --- | --- | --- |
| 1.0 | 2026-02-06 | 초기 작성 (PRD v9.2 기준) |
| 2.0 | 2026-02-06 | PRD v10.0 + 기능명세서 v2.0 반영 (불일치 시 PRD 우선). (1) 분류 체계 전면 변경 — classified_type을 SCHEDULE/TODO/NOTES/TEMP 4개로 재편, note_sub_type 필드 추가, (2) 삭제 모델 변경 — Phase 1: Soft Delete(3초) → Hard Delete, Phase 2a: 휴지통(30일) 도입으로 3단계화, is_trashed/trashed_at 필드 추가(Phase 2a), (3) AI 분류 확인 추적 — is_confirmed/confirmed_at 필드 추가 (AI 분류 현황 시트 뱃지 지원), (4) 멀티 인텐트 분할(Phase 2b) — parent_capture_id 필드 추가, source enum에 SPLIT 추가, (5) Folder에 BOOKMARKS 타입 및 system-bookmarks 시스템 폴더 추가, (6) AnalyticsEvent 이벤트 유형 업데이트 — classification_confirmed/trash_restored/split_capture_created 추가, inbox_item_resolved → temp_item_resolved 변경 |
| 2.1 | 2026-02-07 | 서버 PostgreSQL 스키마 정렬. (1) analytics_events 저장 테이블을 서버 섹션에 명시, (2) Phase 2a 디바이스별 Google OAuth 토큰 저장용 google_device_tokens 테이블 추가 |

## 1. 아키텍처 개요

### 1.1 로컬 우선 원칙

모든 콘텐츠 데이터는 Android Room DB에 로컬 우선 저장한다. 서버는 AI 처리, 캘린더 동기화, 인증·과금, 분석 데이터 수집만 담당한다.

```
[Android Room DB]          [서버 (FastAPI)]
├── captures               ├── AI 분류 API
├── todos                  ├── 캘린더 동기화 API
├── schedules              ├── 분석 이벤트 수집 API
├── notes                  ├── 인증 API (Phase 3a)
├── folders                └── 구독 API (Phase 3a)
├── tags / capture_tags
├── entities
├── classification_logs
├── analytics_events (큐)
└── sync_queue
```

### 1.2 데이터 흐름

```
사용자 입력 → Capture 로컬 저장 (classified_type=TEMP) → 서버 AI 분류 요청 →
→ 분류 결과 수신 → Capture 업데이트 (classified_type + note_sub_type) + 파생 엔티티 생성
→ AI 분류 현황 시트에 미확인 항목 추가
→ (일정인 경우) 캘린더 동기화 처리
```

---

## 2. 엔티티 정의 (로컬 Room DB)

### 2.1 Capture (캡처)

모든 기록의 원본. 시스템의 중심 엔티티.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| original_text | String | ✓ | 사용자가 입력한 원본 텍스트 | 1 |
| ai_title | String? | | AI 생성 요약 제목 (최대 30자) | 1 |
| classified_type | Enum | ✓ | SCHEDULE, TODO, NOTES, TEMP | 1 |
| note_sub_type | Enum? | | INBOX, IDEA, BOOKMARK, USER_FOLDER. classified_type=NOTES일 때만 사용 | 1 |
| confidence | Enum? | | HIGH, MEDIUM, LOW | 1 |
| source | Enum | ✓ | APP, SHARE_INTENT, WIDGET, SPLIT | 1 |
| parent_capture_id | String? | | FK → captures.id. 멀티 인텐트 분할 시 원본 캡처 참조 | 2b |
| image_uri | String? | | 첨부 이미지 로컬 경로 | 2a |
| is_confirmed | Boolean | ✓ | AI 분류 확인 여부 (AI 분류 현황 뱃지에 활용). 기본값 false. TEMP는 대상 외 | 1 |
| confirmed_at | Long? | | AI 분류 확인 시각 | 1 |
| is_deleted | Boolean | ✓ | 소프트 삭제 (Snackbar 실행 취소용). 기본값 false | 1 |
| deleted_at | Long? | | 소프트 삭제 시점 (Snackbar 3초 이내 실행 취소 가능) | 1 |
| is_trashed | Boolean | ✓ | 휴지통 상태. 기본값 false | 2a |
| trashed_at | Long? | | 휴지통 이동 시점 (30일 경과 시 하드 삭제) | 2a |
| draft_text | String? | | 임시 저장 텍스트 (앱 이탈 시, EncryptedSharedPreferences 병행) | 1 |
| created_at | Long | ✓ | 생성 시각 (epoch ms) | 1 |
| updated_at | Long | ✓ | 최종 수정 시각 | 1 |
| classification_completed_at | Long? | | AI 분류 완료 시각 | 1 |

**인덱스:** `classified_type`, `note_sub_type`, `created_at`, `is_deleted`, `is_trashed`, `parent_capture_id`, `is_confirmed`

**classified_type 열거형:**

| 값 | 사용자 표시 | 설명 |
| --- | --- | --- |
| TEMP | (미표시) | 미분류 임시 상태. 사용자에게 분류 칩으로 표시하지 않음 |
| SCHEDULE | 일정 | 캘린더 탭 |
| TODO | 할 일 | 캘린더 탭 할 일 섹션 |
| NOTES | 노트 / 아이디어 | 노트 탭. note_sub_type으로 세분화 |

**note_sub_type 열거형 (classified_type=NOTES일 때):**

| 값 | 사용자 표시 | 기본 folder_id | 설명 |
| --- | --- | --- | --- |
| INBOX | 노트 | system-inbox | 미분류 노트. 기본 서브 분류 |
| IDEA | 아이디어 | system-ideas | 아이디어성 내용 |
| BOOKMARK | (노트) | system-bookmarks | URL 포함 콘텐츠 |
| USER_FOLDER | (노트) | 사용자 폴더 ID | 사용자가 직접 폴더로 이동한 노트 |

**사용자 분류 수정 UI 옵션 ↔ 내부 매핑:**

| UI 표시 | classified_type | note_sub_type |
| --- | --- | --- |
| 일정 | SCHEDULE | null |
| 할 일 | TODO | null |
| 노트 | NOTES | INBOX |
| 아이디어 | NOTES | IDEA |

**classified_type 상태 전이:**

```
TEMP (초기/폴백)
  → AI 분류 성공 → SCHEDULE | TODO | NOTES
  → AI 분류 실패/오프라인 → TEMP 유지 (재분류 대기)

SCHEDULE ↔ TODO ↔ NOTES (사용자 수동 수정)
```

**삭제 상태 전이:**

```
[Phase 1]
Active (기본: is_deleted=false)
  → 사용자 삭제 → Soft Delete (is_deleted=true, deleted_at=now)
    → Snackbar 3초 이내 "실행 취소" → Active 복원
    → Snackbar 만료 → Hard Delete (DB 완전 삭제)

[Phase 2a — 휴지통 도입]
Active (기본: is_deleted=false, is_trashed=false)
  → 사용자 삭제 → Soft Delete (is_deleted=true, deleted_at=now)
    → Snackbar 3초 이내 "실행 취소" → Active 복원
    → Snackbar 만료 → Trash (is_trashed=true, trashed_at=now, is_deleted=false)
      → 30일 이내 → 사용자 복원 가능
      → 30일 경과 → Hard Delete (DB 완전 삭제)
```

### 2.2 Todo (할 일)

Capture가 TODO로 분류될 때 생성되는 파생 엔티티.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| capture_id | String | ✓ | FK → captures.id (UNIQUE) | 1 |
| deadline | Long? | | 마감 일시 (epoch ms) | 1 |
| deadline_source | Enum? | | AI_EXTRACTED, AI_SUGGESTED, USER_SET | 2a |
| is_completed | Boolean | ✓ | 완료 여부. 기본값 false | 1 |
| completed_at | Long? | | 완료 처리 시각 | 1 |
| sort_order | Int | ✓ | 리스트 내 정렬 순서 | 1 |
| sort_source | Enum | ✓ | DEFAULT, AI, USER. 기본값 DEFAULT | 2a |
| created_at | Long | ✓ | 생성 시각 | 1 |
| updated_at | Long | ✓ | 최종 수정 시각 | 1 |

**인덱스:** `capture_id` (UNIQUE), `is_completed`, `deadline`, `sort_order`

**정렬 규칙 (기능명세서 6.1):**

| 우선순위 | 기준 | Phase |
| --- | --- | --- |
| 1 | sort_source=USER인 항목은 사용자 설정 위치 유지 | 2a |
| 2 | deadline이 있는 항목 → deadline 오름차순 (가까운 마감 먼저) | 1 |
| 3 | deadline이 없는 항목 → created_at 역순 (최근 생성 먼저) | 1 |

**상태 머신:**

```
ACTIVE (is_completed=false)
  → 사용자 체크 → COMPLETED (is_completed=true, completed_at 기록)
  → 분류 변경 → DELETED (Todo 레코드 삭제)
  → 캡처 삭제 → 삭제 상태 모델 따름

COMPLETED
  → 사용자 체크 해제 → ACTIVE (completed_at 초기화)
```

### 2.3 Schedule (일정)

Capture가 SCHEDULE로 분류될 때 생성되는 파생 엔티티.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| capture_id | String | ✓ | FK → captures.id (UNIQUE) | 1 |
| start_time | Long? | | 시작 일시 | 1 |
| end_time | Long? | | 종료 일시 | 1 |
| location | String? | | 장소 | 1 |
| is_all_day | Boolean | ✓ | 종일 이벤트 여부. 기본값 false | 1 |
| calendar_sync_status | Enum | ✓ | 캘린더 동기화 상태 | 2a |
| google_event_id | String? | | Google Calendar 이벤트 ID | 2a |
| confidence | Enum | ✓ | HIGH, MEDIUM, LOW | 1 |
| created_at | Long | ✓ | 생성 시각 | 1 |
| updated_at | Long | ✓ | 최종 수정 시각 | 1 |

**confidence 판별 기준 (서버 AI):**

| 신뢰도 | 조건 |
| --- | --- |
| HIGH | 명확한 날짜 + 시간 + 이벤트/장소 모두 식별 |
| MEDIUM | 날짜는 있으나 시간 불확실, 또는 상대 표현 ("다음 주") |
| LOW | 일정 가능성은 있으나 핵심 정보 부족 ("언제 한번 만나자") |

**calendar_sync_status 상태 머신 (Phase 2a):**

```
NOT_LINKED (Phase 1 기본값)
  → Google 연동 활성화 + confidence=HIGH + 완전자동모드 → SYNCED
  → Google 연동 활성화 + 그 외 → SUGGESTION_PENDING

SUGGESTION_PENDING
  → 사용자 승인 → SYNCED
  → 사용자 거부 → REJECTED
  → 일정 날짜 경과 → EXPIRED

SYNCED
  → 사용자 실행 취소 → NOT_LINKED (Google 이벤트 삭제)
  → 분류 변경 → NOT_LINKED (Google 이벤트 삭제 + Schedule 레코드 삭제)
  → Google Calendar API 오류 → SYNC_FAILED

SYNC_FAILED
  → 자동 재시도 성공 → SYNCED
  → 재시도 3회 실패 → SYNC_FAILED (사용자 알림)

REJECTED
  (최종 상태 — 재제안 없음)

EXPIRED
  (최종 상태)
```

**인덱스:** `capture_id` (UNIQUE), `start_time`, `calendar_sync_status`

### 2.4 Note (노트)

Capture가 NOTES로 분류될 때 생성되는 파생 엔티티.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| capture_id | String | ✓ | FK → captures.id (UNIQUE) | 1 |
| folder_id | String? | | FK → folders.id | 1 |
| body | String? | | 편집 가능한 본문 (원본과 별도) | 2b |
| created_at | Long | ✓ | 생성 시각 | 1 |
| updated_at | Long | ✓ | 최종 수정 시각 | 1 |

**인덱스:** `capture_id` (UNIQUE), `folder_id`

**folder_id 결정 규칙 (AI 분류 시):**

| note_sub_type | folder_id |
| --- | --- |
| INBOX | system-inbox |
| IDEA | system-ideas |
| BOOKMARK | system-bookmarks |
| USER_FOLDER | 해당 사용자 폴더 ID (수동 이동 시) |

### 2.5 Folder (폴더)

노트의 분류 컨테이너.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| name | String | ✓ | 폴더명 | 1 |
| type | Enum | ✓ | INBOX, IDEAS, BOOKMARKS, AI_GROUP, USER | 1 |
| sort_order | Int | ✓ | 폴더 정렬 순서 | 1 |
| created_at | Long | ✓ | 생성 시각 | 1 |

**시스템 폴더 (자동 생성, 삭제 불가):**

| id | name | type | sort_order | 표시 조건 |
| --- | --- | --- | --- | --- |
| system-inbox | Inbox | INBOX | 0 | 소속 노트 1개 이상일 때만 표시 |
| system-ideas | Ideas | IDEAS | 1 | 항상 표시 |
| system-bookmarks | Bookmarks | BOOKMARKS | 2 | 항상 표시 |

**폴더 표시 순서:** Inbox (조건부) → Ideas → Bookmarks → 사용자 폴더 (생성 순) → AI_GROUP 폴더 (Phase 3a)

**사용자 폴더 규칙:**

| 규칙 | 설명 |
| --- | --- |
| 이름 최소 길이 | 1자 이상 |
| 이름 최대 길이 | 30자 |
| 이름 중복 | 동일 이름 불가 (시스템 폴더명 포함) |
| 삭제 시 | 소속 노트의 folder_id → system-inbox, note_sub_type → INBOX로 변경 |

### 2.6 Tag (태그)

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| name | String | ✓ | 태그명 (UNIQUE) | 1 |
| created_at | Long | ✓ | 생성 시각 | 1 |

### 2.7 CaptureTag (캡처-태그 연결)

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| capture_id | String | ✓ | FK → captures.id | 1 |
| tag_id | String | ✓ | FK → tags.id | 1 |

**복합 PK:** (capture_id, tag_id)

### 2.8 Entity (추출 엔티티)

AI가 캡처 텍스트에서 추출한 핵심 개체.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| capture_id | String | ✓ | FK → captures.id | 1 |
| type | Enum | ✓ | PERSON, PLACE, DATE, TIME, AMOUNT, OTHER | 1 |
| value | String | ✓ | 원문 표현 ("금요일", "강남역") | 1 |
| normalized_value | String? | | 정규화된 값 ("2026-02-13", "강남역") | 1 |

**인덱스:** `capture_id`, `type`

### 2.9 ClassificationLog (분류 수정 로그)

사용자의 분류 수정 이력. 관측 및 학습에 활용.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 2a |
| capture_id | String | ✓ | FK → captures.id | 2a |
| original_type | Enum | ✓ | 변경 전 classified_type | 2a |
| original_sub_type | Enum? | | 변경 전 note_sub_type (NOTES인 경우) | 2a |
| new_type | Enum | ✓ | 변경 후 classified_type | 2a |
| new_sub_type | Enum? | | 변경 후 note_sub_type (NOTES인 경우) | 2a |
| time_since_classification_ms | Long | ✓ | 분류 완료 → 수정까지 경과 시간 | 2a |
| modified_at | Long | ✓ | 수정 시각 | 2a |

**인덱스:** `capture_id`, `modified_at`

### 2.10 AnalyticsEvent (분석 이벤트)

분류 품질 관측 및 사용자 행동 분석용 이벤트 큐.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 2a |
| event_type | String | ✓ | 이벤트 유형 | 2a |
| event_data | String | ✓ | JSON 직렬화 데이터 | 2a |
| timestamp | Long | ✓ | 이벤트 발생 시각 | 2a |
| is_synced | Boolean | ✓ | 서버 전송 완료 여부. 기본값 false | 2a |

**이벤트 유형:**

| event_type | event_data 포함 필드 | Phase |
| --- | --- | --- |
| capture_created | source, input_length | 2a |
| classification_completed | classified_type, note_sub_type, confidence, processing_time_ms | 2a |
| classification_confirmed | capture_id, confirmed_type, changed(boolean) | 2a |
| classification_modified | original_type, original_sub_type, new_type, new_sub_type, time_since_classification_ms | 2a |
| schedule_suggestion_response | action(approved/rejected/ignored), confidence, source(app/widget/share) | 2a |
| schedule_auto_deleted | time_since_auto_add_ms | 2a |
| temp_item_resolved | resolution(ai/user), time_in_temp_ms | 2a |
| trash_restored | time_in_trash_ms | 2a |
| split_capture_created | parent_capture_id, split_count | 2b |
| capture_revisited | time_since_creation_ms, access_method(list/search) | 2b |
| todo_completed | time_since_creation_ms | 2b |
| search_performed | result_count, result_clicked | 2b |

**인덱스:** `is_synced`, `timestamp`

**배치 전송 규칙:**

| 조건 | 동작 |
| --- | --- |
| 이벤트 50건 축적 | 즉시 전송 |
| 마지막 전송 후 1시간 경과 | 잔여 이벤트 전송 |
| 네트워크 복구 시 | 미전송 이벤트 전송 |

### 2.11 SyncQueue (동기화 큐)

오프라인 시 서버 요청을 큐잉.

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| id | String (UUID) | ✓ | PK | 1 |
| action | Enum | ✓ | CLASSIFY, RECLASSIFY, CALENDAR_CREATE, CALENDAR_DELETE, ANALYTICS_BATCH | 1 |
| payload | String | ✓ | JSON 직렬화 요청 데이터 | 1 |
| retry_count | Int | ✓ | 재시도 횟수. 기본값 0 | 1 |
| max_retries | Int | ✓ | 최대 재시도. 기본값 3 | 1 |
| status | Enum | ✓ | PENDING, PROCESSING, COMPLETED, FAILED | 1 |
| created_at | Long | ✓ | 생성 시각 | 1 |
| next_retry_at | Long? | | 다음 재시도 예정 시각 | 1 |

**인덱스:** `status`, `next_retry_at`

**재시도 정책 (기능명세서 0.5):**

| 작업 | 재시도 정책 |
| --- | --- |
| CLASSIFY | 최대 3회, 지수 백오프 (5s → 15s → 45s) |
| RECLASSIFY | 최대 3회, 실패 시 다음 주기로 이월 |
| CALENDAR_CREATE | 최대 3회, 실패 시 SYNC_FAILED + 사용자 알림 |
| CALENDAR_DELETE | 최대 5회, 지수 백오프 |
| ANALYTICS_BATCH | 최대 3회, 실패 시 다음 배치에 포함 |

**큐 상태 전이:**

```
PENDING → PROCESSING → COMPLETED
                     → FAILED (재시도 초과)
PENDING → PROCESSING → PENDING (재시도 대기, next_retry_at 설정)
```

### 2.12 UserPreference (사용자 설정) — Phase 1+

| 필드 | 타입 | 필수 | 설명 | Phase |
| --- | --- | --- | --- | --- |
| key | String | ✓ | PK. 설정 키 | 1 |
| value | String | ✓ | 설정 값 | 1 |

**주요 설정 키:**

| key | 기본값 | 설명 | Phase |
| --- | --- | --- | --- |
| dark_mode | SYSTEM | SYSTEM / LIGHT / DARK | 1 |
| schedule_add_mode | SUGGESTION | SUGGESTION / AUTO | 2a |
| schedule_notification_enabled | true | 일정 추가/제안 알림 on/off | 2a |
| deadline_notification_enabled | true | 마감 알림 on/off | 3a |
| deadline_notification_minutes | 30 | 마감 전 알림 시간 (분) | 3a |

### 2.13 GoogleAuth — Phase 3a

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| id | String | ✓ | PK |
| google_id | String | ✓ | Google 계정 ID |
| email | String | ✓ | Google 이메일 |
| access_token | String | ✓ | EncryptedSharedPreferences에 저장 |
| refresh_token | String | ✓ | EncryptedSharedPreferences에 저장 |
| token_expiry | Long | ✓ | 토큰 만료 시각 |

---

## 3. 관계도 (ER)

```
Capture (1) ──── (0..1) Todo
    │           
    ├──── (0..1) Schedule
    │           
    ├──── (0..1) Note ──── (0..1) Folder
    │           
    ├──── (0..*) CaptureTag ──── (1) Tag
    │           
    ├──── (0..*) Entity
    │           
    ├──── (0..*) ClassificationLog
    │
    └──── (0..*) Capture [parent_capture_id → 자식 캡처들]
```

**핵심 제약:**

- Capture 1개에 대해 Todo, Schedule, Note 중 최대 1개만 존재
- classified_type에 따른 파생 엔티티 매핑:
    - TODO → Todo 레코드 존재
    - SCHEDULE → Schedule 레코드 존재
    - NOTES → Note 레코드 존재
    - TEMP → 파생 엔티티 없음
- classified_type=NOTES일 때 note_sub_type은 반드시 존재해야 함 (NOT NULL)
- classified_type≠NOTES일 때 note_sub_type은 null
- 분류 변경 시 기존 파생 엔티티 삭제 + 새 파생 엔티티 생성 (트랜잭션)
- parent_capture_id가 있는 캡처는 source=SPLIT (Phase 2b)

---

## 4. 파생 엔티티 생명주기

### 4.1 생성

| 트리거 | 동작 |
| --- | --- |
| AI 분류 완료 (type=TODO) | Todo 레코드 생성, deadline/tags 설정 |
| AI 분류 완료 (type=SCHEDULE) | Schedule 레코드 생성, start_time/location 설정 |
| AI 분류 완료 (type=NOTES, sub=IDEA) | Note 레코드 생성, folder_id=system-ideas |
| AI 분류 완료 (type=NOTES, sub=INBOX) | Note 레코드 생성, folder_id=system-inbox |
| AI 분류 완료 (type=NOTES, sub=BOOKMARK) | Note 레코드 생성, folder_id=system-bookmarks |
| AI 분류 완료 (type=TEMP) | 파생 엔티티 없음 |

### 4.2 분류 변경 (트랜잭션)

모든 분류 변경은 단일 Room 트랜잭션 내에서 처리.

```kotlin
@Transaction
fun changeClassification(
    captureId: String, 
    newType: ClassifiedType,
    newSubType: NoteSubType? = null
) {
    // 1. 기존 파생 엔티티 삭제
    deleteTodoByCaptureId(captureId)
    deleteScheduleByCaptureId(captureId)  // + Google Calendar 이벤트 삭제 큐잉 (SYNCED이면)
    deleteNoteByCaptureId(captureId)
    
    // 2. Capture 유형 업데이트
    updateCaptureType(captureId, newType, newSubType)
    
    // 3. 새 파생 엔티티 생성
    when (newType) {
        TODO -> insertTodo(...)
        SCHEDULE -> insertSchedule(...)
        NOTES -> insertNote(folderId = when(newSubType) {
            IDEA -> "system-ideas"
            BOOKMARK -> "system-bookmarks"
            INBOX -> "system-inbox"
            USER_FOLDER -> /* 유지 또는 system-inbox */
        })
        TEMP -> { /* no derived entity */ }
    }
    
    // 4. 분류 수정 로그 기록 (Phase 2a)
    insertClassificationLog(...)
}
```

**파생 객체 처리 상세 (기능명세서 2.4):**

| 변경 방향 | 파생 처리 | 추가 UX |
| --- | --- | --- |
| 일정 → 할 일 | Schedule 삭제 + Todo 생성 | SYNCED이면 "캘린더 이벤트도 삭제됩니다" 토스트 |
| 일정 → 노트/아이디어 | Schedule 삭제 + Note 생성 | 동일 |
| 할 일 → 일정 | Todo 삭제 + Schedule 생성 | 사용자 명시적 변경이므로 자동 처리 |
| 할 일 → 노트/아이디어 | Todo 삭제 + Note 생성 | 즉시 처리 |
| 노트/아이디어 → 일정 | Note 삭제 + Schedule 생성 | 사용자 명시적 변경이므로 자동 처리 |
| 노트/아이디어 → 할 일 | Note 삭제 + Todo 생성 | 즉시 처리 |
| 노트 ↔ 아이디어 | note_sub_type만 변경 (INBOX ↔ IDEA) | folder_id 업데이트 |

### 4.3 삭제

**Phase 1 (휴지통 미도입):**

| 단계 | 트리거 | 동작 | 복원 |
| --- | --- | --- | --- |
| Soft Delete | 사용자 삭제 동작 | is_deleted=true, deleted_at=now. UI에서 즉시 제거 | Snackbar "실행 취소" (3초 이내) |
| Hard Delete | Snackbar 만료 | Capture + 모든 파생 엔티티 삭제 | 불가 |

**Phase 2a (휴지통 도입):**

| 단계 | 트리거 | 동작 | 복원 |
| --- | --- | --- | --- |
| Soft Delete | 사용자 삭제 동작 | is_deleted=true, deleted_at=now. UI에서 즉시 제거 | Snackbar "실행 취소" (3초 이내) |
| Trash | Snackbar 만료 | is_deleted=false, is_trashed=true, trashed_at=now | 휴지통에서 복원 버튼 (30일 이내) |
| Hard Delete | 30일 경과 또는 휴지통 비우기 | Capture + 모든 파생 엔티티 삭제 | 불가 |

**Hard Delete 범위:** Capture + Todo/Schedule/Note + CaptureTag + Entity + ClassificationLog. Schedule이 SYNCED 상태이면 Google Calendar 이벤트 삭제 요청 큐잉.

**자동 정리:** WorkManager 일간 작업으로 trashed_at + 30일 경과 항목 Hard Delete.

### 4.4 멀티 인텐트 분할 (Phase 2b)

| 단계 | 동작 |
| --- | --- |
| 1 | AI가 원문에서 N개 의도 식별 |
| 2 | 원본 Capture 보존 (original_text 유지, 원본의 classified_type은 AI 판단에 따라 설정) |
| 3 | 각 의도별 개별 Capture 생성 (source=SPLIT, parent_capture_id=원본 ID) |
| 4 | 각 개별 Capture에 대해 독립적 분류 및 파생 엔티티 생성 |

---

## 5. 전문 검색 인덱싱 (Phase 1)

Room FTS4를 사용한 전문 검색 테이블.

### 5.1 CaptureSearch (FTS 가상 테이블)

| 필드 | 소스 | 설명 |
| --- | --- | --- |
| capture_id | captures.id | 원본 Capture 참조 |
| title_text | captures.ai_title | AI 제목 |
| original_text | captures.original_text | 원문 |
| tag_text | GROUP_CONCAT(tags.name) | 태그 합산 텍스트 |
| entity_text | GROUP_CONCAT(entities.value) | 엔티티 합산 텍스트 |

**검색 우선순위:** title_text > tag_text > entity_text > original_text

**검색 범위:** 전체 캡처 대상 (노트뿐 아니라 할 일, 일정 포함). 노트 메인 화면 검색 아이콘(🔍)에서 진입.

---

## 6. 서버 측 스키마

서버는 상태를 영구 저장하지 않는 처리 파이프라인이다. Phase 3a의 인증·구독 도입 전까지 서버 DB는 분석 데이터 저장에만 사용한다.

### 6.1 analytics_events (서버 PostgreSQL)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | UUID | PK |
| device_id | String | 익명 디바이스 식별자 |
| event_type | String | 이벤트 유형 |
| event_data | JSONB | 이벤트 데이터 |
| timestamp | Timestamp | 이벤트 발생 시각 |
| received_at | Timestamp | 서버 수신 시각 |

### 6.2 google_device_tokens (Phase 2a — 서버 PostgreSQL)

디바이스 ID 기준 Google Calendar OAuth 토큰 저장 테이블.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| device_id | String | PK. 디바이스 식별자 |
| access_token | String | Google 액세스 토큰 |
| refresh_token | String? | Google 리프레시 토큰 |
| token_expiry | Timestamp? | 액세스 토큰 만료 시각 |
| created_at | Timestamp | 최초 저장 시각 |
| updated_at | Timestamp | 마지막 갱신 시각 |

### 6.3 users (Phase 3a — 서버 PostgreSQL)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | UUID | PK |
| google_id | String | Google 계정 ID (UNIQUE) |
| email | String | 이메일 |
| subscription_tier | Enum | FREE, PREMIUM |
| subscription_expires_at | Timestamp? | 구독 만료일 |
| created_at | Timestamp | 가입일 |

---

## 7. 마이그레이션 전략

Room DB 스키마 변경 시 마이그레이션을 제공하여 데이터 손실을 방지한다.

| 버전 | 변경 | Phase |
| --- | --- | --- |
| 1 | 초기 스키마: captures (classified_type=TEMP/SCHEDULE/TODO/NOTES, note_sub_type, is_confirmed, confirmed_at 포함. 휴지통 필드 미포함), todos, schedules, notes, folders (system-inbox/ideas/bookmarks), tags, capture_tags, entities, sync_queue, user_preferences, capture_search FTS | 1 |
| 2 | captures에 is_trashed, trashed_at 추가 (휴지통 도입). classification_logs, analytics_events 테이블 추가. schedules에 calendar_sync_status, google_event_id 추가. captures에 image_uri 추가. todos에 deadline_source, sort_source 추가 | 2a |
| 3 | captures에 parent_capture_id 추가. source enum에 SPLIT 추가. notes에 body 추가 | 2b |
| 4 | google_auth 테이블 추가 | 3a |

---

*Document Version: 2.1 | Last Updated: 2026-02-07*
