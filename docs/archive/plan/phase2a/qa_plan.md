# Phase 1 + 2a QA 계획서

> **버전**: v1.0
> **작성일**: 2026-02-09
> **대상 범위**: Phase 1 (MVP) + Phase 2a (코어 루프 완성)
> **기준 브랜치**: `phase2-qa1`
> **현재 테스트**: 112개 유닛 테스트 PASSED

---

## 1. QA 목표

Phase 1 + 2a에서 구현된 모든 기능이 기능명세서(v2.3) 기준으로 정상 동작하는지 검증한다.

**범위 제외**: Phase 2b 이후 구현 예정 기능 (멀티인텐트 분할, 히스토리/검색 필터 강화, 할일 위젯 4×2 등)

---

## 2. QA 영역 개요

| # | 영역 | 우선순위 | 항목 수 | 상태 |
|---|------|:---:|:---:|:---:|
| A | 테스트 커버리지 갭 보충 | P0 | 7건 | 미착수 |
| B | 핵심 플로우 검증 | P0 | 6건 | 미착수 |
| C | Worker & 백그라운드 작업 | P1 | 5건 | 미착수 |
| D | 화면별 기능 검증 | P1 | 11건 | 미착수 |
| E | 에러/엣지 케이스 | P2 | 5건 | 미착수 |
| F | 아키텍처 이슈 | P2 | 3건 | 미착수 |

---

## 3. [A] 테스트 커버리지 갭 보충 (P0)

### A-1. 테스트 없는 ViewModel (2건)

현재 9개 ViewModel 중 2개에 테스트가 없다.

| ViewModel | 담당 화면 | 핵심 검증 항목 |
|-----------|----------|--------------|
| `TrashViewModel` | 휴지통 | restoreItem, deleteItem, emptyTrash, 초기 로딩 |
| `AIStatusSheetViewModel` | AI 분류 확인 시트 | confirmClassification, changeClassification, confirmAll, 24시간 내 미확인 로딩 |

**검증 기준**: 각 ViewModel의 모든 public 메서드에 대해 happy path + 에러 케이스 테스트 존재

### A-2. 테스트 없는 UseCase — 실제 로직 보유 (5건)

| UseCase | 로직 요약 | 핵심 검증 항목 |
|---------|----------|--------------|
| `ToggleTodoCompletionUseCase` | 완료 전환 시 타임스탬프 계산 + 이벤트 추적 | incomplete→complete 시 이벤트 발행, complete→incomplete 시 이벤트 미발행 |
| `ReorderTodoUseCase` | 리스트 순회 + sortOrder 업데이트 | 3개 항목 순서 변경 → 각각 올바른 sortOrder + SortSource.USER |
| `EmptyTrashUseCase` | 전체 휴지통 하드 삭제 | 휴지통 5개 → 5회 HardDeleteCaptureUseCase 호출 |
| `ApproveCalendarSuggestionUseCase` | schedule + capture 조회 → 동기화 | startTime 존재 시 syncToCalendar 호출, startTime 없으면 에러 |
| `SetCalendarSettingsUseCase` | 모드 유효성 검증 | "auto"/"suggest" 정상, 잘못된 값 거부 |

**검증 기준**: 각 UseCase의 분기 로직별 최소 1개 테스트

---

## 4. [B] 핵심 플로우 검증 (P0)

### B-1. 캡처 → 분류 → 파생 엔티티 파이프라인

```
검증 단계:
1. 텍스트 입력 → 전송 → TEMP 캡처 저장 확인
2. SyncQueue에 CLASSIFY 작업 enqueue 확인
3. ClassifyCaptureWorker 트리거 → API 호출 (Mock)
4. 분류 결과 반영: classifiedType, aiTitle, confidence 업데이트
5. 파생 엔티티 자동 생성 (TODO→Todo, SCHEDULE→Schedule, NOTES→Note)
6. FTS 인덱스 동기화 (CaptureSearchFts 업데이트)
7. 태그/엔티티 추출 결과 저장
```

