# Phase 2b 구현 계획: 사용성 확장 (최소 출시 기준)

> **작성일**: 2026-02-07
> **브랜치**: `feature/phase2`
> **상태**: 계획 수립 완료
> **선행 조건**: Phase 2a 완료 (81개 파일, 96개 유닛 테스트 통과)

---

## 1. 개요

Phase 2b는 멀티 인텐트 분할, 노트 편집, 할일 위젯, 히스토리/검색 필터, 관측 지표 확장을 구현하여 최소 출시 기준을 달성하는 단계이다.

---

## 2. 현재 상태 확인

| 항목 | 상태 | 비고 |
|------|------|------|
| `CaptureSource.SPLIT` | ✅ 존재 | 도메인 모델에 이미 정의됨 |
| `CaptureEntity.parent_capture_id` | ❌ 미존재 | 2b-0에서 추가 |
| `NoteEntity.body` | ❌ 미존재 | 2b-0에서 추가 |
| `Capture.parentCaptureId` / `Note.body` 도메인 모델 | ❌ 미존재 | 2b-0에서 추가 |
| `Classification.splitItems` / `ClassifyResponse.splitItems` | ❌ 미존재 | 2b-0에서 추가 |
| `FolderRepositoryImpl.deleteFolder` Inbox 이동 | ✅ 구현됨 | 트랜잭션 처리 완료 |
| `CreateFolderUseCase` 이름 최대 길이 검증 | ❌ 미검증 | 2b-3에서 추가 (30자) |
| 위젯 기반 | RemoteViews | `CaptureWidgetProvider`는 Glance 아님 |
| FTS 검색 | FTS4 MATCH + captures JOIN | 필터 없음 |

---

## 3. 서브페이즈 구성 (9단계)

### 2b-0: DB v13 스키마 + 전 레이어 기반 코드

**목표**: Entity/Model/Mapper/DTO에 새 필드 추가

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/local/database/FlitDatabase.kt` | 수정 | version 12→13 |
| `data/local/database/entities/CaptureEntity.kt` | 수정 | `parent_capture_id` (String?) + Index 추가 |
| `data/local/database/entities/NoteEntity.kt` | 수정 | `body` (String?) 추가 |
| `domain/model/Capture.kt` | 수정 | `parentCaptureId: String? = null` 추가 |
| `domain/model/Note.kt` | 수정 | `body: String? = null` 추가 |
| `data/mapper/CaptureMapper.kt` | 수정 | `parentCaptureId` 매핑 |
| `data/mapper/NoteMapper.kt` | 수정 | `body` 매핑 |
| `domain/model/Classification.kt` | 수정 | `splitItems: List<SplitItem>? = null` 추가 |
| `domain/model/SplitItem.kt` | 신규 | 분할 아이템 도메인 모델 |
| `data/remote/dto/v2/ClassifyResponse.kt` | 수정 | `splitItems: List<SplitItemDto>?` 추가 |
| `data/remote/dto/v2/SplitItemDto.kt` | 신규 | 분할 DTO |
| `data/debug/MockDataInitializer.kt` | 수정 | 새 필드 반영 |
| `test/util/TestFixtures.kt` | 수정 | 새 필드 반영 |

**검증**: `compileDebugKotlin` 통과 + 기존 96개 테스트 통과

---

### 2b-1: 멀티 인텐트 분할

**목표**: AI 분류 시 다중 의도 감지 → 개별 Capture 생성

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/remote/api/MockFlitApi.kt` | 수정 | "~하고", "~그리고" 패턴 감지 → `splitItems` 응답 생성 |
| `data/local/database/dao/CaptureDao.kt` | 수정 | `getByParentCaptureId(parentId)` 쿼리 추가 |
| `domain/repository/CaptureRepository.kt` | 수정 | `getChildCaptures(parentId)` 추가 |
| `data/repository/CaptureRepositoryImpl.kt` | 수정 | 위 메서드 구현 |
| `domain/usecase/classification/ProcessClassificationResultUseCase.kt` | 수정 | splitItems 존재 시 자식 Capture 생성 로직 추가 |
| `data/worker/ClassifyCaptureWorker.kt` | 수정 | splitItems 응답 파싱 후 ProcessClassificationResult에 전달 |

**핵심 로직** (ProcessClassificationResultUseCase):
```
if (classification.splitItems != null) {
    for (splitItem in classification.splitItems) {
        1. 새 Capture 생성 (source=SPLIT, parentCaptureId=captureId)
        2. invoke(newCaptureId, splitItemClassification) // 재귀적 파생 엔티티 생성
    }
}
```

