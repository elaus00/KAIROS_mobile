# Phase 1 UI 분석 보고서

> 작성일: 2026-02-06
> 기준: PRD v10.0, 기능명세서 v2.1, Phase 1 구현 계획

---

## 1. 현재 프레젠테이션 레이어 구조

```
presentation/
├── main/
│   └── MainScreen.kt              # HorizontalPager + KairosBottomNav (3탭)
├── capture/
│   ├── CaptureContent.kt          # 홈 탭 캡처 UI
│   ├── QuickCaptureViewModel.kt   # 캡처 ViewModel (PRD v4.0 기반)
│   ├── QuickCaptureOverlay.kt     # 오버레이 팝업 (삭제 대상)
│   └── QuickCapturePopup.kt       # 팝업 (삭제 대상)
├── calendar/
│   ├── CalendarScreen.kt          # 캘린더 탭 (Screen + Content)
│   ├── CalendarViewModel.kt       # 캘린더 ViewModel
│   ├── CalendarUiState.kt         # 상태
│   └── components/
│       ├── CalendarCard.kt        # 날짜 헤더 + 주간/월간 뷰
│       ├── WeekPicker.kt          # 주간 날짜 선택
│       ├── MonthView.kt           # 월간 뷰
│       ├── ScheduleTimeline.kt    # 일정 타임라인
│       └── TaskList.kt            # 할 일 목록
├── notes/
│   ├── NotesScreen.kt             # 노트 탭 (Screen + Content)
│   ├── NotesViewModel.kt          # 노트 ViewModel
│   ├── NotesUiState.kt            # 상태
│   ├── edit/
│   │   ├── NoteEditScreen.kt      # 노트 편집
│   │   ├── NoteEditViewModel.kt   # 편집 ViewModel
│   │   └── NoteEditUiState.kt     # 편집 상태
│   └── components/
│       ├── FolderChips.kt         # 폴더 칩 (PRD v4.0 구조)
│       ├── NoteCard.kt            # 노트 카드
│       ├── BookmarkCard.kt        # 북마크 카드
│       ├── NotesSearchBar.kt      # 노트 내 검색
│       └── NotesTabRow.kt         # 노트/북마크 탭
├── search/
│   ├── SearchScreen.kt            # 검색 화면
│   ├── SearchViewModel.kt         # 검색 ViewModel
│   └── SearchUiState.kt           # 검색 상태
├── result/
│   ├── ResultScreen.kt            # 분류 결과 화면 (삭제 대상)
│   ├── ResultViewModel.kt         # 결과 ViewModel (삭제 대상)
│   ├── ResultUiState.kt           # 결과 상태 (삭제 대상)
│   └── components/
│       ├── ConfirmResultCard.kt   # 확인 카드 (삭제 대상)
│       ├── AutoSaveResultCard.kt  # 자동저장 카드 (삭제 대상)
│       ├── ResultEditBottomSheet.kt # 수정 시트 (삭제 대상)
│       └── TypeSelectionCard.kt   # 타입 선택 (삭제 대상)
├── notifications/
│   ├── NotificationsScreen.kt     # 알림 화면 (삭제 대상)
│   ├── NotificationsViewModel.kt  # 알림 ViewModel (삭제 대상)
│   └── NotificationsUiState.kt    # 알림 상태 (삭제 대상)
├── settings/
│   ├── SettingsScreen.kt          # 설정 화면
│   ├── SettingsViewModel.kt       # 설정 ViewModel
│   ├── SettingsUiState.kt         # 설정 상태
│   ├── ProfileScreen.kt           # 프로필 (삭제 대상)
│   └── PrivacyPolicyScreen.kt     # 개인정보 처리방침
├── home/
│   ├── HomeScreen.kt              # (이미 삭제됨, git tracked)
│   ├── HomeUiState.kt             # (이미 삭제됨)
│   ├── HomeViewModel.kt           # (이미 삭제됨)
│   └── components/
│       ├── CaptureInputArea.kt    # 입력 영역 (삭제 대상)
│       ├── CaptureGrid.kt         # 캡처 그리드 (삭제 대상)
│       └── AIRecommendationCard.kt # AI 추천 카드 (삭제 대상)
├── components/
│   ├── common/
│   │   ├── KairosBottomNav.kt     # 하단 네비게이션 (재사용)
│   │   ├── KairosChip.kt          # 칩 컴포넌트 (재사용, 수정 필요)
│   │   ├── SwipeableCard.kt       # 스와이프 삭제 카드 (재사용)
│   │   └── SectionHeader.kt       # 섹션 헤더 (재사용)
│   ├── search/
│   │   ├── SearchBar.kt           # 검색 바 (재사용)
│   │   ├── SearchResultCard.kt    # 검색 결과 카드 (수정 필요)
│   │   └── FilterChipRow.kt       # 필터 칩 (수정 필요)
│   ├── notifications/
│   │   └── NotificationCard.kt    # 알림 카드 (삭제 대상)
│   └── settings/
│       └── SwitchPreference.kt    # 설정 스위치 (재사용)
└── navigation/
    └── NavGraph.kt                # 네비게이션 그래프 (수정 필요)
```