**합격 기준**: 캡처 전송 후 분류 결과가 올바른 파생 엔티티를 생성하고, FTS 검색에 노출됨

### B-2. 분류 변경 캐스케이드

```
검증 시나리오:
1. Schedule → Todo: 캘린더 이벤트 삭제 → Todo 생성 → ClassificationLog 기록
2. Todo → Schedule: Todo 삭제 → Schedule 생성 → 캘린더 동기화 트리거
3. Notes → Todo: Note 삭제 → Todo 생성
4. Notes → Schedule: Note 삭제 → Schedule 생성
5. Todo → Notes: Todo 삭제 → Note 생성 (INBOX 폴더)
6. Schedule → Notes: Schedule 삭제 → Note 생성 (INBOX 폴더)
7. 아무 타입 → TEMP: 기존 파생 삭제, 새 파생 미생성
8. Note ↔ Idea: note_sub_type 스왑 + folder_id 변경 (파생 삭제/재생성 없음)
```

**합격 기준**: 분류 변경 후 이전 파생 엔티티가 완전 삭제되고, 새 파생 엔티티가 올바르게 생성됨. @Transaction으로 원자적 실행.

### B-3. 3단계 삭제 모델

```
검증 시나리오:
1. 소프트 삭제: isDeleted=true 설정 → UI에서 즉시 제거
2. Undo (3초 이내): isDeleted=false 복원 → UI에 다시 표시
3. 휴지통 이동: isTrashed=true, trashedAt 기록
4. 휴지통 복원: isTrashed=false, trashedAt 초기화
5. 영구 삭제: capture + todo/schedule/note/tag/image 모두 삭제
6. 휴지통 비우기: 모든 휴지통 항목 영구 삭제
7. 자동 정리: 30일 초과 항목 TrashCleanupWorker가 삭제
```

**합격 기준**: 각 단계에서 데이터 상태가 올바르고, 삭제 시 파생 엔티티와 이미지까지 완전 정리됨

### B-4. Google Calendar 동기화 상태 머신

```
검증 시나리오:
1. 캘린더 비활성 → 동기화 시도 안함 (NOT_LINKED 유지)
2. 자동 모드 + HIGH confidence → SYNCED (알림: 자동 추가)
3. 자동 모드 + LOW confidence → SUGGESTION_PENDING (알림: 제안)
4. 제안 모드 → 항상 SUGGESTION_PENDING
5. SUGGESTION_PENDING → 승인 → SYNCED
6. SUGGESTION_PENDING → 거부 → REJECTED
7. 동기화 API 실패 → SYNC_FAILED
8. SYNC_FAILED → CalendarSyncWorker 재시도 → 성공 시 SYNCED
9. startTime 없는 일정 → 동기화 건너뜀
```

**합격 기준**: 모든 상태 전이가 기능명세서의 상태 머신과 일치

### B-5. FTS 검색

```
검증 시나리오:
1. 제목으로 검색 → 결과 반환
2. 태그로 검색 → 결과 반환
3. 원본 텍스트로 검색 → 결과 반환
4. 빈 검색어 → 빈 결과 (쿼리 미실행)
5. 캡처 수정 후 → FTS 인덱스 갱신 → 수정된 내용으로 검색 가능
6. 캡처 삭제 후 → 검색 결과에서 제외
```

**합격 기준**: FTS 인덱스가 캡처 생명주기와 동기화되어 있고, 검색 결과가 정확함

### B-6. 이미지 첨부

```
검증 시나리오:
1. 갤러리에서 이미지 선택 → URI 저장 → 캡처에 연결
2. 이미지 미리보기 표시 → 제거 버튼 동작
3. 이미지 첨부 캡처 전송 → 내부 저장소에 파일 복사
4. 캡처 영구 삭제 시 → 이미지 파일도 삭제
5. 이미지만 첨부 (텍스트 없음) → TEMP 분류로 저장
```

**합격 기준**: 이미지가 내부 저장소에 안전하게 저장되고, 캡처 삭제 시 함께 정리됨

---

## 5. [C] Worker & 백그라운드 작업 (P1)