**검증**: split 패턴 입력 → 자식 Capture 생성 확인, 단일 의도 입력 → 기존 동작 유지

---

### 2b-2: 노트 상세 보기/편집

**목표**: NoteDetailScreen 신규 화면 — 제목/본문/태그/폴더 편집

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/local/database/dao/NoteDao.kt` | 수정 | `NoteWithCaptureRow`에 `body` 필드 추가, `updateBody()` 쿼리 추가 |
| `domain/repository/NoteRepository.kt` | 수정 | `getNoteDetail(noteId)`, `updateNoteBody(noteId, body)` 추가 |
| `data/repository/NoteRepositoryImpl.kt` | 수정 | 위 메서드 구현 |
| `domain/usecase/note/GetNoteDetailUseCase.kt` | 신규 | 노트 + 캡처 + 태그 조합 조회 |
| `domain/usecase/note/UpdateNoteUseCase.kt` | 신규 | 제목(ai_title), 본문(body), 태그, 폴더 업데이트 |
| `presentation/notes/detail/NoteDetailUiState.kt` | 신규 | UI 상태 정의 |
| `presentation/notes/detail/NoteDetailViewModel.kt` | 신규 | 편집 상태 추적, 저장, 변경 감지 |
| `presentation/notes/detail/NoteDetailScreen.kt` | 신규 | Compose UI (제목/본문/태그/분류/폴더/원본/날짜) |
| `navigation/NavGraph.kt` | 수정 | `NOTE_DETAIL` route 추가 |
| `presentation/notes/NotesScreen.kt` | 수정 | 노트 아이템 탭 → NoteDetail 네비게이션 |

**UI 구성**:
- TopBar: ← / 삭제 / [저장] (변경사항 있을 때만 활성화)
- 제목 (편집 가능 TextField)
- 분류 칩 + 태그 (편집 가능)
- 본문 (편집 가능 TextField, body가 null이면 original_text 표시)
- 원본 텍스트 (접힘 가능, 읽기 전용)
- 소속 폴더 (탭 → 폴더 선택 바텀시트)
- 작성 날짜

**검증**: 편집→저장→DB 반영, 변경사항 있을 때 뒤로가기 시 확인 다이얼로그

---

### 2b-3: 폴더 이름 검증 강화

**목표**: 폴더 이름 최대 30자 제한 추가

| 파일 | 변경 | 내용 |
|------|------|------|
| `domain/usecase/folder/CreateFolderUseCase.kt` | 수정 | `require(name.length <= 30)` 추가 |
| `domain/usecase/folder/RenameFolderUseCase.kt` | 수정 | `require(newName.length <= 30)` 추가 |

> 폴더 삭제 시 Inbox 이동은 `FolderRepositoryImpl.deleteFolder`에 이미 구현됨.
> 폴더 이동은 2b-2 NoteDetailScreen에서 구현.

**검증**: 31자 이름 입력 시 에러, 기존 폴더 테스트 통과

---

### 2b-4: 히스토리 필터링

**목표**: 분류 유형 칩 + 날짜 범위 필터

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/local/database/dao/CaptureDao.kt` | 수정 | `getFilteredCapturesPaged(type?, startDate?, endDate?, limit, offset)` 추가 |
| `domain/repository/CaptureRepository.kt` | 수정 | `getFilteredCaptures(...)` 추가 |
| `data/repository/CaptureRepositoryImpl.kt` | 수정 | 필터 쿼리 구현 |
| `domain/usecase/capture/GetFilteredCapturesUseCase.kt` | 신규 | 필터 파라미터 기반 캡처 조회 |
| `presentation/history/HistoryUiState.kt` | 수정 | `selectedType`, `startDate`, `endDate` 필드 추가 |
| `presentation/history/HistoryViewModel.kt` | 수정 | 필터 핸들러, 필터 적용 시 `getFilteredCapturesUseCase` 사용 |
| `presentation/history/HistoryScreen.kt` | 수정 | 필터 칩 행 + 날짜 범위 UI 추가 |
| `presentation/components/common/FilterChipRow.kt` | 신규 | 재사용 가능한 분류 유형 칩 컴포넌트 |

**DAO 쿼리 전략**: nullable 파라미터 조건부 쿼리 사용
```sql
WHERE is_deleted = 0 AND is_trashed = 0
  AND (:type IS NULL OR classified_type = :type)
  AND (:startDate IS NULL OR created_at >= :startDate)
  AND (:endDate IS NULL OR created_at <= :endDate)
ORDER BY created_at DESC LIMIT :limit OFFSET :offset
```