---

## 2. PRD v10.0 화면 요구사항과 현재 구현의 GAP 분석

### 2.1 홈 화면 (CaptureContent) — Phase 1-6

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| Top Bar 좌: "Kairos" 타이틀 | O — 구현됨 | 없음 |
| Top Bar 우: 알림 벨(뱃지) + 설정 아이콘 | X — 분류 태그 칩 + 설정 아이콘 | **벨 아이콘 + 뱃지 추가 필요** |
| 날짜 표시 (상단 중앙) | X — 미구현 | **날짜 표시 추가 필요** |
| 메인 영역: 빈 상태 일러스트 | X — 텍스트 입력 영역이 전체 채움 | **빈 상태 디자인 추가 필요** |
| 하단 입력바: 📎 + 텍스트 + ↑ | 부분 — 이미지(Image 아이콘) + 글자수 + 전송 | **📎 아이콘으로 변경, 글자수 표시는 기능명세에 없음** |
| 캡처 완료 시 Snackbar "저장됨" | O — "캡처가 저장되었습니다" | 메시지 간결화 필요 ("저장됨") |
| 입력창 높이 최대 4줄 확장 | X — 무제한 확장 (weight(1f)) | **입력창을 하단 고정으로 전환 필요** |
| 최대 5,000자 제한 | X — 500자 (maxCharacterCount=500) | **5,000자로 변경 필요** |
| TEMP 저장 → SyncQueue 분류 | X — captureRepository.saveCapture 직접 호출 | **SubmitCaptureUseCase 연동 필요** |
| 분류 태그 칩 (입력 중 표시) | O — CaptureTypeChip | **Phase 1에서는 입력 중 분류 불필요 (전송 후 AI 분류)** |

**핵심 GAP:** 현재 CaptureContent는 "입력 영역이 화면 전체를 채우는" 구조인데, PRD v10.0은 "입력바가 하단에 고정되고, 메인 영역은 빈 상태"인 구조. 전면 재작성 필요.

### 2.2 AI 분류 현황 시트 — Phase 1-6

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| 바텀시트 오버레이 (~70% 높이) | X — NotificationsScreen (전체 화면) | **바텀시트로 전면 재작성** |
| 24시간 내 미확인 분류 리스트 | X — 범용 알림 목록 | **분류 전용 데이터 소스 필요** |
| 분류 드롭다운 (4옵션) | X — 미구현 | **ClassificationDropdown 신규 생성** |
| [확인] 버튼 / "전체 확인" | X — 미구현 | **확인 기능 신규 구현** |
| "전체 기록 보기" 링크 | X — 미구현 | **히스토리 화면 연결 필요** |

**핵심 GAP:** 현재 `notifications/` 패키지는 PRD v4.0 기반 범용 알림 시스템. PRD v10.0은 AI 분류 전용 바텀시트로 완전히 다른 컨셉. 전체 삭제 후 `classification/` 패키지로 신규 생성.

### 2.3 전체 히스토리 — Phase 1-7

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| 역시간순 전체 캡처 리스트 | X — 미구현 | **신규 화면** |
| 20건 무한 스크롤 | X — 미구현 | **페이지네이션 구현 필요** |
| 분류 칩 + 태그 + 분류 중 표시 | X — 미구현 | **HistoryItem 컴포넌트 필요** |
| 스와이프 삭제 | SwipeableCard 존재 | 재사용 가능 |

**핵심 GAP:** 완전 신규 화면. `presentation/history/` 패키지 생성 필요.