### C-1. ReclassifyTempWorker (15분 주기)

```
검증 항목:
- TEMP 캡처 존재 시 → /classify/batch API 호출
- 분류 결과 → 각 캡처에 ProcessClassificationResultUseCase 적용
- TEMP 캡처 없으면 → 조기 반환 (API 미호출)
- 네트워크 연결 필요 (Constraints.CONNECTED)
```

### C-2. TrashCleanupWorker (1일 주기)

```
검증 항목:
- 30일 초과 휴지통 항목 → HardDeleteCaptureUseCase로 삭제
- 30일 이내 항목 → 유지
- 빈 휴지통 → 조기 반환
```

### C-3. AnalyticsBatchWorker (1시간 주기)

```
검증 항목:
- 미전송 이벤트 100개씩 배치 → /analytics/events POST
- 전송 성공 → markSynced 처리
- 7일 이상 오래된 이벤트 → deleteOld
- 미전송 이벤트 없으면 → 조기 반환
```

### C-4. CalendarSyncWorker (1시간 주기)

```
검증 항목:
- SYNC_FAILED 상태 일정 조회 → 재시도
- 재시도 성공 → SYNCED로 업데이트
- 재시도 실패 → SYNC_FAILED 유지
```

### C-5. SyncQueue 지수 백오프

```
검증 항목:
- 1차 실패: 5초 후 재시도
- 2차 실패: 15초 후 재시도
- 3차 실패: 45초 후 재시도 → 이후 FAILED
- 앱 킬 시 PROCESSING → PENDING 리셋 (resetProcessingToPending)
```

---

## 6. [D] 화면별 기능 검증 (P1)

### D-1. 홈(캡처) 화면

| 항목 | 검증 내용 |
|------|----------|
| 텍스트 입력 | 타이핑 → 실시간 반영, 5000자 제한 |
| 이미지 첨부 | 갤러리 선택 → 미리보기 표시 → 제거 가능 |
| 전송 | 텍스트 또는 이미지 있으면 활성화, 로딩 상태, 성공 시 초기화 |
| 드래프트 | 앱 백그라운드 시 자동 저장, 복귀 시 복원 |
| 알림 벨 | 미확인 분류 개수 배지, 탭 시 AI 상태 시트 열림 |
| 상단 아이콘 | 히스토리/설정 네비게이션 |

### D-2. AI 분류 확인 시트

| 항목 | 검증 내용 |
|------|----------|
| 로딩 | 24시간 이내 미확인 캡처 목록 |
| 개별 확인 | 분류 승인 → isConfirmed=true |
| 분류 변경 | 타입 드롭다운 → ChangeClassificationUseCase |
| 전체 확인 | confirmAll → 모든 미확인 일괄 승인 |

### D-3. 캘린더 화면

| 항목 | 검증 내용 |
|------|----------|
| 월/주 전환 | 확장 토글 → 레이아웃 변경 |
| 날짜 선택 | 탭 → 해당 날짜 일정/할일 로딩 |
| 월 네비게이션 | 스와이프 → 이전/다음 달 |
| 일정 목록 | ScheduleTimeline에 시간순 표시 |
| 할일 목록 | TaskList에 체크박스 + 마감일 표시 |
| 할일 체크 | 즉시 UI 반영 + 지연된 DB 업데이트 |
| 동기화 승인/거부 | SUGGESTION_PENDING 일정에 버튼 표시, 동작 확인 |
| 스와이프 삭제 | SwipeableCard → 소프트 삭제 + Snackbar undo |

### D-4. 노트 화면

| 항목 | 검증 내용 |
|------|----------|
| 폴더 필터 칩 | "전체" + 시스템 폴더 + 사용자 폴더 표시 |
| 필터 전환 | 칩 탭 → 캐시된 목록에서 필터링 (재쿼리 없음) |
| 폴더 생성 | + 버튼 → 다이얼로그 → 이름 입력 → 생성 |
| 폴더 이름변경 | 사용자 폴더 롱프레스 → 이름변경 다이얼로그 |
| 폴더 삭제 | 사용자 폴더 롱프레스 → 삭제 → 하위 노트 이동 처리 |
| 노트 클릭 | 노트 상세 화면 네비게이션 |
| 빈 상태 | 폴더 필터별 + 전체 빈 상태 메시지 |