**검증**: 필터 조합 → 올바른 결과, 초기화 → 전체 표시

---

### 2b-5: 검색 필터 확장

**목표**: 검색 결과에 분류 유형 + 날짜 범위 필터

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/local/database/dao/CaptureSearchDao.kt` | 수정 | `searchFiltered(query, type?, startDate?, endDate?)` 추가 |
| `domain/repository/CaptureRepository.kt` | 수정 | `searchCapturesFiltered(...)` 추가 |
| `data/repository/CaptureRepositoryImpl.kt` | 수정 | 필터 FTS 검색 구현 |
| `domain/usecase/search/SearchCapturesUseCase.kt` | 수정 | 필터 파라미터 추가 (기본값 null) |
| `presentation/search/SearchUiState.kt` | 수정 | `selectedType`, `startDate`, `endDate` 추가 |
| `presentation/search/SearchViewModel.kt` | 수정 | 필터 핸들러, 필터 적용 검색 |
| `presentation/search/SearchScreen.kt` | 수정 | FilterChipRow 재사용, 날짜 범위 UI |

**FTS 필터 쿼리**:
```sql
SELECT c.* FROM captures c
INNER JOIN capture_search cs ON c.id = cs.capture_id
WHERE capture_search MATCH :query
AND c.is_deleted = 0 AND c.is_trashed = 0
AND (:type IS NULL OR c.classified_type = :type)
AND (:startDate IS NULL OR c.created_at >= :startDate)
AND (:endDate IS NULL OR c.created_at <= :endDate)
ORDER BY c.created_at DESC LIMIT :limit
```

**검증**: FTS 검색 + 필터 조합 동작 확인

---

### 2b-6: 할 일 위젯 (4x2)

**목표**: 오늘 마감 할일 표시 + 체크박스 완료 처리

| 파일 | 변경 | 내용 |
|------|------|------|
| `data/local/database/dao/TodoDao.kt` | 수정 | `getTodayIncompleteTodos(todayEndMs)` 쿼리 추가 |
| `presentation/widget/TodoWidgetProvider.kt` | 신규 | AppWidgetProvider + RemoteViewsService |
| `res/layout/widget_todo.xml` | 신규 | 위젯 레이아웃 (제목 + ListView + 앱열기 버튼) |
| `res/layout/widget_todo_item.xml` | 신규 | 리스트 아이템 (체크박스 + 제목 + 마감시간) |
| `res/xml/widget_todo_info.xml` | 신규 | 위젯 메타데이터 (4x2 최소 크기) |
| `AndroidManifest.xml` | 수정 | TodoWidgetProvider + Service + BroadcastReceiver 등록 |

**핵심 동작**:
- `TodoWidgetProvider.onUpdate()` → Room에서 오늘 마감 할일 조회 → RemoteViews 업데이트
- 체크박스 클릭 → BroadcastReceiver → `TodoDao.toggleCompletion()` → 위젯 갱신
- "앱 열기" → MainActivity 진입

**검증**: 위젯 배치 → 오늘 할일 표시, 체크박스 탭 → 완료 처리 + 목록 갱신

---

### 2b-7: 확장 관측 지표

**목표**: 4개 신규 분석 이벤트 발행

| 이벤트 | 발행 위치 | event_data |
|--------|----------|-----------|
| `capture_revisited` | `CaptureDetailViewModel.init`, `NoteDetailViewModel.init` | `time_since_creation_ms`, `access_method` |
| `todo_completed` | `ToggleTodoCompletionUseCase` (완료 시) | `time_since_creation_ms` |
| `search_performed` | `SearchViewModel.executeSearch` | `result_count`, `result_clicked` |
| `split_capture_created` | `ProcessClassificationResultUseCase` (split 시) | `parent_capture_id`, `split_count` |

| 파일 | 변경 | 내용 |
|------|------|------|
| `presentation/detail/CaptureDetailViewModel.kt` | 수정 | `capture_revisited` 이벤트 |
| `presentation/notes/detail/NoteDetailViewModel.kt` | 수정 | `capture_revisited` 이벤트 |
| `domain/usecase/todo/ToggleTodoCompletionUseCase.kt` | 수정 | 완료 시 `todo_completed` 이벤트 |
| `presentation/search/SearchViewModel.kt` | 수정 | `search_performed` 이벤트 |
| `domain/usecase/classification/ProcessClassificationResultUseCase.kt` | 수정 | split 시 `split_capture_created` 이벤트 |

**검증**: 각 이벤트 트리거 → `analytics_events` 테이블 기록 확인

---

### 2b-8: 테스트 + 통합 검증

**목표**: 신규/변경 UseCase 및 ViewModel 테스트 추가

| 파일 | 변경 | 내용 |
|------|------|------|
| `test/.../NoteDetailViewModelTest.kt` | 신규 | 편집/저장/분류변경/폴더이동 테스트 |
| `test/.../HistoryViewModelTest.kt` | 수정 | 필터 기능 테스트 |
| `test/.../SearchViewModelTest.kt` | 수정 | 필터 기능 테스트 |
| `test/.../CaptureUseCasesTest.kt` | 수정 | split 처리 테스트 |
| `test/.../FolderUseCasesTest.kt` | 수정 | 이름 길이 검증 테스트 |

**검증**: `./gradlew testDebugUnitTest` 전체 통과 + `compileDebugKotlin` 통과

---

## 4. 서브페이즈 의존성

```
2b-0 (DB 기반)
 ├→ 2b-1 (멀티 인텐트)
 ├→ 2b-2 (노트 편집) → 2b-3 (폴더 검증)
 ├→ 2b-4 (히스토리 필터) → 2b-5 (검색 필터, FilterChipRow 재사용)
 └→ 2b-6 (할일 위젯)