### 2.4 캘린더 탭 — Phase 1-8

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| "Calendar" 타이틀 | O — 구현됨 | 없음 |
| 주간 캘린더 스트립 | O — WeekPicker 구현됨 | 구조 유사, **도메인 모델 연동 변경 필요** |
| 이벤트 도트 | O — hasSchedule dot | 재사용 가능 |
| 일정 타임라인 (시간+도트+카드) | O — ScheduleTimeline 구현됨 | **도메인 모델(Schedule) 변경에 따라 수정 필요** |
| 할 일 섹션 (체크박스+마감일) | O — TaskList 구현됨 | **도메인 모델(Todo) 변경에 따라 수정 필요** |
| 마감일 표시 형식 (오늘/이후/초과) | X — dueTime 기반 시간만 표시 | **마감일 형식 변경 필요** |
| 완료 시 슬라이드아웃 애니메이션 | X — 즉시 제거 | **애니메이션 추가 필요** |
| TodoPriority 칩 | O — PriorityChip | **삭제 (PRD에서 우선순위 제거됨), 태그 칩으로 대체** |

**핵심 GAP:** 전체 구조는 유사하나 도메인 모델이 완전히 바뀌므로 ViewModel/UiState는 전면 재작성. 컴포넌트(WeekPicker, ScheduleTimeline, TaskList)는 도메인 모델 변경에 맞춰 수정.

### 2.5 노트 탭 — Phase 1-9

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| "노트" 타이틀 + 검색(🔍) 아이콘 | 부분 — "Notes" + 검색 바 (인라인) | **타이틀 한글화, 검색 아이콘으로 변경** |
| 폴더 리스트 (Inbox/Ideas/Bookmarks/사용자) | X — FolderChips (칩 필터) | **폴더 리스트 형태로 전환 필요** |
| 폴더 아이템 (이름 + 노트 수) | X — 칩 필터 | **FolderItem 컴포넌트 신규 생성** |
| [+ 새 폴더] 버튼 | X — 미구현 | **CreateFolderDialog 신규 생성** |
| 노트/북마크 탭 분리 | O — NotesTabRow | **삭제 (PRD v10.0에서 탭 구분 없음, 폴더로 통합)** |
| Inbox 0개이면 숨김 | X — 미구현 | **조건부 표시 로직 필요** |
| 노트 상세 보기 (Phase 2b) | NoteEditScreen 존재 | Phase 1에서는 최소 구현 |

**핵심 GAP:** 현재 노트 화면은 "노트/북마크 탭 + 폴더 칩 필터" 구조인데, PRD v10.0은 "폴더 리스트" 구조. 전면 재작성 필요.

### 2.6 검색 — Phase 1-9

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| 노트 메인에서 🔍 진입 | X — 독립 SearchScreen | **진입점 변경 (노트 탭 상단)** |
| 전체 캡처 대상 FTS 검색 | 부분 — SearchViewModel 존재 | **FTS 소스 변경 필요** |
| 결과: 분류 칩 포함 | 부분 — SearchResultCard | **새 도메인 모델에 맞게 수정** |

**핵심 GAP:** 기존 SearchScreen의 뼈대는 재사용 가능하나, 데이터 소스(FTS)와 도메인 모델이 변경되므로 ViewModel/UiState 전면 재작성.

### 2.7 설정 — Phase 1-10

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| 다크 모드 (시스템/라이트/다크) | 부분 — 토글만 (ON/OFF) | **3옵션 라디오 버튼으로 변경** |
| 개인정보 처리방침 (WebView) | O — PrivacyPolicyScreen | 재사용 |
| 이용약관 | X — 미구현 | **추가 필요** |
| 앱 버전 | O — "1.0.0" 하드코딩 | 동적 버전 표시 권장 |
| 프로필 섹션 | O — ProfileSection | **삭제 (Phase 1 불필요)** |
| 연동 섹션 (Google Calendar/Obsidian) | O — IntegrationSettingItem | **삭제 (Phase 2a)** |
| AI 설정 (자동 분류/스마트 배치) | O — SwitchSettingItem | **삭제 (Phase 2a+)** |
| 노트 보기 방식 | O — ViewModeSettingItem | **삭제 (PRD에서 제거)** |
| 앱 시작 시 빠른 메모 | O — SwitchSettingItem | **삭제 (불필요)** |