### D-5. 노트 상세 화면

| 항목 | 검증 내용 |
|------|----------|
| 로딩 | noteId로 노트 + 캡처 정보 로드 |
| 제목 편집 | 타이핑 → hasChanges=true → 저장 버튼 표시 |
| 본문 편집 | 타이핑 → hasChanges=true → 저장 버튼 표시 |
| 폴더 변경 | 드롭다운 → 폴더 선택 → hasChanges=true |
| 저장 | 변경 내용 persist → 로딩 상태 → 성공 |
| 이미지 표시 | imageUri 있으면 이미지 표시 |

### D-6. 캡처 상세 화면

| 항목 | 검증 내용 |
|------|----------|
| 분류 칩 | 4개 (할일/일정/노트/아이디어) 표시, 현재 타입 선택됨 |
| 분류 변경 | 칩 탭 → ChangeClassificationUseCase → 캐스케이드 |
| 캘린더 동기화 | SUGGESTION_PENDING 시 승인/거부 버튼 |
| 원본 텍스트 | 읽기 전용 표시 |
| 이미지 | imageUri 있으면 표시 |

### D-7. 히스토리 화면

| 항목 | 검증 내용 |
|------|----------|
| 무한 스크롤 | 20개 페이징, 끝 근처 스크롤 시 loadMore |
| 타입 필터 | 전체/일정/할일/노트 칩 |
| 날짜 필터 | 전체/오늘/이번주/이번달 칩 |
| 스와이프 삭제 | SwipeableCard → 소프트 삭제 + Snackbar undo |
| 인라인 분류 변경 | 드롭다운 → changeClassification |

### D-8. 검색 화면

| 항목 | 검증 내용 |
|------|----------|
| 자동 포커스 | 화면 진입 시 입력창 포커스 |
| 실시간 검색 | 타이핑 → 즉시 결과 갱신 |
| 타입 필터 | FilterChipRow로 타입 필터링 |
| 결과 클릭 | 캡처 상세 네비게이션 |
| 빈 상태 | 검색 전 / 결과 없음 구분 메시지 |

### D-9. 휴지통 화면

| 항목 | 검증 내용 |
|------|----------|
| 목록 | 휴지통 항목 + 삭제 시점 표시 |
| 복원 | 개별 복원 → isTrashed=false |
| 영구 삭제 | 개별 하드 삭제 (파생 + 이미지 포함) |
| 전체 비우기 | 확인 다이얼로그 → 전체 하드 삭제 |
| 빈 상태 | "휴지통이 비어 있습니다" |

### D-10. 설정 화면

| 항목 | 검증 내용 |
|------|----------|
| 테마 | 시스템/라이트/다크 라디오 선택 → 즉시 반영 |
| 캘린더 토글 | 활성화/비활성화 → UserPreference 저장 |
| 캘린더 모드 | 자동/제안 선택 → 동작 방식 변경 |
| 알림 토글 | 활성화/비활성화 |
| 법적 문서 | 개인정보처리방침/이용약관 WebView |
| 앱 버전 | 올바른 버전 번호 표시 |

### D-11. 온보딩 화면

| 항목 | 검증 내용 |
|------|----------|
| 페이지 네비게이션 | 4페이지 HorizontalPager + 페이지 인디케이터 |
| 건너뛰기 | 텍스트 탭 → 홈 화면 이동 |
| 첫 캡처 | 4페이지에서 텍스트 입력 → 전송 → 홈 이동 |
| Google 연결 | 3페이지 연결 버튼 (현재 구현 연기 상태 — UI만 확인) |
| 완료 표시 | onboardingCompleted=true → 재방문 시 건너뜀 |

---

## 7. [E] 에러/엣지 케이스 (P2)

### E-1. 네트워크 실패