2b-1 + 2b-2 + 2b-5 → 2b-7 (관측 지표)
전체 → 2b-8 (테스트)
```

---

## 5. 멀티 에이전트 전략 (4인 팀)

### Wave 1: 2b-0 (단독, data 에이전트)
- Entity/Model/Mapper/DTO 변경 → 컴파일 확인

### Wave 2: 최대 병렬 (2b-0 완료 후)
- **data**: 2b-1 (멀티 인텐트 — MockAPI, DAO, Repository, ProcessClassificationResult)
- **domain**: 2b-2 + 2b-3 (노트 편집 — UseCase + ViewModel + Screen)
- **ui**: 2b-4 + 2b-6 (히스토리 필터 + 할일 위젯)
- **qa**: TestFixtures 업데이트 + 기존 테스트 호환성 확인

### Wave 3: 후반 작업
- **ui**: 2b-5 (검색 필터 — FilterChipRow 재사용)
- **data + domain**: 2b-7 (관측 지표 — 각 이벤트 발행 코드)
- **qa**: 2b-8 (전체 테스트)

### 공유 파일 규칙

| 파일 | 담당 |
|------|------|
| `NavGraph.kt` | domain 에이전트 (NOTE_DETAIL route) |
| `FlitDatabase.kt` | data 에이전트 (version 변경) |
| `CaptureDao.kt` | data 에이전트 (filter + childCaptures 쿼리) |
| `CaptureRepository.kt` | data 에이전트 (인터페이스 확장) |
| `TestFixtures.kt` | qa 에이전트 |

---

## 6. 예상 규모

| 구분 | 신규 | 수정 | 합계 |
|------|------|------|------|
| Data | 2 | ~12 | ~14 |
| Domain | ~4 | ~8 | ~12 |
| Presentation | ~7 | ~8 | ~15 |
| Resource | ~4 | ~1 | ~5 |
| Test | ~1 | ~5 | ~6 |
| **합계** | **~18** | **~34** | **~52** |

---

## 7. 검증 방법

1. **단위 검증**: 각 서브페이즈 완료 시 `./gradlew :app:compileDebugKotlin`
2. **테스트**: 최종 `./gradlew testDebugUnitTest` 전체 통과
3. **수동 검증**: 에뮬레이터에서 주요 시나리오 확인
   - 멀티 인텐트 분할: "보고서 쓰고 저녁 예약" 입력 → 2개 캡처 생성
   - 노트 편집: 노트 탭 → 상세 → 본문 편집 → 저장
   - 할일 위젯: 홈 화면 위젯 추가 → 체크박스 탭
   - 히스토리 필터: 유형 칩 + 날짜 필터 조합
   - 검색 필터: FTS 검색 + 필터 조합

---

## 8. 주의사항

- Destructive Migration 유지 (프로덕션 배포 전 Migration 코드 별도 작성)
- `ProcessClassificationResultUseCase` 수정 시 기존 단일 의도 경로 깨지지 않도록 `splitItems == null` 분기 필수
- FTS 필터 쿼리에서 `is_trashed = 0` 조건 추가 필수 (현재 `search()` 메서드에 누락되어 있음)
- 주석/문서는 한글 필수
- 에이전트 타입: Sonnet 모델 사용

---

*Phase 2b 완료 후 → Phase 3 서버 연동 (Mock 삭제 → 실제 API 연결 → 통합 테스트)*