**핵심 GAP:** 설정 화면 대폭 간소화. 프로필/연동/AI/보기방식 섹션 전체 삭제. "표시(다크모드)" + "정보(방침/약관/버전)"만 남김.

### 2.8 상세 화면 — Phase 1-10

| PRD 요구사항 | 현재 구현 | GAP |
|---|---|---|
| 할 일 상세 보기 (체크+제목+분류+마감+원본) | X — ResultScreen (분류 결과 확인 목적) | **완전히 다른 목적. 최소 상세 화면 신규 생성** |
| 분류 칩 변경 드롭다운 | 부분 — ResultEditBottomSheet | **인라인 드롭다운으로 변경** |
| 원문 표시 (수정 불가) | X — 미구현 | **원본 텍스트 영역 추가 필요** |

**핵심 GAP:** 현재 `result/` 패키지는 "캡처 후 즉시 분류 확인" 목적. PRD v10.0은 이 화면이 없어지고, 대신 "캡처 상세 보기" 화면이 필요. 전체 삭제 후 최소 상세 화면 구현.

---

## 3. 화면별 결론 요약

### 전면 재작성 필요 (기존 코드 구조와 PRD 요구사항이 근본적으로 다름)

| 화면 | 이유 | Phase |
|---|---|---|
| **CaptureContent** | 전체 입력 영역 → 하단 입력바 + 빈 상태 + 벨 아이콘 | 1-6 |
| **QuickCaptureViewModel** | Draft/분류추천 → TEMP 저장 + SyncQueue + 뱃지 카운트 | 1-6 |
| **NotesScreen/Content** | 노트/북마크 탭 → 폴더 리스트 구조 | 1-9 |
| **NotesViewModel/UiState** | Bookmark 모델 → Folder 기반 | 1-9 |
| **CalendarViewModel/UiState** | 도메인 모델 변경 (Schedule/Todo 전면 교체) | 1-8 |
| **SearchViewModel/UiState** | FTS 데이터 소스 변경 | 1-9 |
| **SettingsScreen** | 대폭 간소화 (항목 대부분 삭제) | 1-10 |
| **SettingsViewModel/UiState** | 대폭 간소화 | 1-10 |
| **NavGraph** | 라우트 구조 변경 (History 추가, Result/Notifications/Profile/QuickCapture 삭제) | 1-6~ |

### 신규 생성 필요

| 파일 | 설명 | Phase |
|---|---|---|
| `classification/AIStatusSheet.kt` | AI 분류 현황 바텀시트 | 1-6 |
| `classification/AIStatusSheetViewModel.kt` | 미확인 목록 관리 | 1-6 |
| `classification/ClassificationDropdown.kt` | 4옵션 분류 변경 드롭다운 | 1-6 |
| `capture/CaptureUiState.kt` | 새 상태 모델 | 1-6 |
| `history/HistoryScreen.kt` | 전체 기록 화면 | 1-7 |
| `history/HistoryViewModel.kt` | 페이징 + 삭제 | 1-7 |
| `history/HistoryUiState.kt` | 상태 | 1-7 |
| `history/components/HistoryItem.kt` | 기록 아이템 (제목+시간+분류칩+태그) | 1-7 |
| `notes/components/FolderItem.kt` | 폴더 행 (이름 + 노트 수) | 1-9 |
| `notes/components/FolderNoteList.kt` | 폴더 내 노트 목록 | 1-9 |
| `notes/CreateFolderDialog.kt` | 폴더 생성 다이얼로그 | 1-9 |
| `notes/RenameFolderDialog.kt` | 폴더 이름 변경 | 1-9 |
| `onboarding/OnboardingScreen.kt` | 3화면 온보딩 | 1-12 |
| `onboarding/OnboardingViewModel.kt` | 완료 상태 관리 | 1-12 |

### 삭제 대상 (PRD v10.0에서 제거되었거나 대체된 화면)

| 패키지/파일 | 이유 |
|---|---|
| `presentation/result/` 전체 | AI Status Sheet + 상세 화면으로 대체 |
| `presentation/notifications/` 전체 | AI Status Sheet로 대체 |
| `presentation/capture/QuickCaptureOverlay.kt` | Phase 1 불필요 |
| `presentation/capture/QuickCapturePopup.kt` | Phase 1 불필요 |
| `presentation/settings/ProfileScreen.kt` | Phase 1 불필요 |
| `presentation/home/components/` 전체 | 기존 홈 화면 컴포넌트 (삭제됨) |
| `components/notifications/NotificationCard.kt` | 삭제 |