```
검증 시나리오:
- 분류 API 실패 → TEMP 유지, SyncQueue 재시도
- 캘린더 동기화 실패 → SYNC_FAILED, 에러 메시지 표시
- Analytics 전송 실패 → 로컬 보관, 다음 배치에서 재시도
```

### E-2. 데이터 정합성

```
검증 시나리오:
- 분류 변경 중 앱 킬 → @Transaction 롤백 확인
- SyncQueue PROCESSING 중 앱 킬 → 재시작 시 PENDING 리셋
- 폴더 삭제 시 해당 폴더 노트 → 받은함(Inbox)으로 이동
- 동시에 같은 캡처 분류 변경 시도 → 데이터 무결성 유지
```

### E-3. 경계값

```
검증 시나리오:
- 텍스트 5000자 정확히 입력 → 저장 성공
- 텍스트 5001자 입력 시도 → 차단
- 빈 텍스트 + 빈 이미지 → 전송 버튼 비활성화
- 폴더명 30자 → 생성 성공
- 폴더명 31자 → 생성 거부
- 중복 폴더명 → 생성 거부
- 빈 폴더명 → 생성 거부
```

### E-4. UI 상태 복원

```
검증 시나리오:
- 화면 회전 → ViewModel 상태 유지
- 백그라운드 → 포그라운드 → 드래프트 복원
- 메모리 부족으로 프로세스 종료 → 재시작 시 정상 동작
```

### E-5. 위젯

```
검증 시나리오:
- 캡처 위젯 탭 → 앱 열림 + 홈 탭 포커스
- 위젯에서 from_widget=true 인텐트 전달 확인
- 앱 미실행 상태에서 위젯 탭 → 정상 앱 시작
```

---

## 8. [F] 아키텍처 이슈 (P2)

### F-1. UpdateNoteUseCase의 CaptureDao 직접 주입

- **현상**: `UpdateNoteUseCase`가 `CaptureDao`를 직접 주입하여 제목 업데이트 수행
- **문제**: Domain → Data 계층 직접 의존, Repository 패턴 우회
- **권장**: `CaptureRepository`를 통한 title 업데이트 메서드 추가

### F-2. deleteCalendarEvent 미연결

- **현상**: `FlitApi`에 `deleteCalendarEvent` 엔드포인트 정의됨
- **문제**: Repository에서 해당 API를 호출하는 메서드가 불명확
- **권장**: 캘린더 이벤트 삭제 플로우 추적 → 누락 시 연결

### F-3. Google OAuth 미완성

- **현상**: `OnboardingViewModel.connectGoogle()` 메서드 존재하나 실제 OAuth 플로우 미구현
- **문제**: 온보딩 3페이지 "Google 캘린더 연결" 버튼이 동작하지 않음
- **현재 상태**: 설정 화면의 디버그 도구로 수동 토큰 입력 가능 (DEBUG 빌드)
- **권장**: Phase 3 서버 연동 시 구현 예정 — 현재는 UI만 존재함을 문서화

---

## 9. 기존 테스트 현황

### 유닛 테스트 (112개 PASSED)

| 카테고리 | 파일 | 테스트 대상 |
|----------|------|-----------|
| **UseCase** | `CaptureUseCasesTest.kt` | SubmitCapture, HardDeleteCapture |
| **UseCase** | `ClassificationUseCasesTest.kt` | ProcessClassification, ChangeClassification, ConfirmClassification, GetUnconfirmed |
| **UseCase** | `SyncScheduleToCalendarUseCaseTest.kt` | 캘린더 동기화 상태 머신 |
| **UseCase** | `FolderUseCasesTest.kt` | CreateFolder, RenameFolder |
| **ViewModel** | `CaptureViewModelTest.kt` | 캡처 입력/전송/드래프트 |
| **ViewModel** | `CalendarViewModelTest.kt` | 날짜 선택/일정 로딩/동기화 |
| **ViewModel** | `NotesViewModelTest.kt` | 폴더 필터/CRUD |
| **ViewModel** | `NoteDetailViewModelTest.kt` | 노트 상세 로딩/편집/저장 |
| **ViewModel** | `CaptureDetailViewModelTest.kt` | 분류 변경/동기화 UI |
| **ViewModel** | `HistoryViewModelTest.kt` | 페이징/필터/삭제 |
| **ViewModel** | `SearchViewModelTest.kt` | 실시간 검색/타입 필터 |
| **ViewModel** | `OnboardingViewModelTest.kt` | 온보딩 플로우 |
| **ViewModel** | `SettingsViewModelTest.kt` | 테마/캘린더/알림 설정 |
| **유틸** | `MainDispatcherRule.kt` | 코루틴 테스트 디스패처 |
| **유틸** | `TestFixtures.kt` | 8개 도메인 모델 팩토리 |

### 테스트 품질 평가

| 영역 | 등급 | 비고 |
|------|:---:|------|
| 도메인 로직 (분류/동기화) | A+ | 분기 커버리지 높음, 멀티인텐트 시나리오 포함 |
| ViewModel happy path | B+ | 초기 로딩/기본 동작 검증 |
| 에러 시나리오 | D | 네트워크 실패/null 반환 테스트 거의 없음 |
| 통합 테스트 | N/A | Room/Worker 통합 테스트 없음 (계획 외) |

---

## 10. QA 실행 계획

### 1단계: 테스트 보충 (A 영역)

```
1. TrashViewModel 테스트 작성
   → verify: 모든 public 메서드 커버
2. AIStatusSheetViewModel 테스트 작성
   → verify: confirm/change/confirmAll 커버
3. ToggleTodoCompletionUseCase 테스트 작성
   → verify: 타임스탬프 계산 + 이벤트 분기
4. ReorderTodoUseCase 테스트 작성
   → verify: sortOrder 할당 + SortSource
5. EmptyTrashUseCase 테스트 작성
   → verify: N건 일괄 삭제 오케스트레이션
6. ApproveCalendarSuggestionUseCase 테스트 작성
   → verify: startTime 유무 분기
7. SetCalendarSettingsUseCase 테스트 작성
   → verify: 모드 유효성 검증
```

### 2단계: 핵심 플로우 수동 검증 (B 영역)

```
1. 캡처→분류 E2E 플로우
   → verify: TEMP→분류 완료→파생 엔티티 존재
2. 분류 변경 8가지 시나리오
   → verify: 이전 파생 삭제 + 새 파생 생성
3. 삭제 3단계 전체 플로우
   → verify: soft→trash→hard 데이터 상태
4. 캘린더 동기화 9가지 상태 전이
   → verify: 상태 머신 일치
5. FTS 검색 + 인덱스 동기화
   → verify: CRUD 후 검색 결과 정확
6. 이미지 첨부 + 삭제 시 정리
   → verify: 내부 저장소 파일 라이프사이클
```

### 3단계: 화면별 + 엣지 케이스 (C/D/E 영역)

```
1. 각 화면별 체크리스트 수동 검증
2. Worker 동작 검증 (로그 확인)
3. 에러/경계값 시나리오 검증
```

### 4단계: 아키텍처 이슈 수정 (F 영역)

```
1. UpdateNoteUseCase DAO 직접 주입 → Repository 경유로 리팩터링
2. deleteCalendarEvent 연결 여부 확인 + 필요 시 연결
3. OAuth 미완성 상태 문서화
```

---

## 부록: 관련 문서

| 문서 | 경로 |
|------|------|
| 기능명세서 v2.3 | `docs/specs/functional_spec.md` |
| 데이터모델 명세서 v2.0 | `docs/specs/data_model_spec.md` |
| API 명세서 v2.4 | `docs/specs/api_spec.md` |
| 디자인 가이드 v1.0 | `docs/specs/design-guide.md` |
| UX 라이팅 감사 v1.0 | `docs/specs/ux-writing-audit.md` |
| Phase 2a 구현 보고서 | `docs/plan/phase2a/implementation_report.md` |
| UI 개선 보고서 | `docs/analysis/ui-improvement-report.md` |