---

## 4. 재사용 가능한 공통 컴포넌트

### 변경 없이 재사용

| 컴포넌트 | 파일 | 사용처 |
|---|---|---|
| **KairosBottomNav** | `components/common/KairosBottomNav.kt` | MainScreen (3탭 네비게이션) |
| **KairosTab enum** | `components/common/KairosBottomNav.kt` | HorizontalPager 탭 정의 |
| **SwipeableCard** | `components/common/SwipeableCard.kt` | 히스토리/캘린더/노트 스와이프 삭제 |
| **SectionHeader** | `components/common/SectionHeader.kt` | 캘린더/설정 섹션 구분 |
| **SectionHeaderKorean** | `components/common/SectionHeader.kt` | 한글 섹션 구분 |
| **SwitchPreference** | `components/settings/SwitchPreference.kt` | 설정 화면 토글 |

### 수정 후 재사용

| 컴포넌트 | 파일 | 수정 내용 |
|---|---|---|
| **KairosChip** | `components/common/KairosChip.kt` | 분류 유형별 색상 매핑 추가 (일정/할 일/노트/아이디어) |
| **SearchBar** | `components/search/SearchBar.kt` | 도메인 모델 변경에 맞춰 수정 |
| **SearchResultCard** | `components/search/SearchResultCard.kt` | 새 Capture 모델에 맞게 재작성 |
| **FilterChipRow** | `components/search/FilterChipRow.kt` | ClassifiedType enum으로 변경 |
| **WeekPicker** | `calendar/components/WeekPicker.kt` | 도메인 모델 변경 최소 수정 |

### 보존 (변경 없음)

| 파일 | 이유 |
|---|---|
| `ui/theme/Color.kt` | 디자인 시스템 색상 |
| `ui/theme/Theme.kt` | KairosTheme 제공 |
| `ui/theme/Type.kt` | 타이포그래피 |
| `MainScreen.kt` | HorizontalPager 스캐폴딩 (참조 업데이트만) |

---

## 5. MainScreen HorizontalPager + KairosBottomNav 구조

### 현재 구조

```
MainScreen
├── Scaffold
│   ├── bottomBar: KairosBottomNav (NOTES/HOME/CALENDAR)
│   └── content: HorizontalPager
│       ├── page 0: NotesContent (Scaffold 없음)
│       ├── page 1: CaptureContent (Scaffold 없음)
│       └── page 2: CalendarContent (Scaffold 없음)
```

### 핵심 특성
- **HorizontalPager**: 스와이프로 탭 전환. HOME(index 1)이 가운데
- **KairosTab enum**: NOTES(0), HOME(1), CALENDAR(2) 순서
- **derivedStateOf**: pagerState.currentPage로 현재 탭 계산
- **SnackbarHostState**: MainScreen에서 생성, CaptureContent에 전달
- **beyondViewportPageCount = 1**: 인접 페이지 프리렌더링

### Phase 1에서 변경 필요한 부분
- `CaptureContent` 파라미터 변경 (알림 벨 뱃지, 히스토리 네비게이션 콜백 추가)
- `NotesContent` 파라미터 변경 (검색 화면 네비게이션 콜백 추가)
- `CalendarContent` 파라미터 변경 (새 도메인 모델)
- **구조 자체는 유지** — HorizontalPager + KairosBottomNav 패턴 보존

---

## 6. 구현 우선순위 및 의존관계

```
Phase 1-4 (UseCase 완료) 이후:

1-6: 홈 + AI Status Sheet
     └─ 1-7: 전체 히스토리 (AI Status Sheet에서 진입)

1-8: 캘린더 탭 (독립)

1-9: 노트 탭 + 검색 (독립)

1-10: 설정 + 상세 화면 (독립)

1-11: Share Intent (독립)

1-12: 온보딩 (독립)
```

1-6이 가장 핵심이며 가장 많은 변경이 필요. 1-7은 1-6에 의존하고, 나머지는 독립적으로 병렬 진행 가능.

---

*Document Version: 1.0 | Last Updated: 2026-02-06*
