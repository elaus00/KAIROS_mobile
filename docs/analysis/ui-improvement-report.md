# KAIROS Mobile UI 개선 보고서

> 작성일: 2026-02-08
> 범위: presentation/ 전체 화면 및 컴포넌트 (48개 파일)
> 기준: UI 디자이너 관점 코드 레벨 리뷰

---

## 요약

KAIROS Mobile은 미니멀 모노크롬 디자인 시스템(KairosTheme)을 잘 구축하고 있으며, "Just Capture" 철학에 충실한 홈 화면 UX가 돋보인다. 다만 화면 간 일관성, 접근성, 인터랙션 피드백, 빈 상태 처리, 다크모드 세부 조정에서 개선 여지가 있다. 아래에서 화면별/카테고리별로 우선순위와 함께 정리한다.

---

## 1. 시각적 일관성 문제

### 1-1. 화면 제목 스타일 불일치 (P1)

| 화면 | 제목 | fontSize | fontWeight | padding |
|------|------|----------|------------|---------|
| CaptureContent (Home) | "Kairos" | 24.sp | SemiBold | h:20, v:16 |
| CalendarContent | "Calendar" | 24.sp | SemiBold | h:20, v:16 |
| NotesContent | "Notes" | 24.sp | SemiBold | **h:16, v:12** |
| HistoryScreen | "전체 기록" | **20.sp** | SemiBold | h:16, v:16 |
| TrashScreen | "휴지통" | **20.sp** | SemiBold | h:16, v:16 |
| SettingsScreen (TopAppBar) | "Settings" | **20.sp** | SemiBold | TopAppBar 기본 |
| CaptureDetailScreen (TopAppBar) | 동적 | **18.sp** | SemiBold | TopAppBar 기본 |
| NoteDetailScreen (TopAppBar) | "노트" | **18.sp** | SemiBold | TopAppBar 기본 |

**현재 상태**: 메인 3탭(Home/Calendar/Notes) 중 Notes의 padding이 다름(h:16 vs h:20). 서브 화면(History/Trash)은 20.sp로 메인 탭(24.sp)과 다르지만, 서브화면이므로 의도적일 수 있다.

**권장 개선안**:
- Notes 헤더의 horizontal padding을 20.dp로 통일 (메인 3탭 기준)
- 서브 화면 제목을 20.sp로 통일한 것은 적절하나, vertical padding도 16.dp로 통일 권장

### 1-2. 칩(Chip) 컴포넌트 radius 불일치 (P1)

| 사용처 | 코너 반경 |
|--------|-----------|
| KairosChip (공통) | 8.dp |
| ClassificationDropdown 칩 | **16.dp** |
| OnboardingScreen ClassificationChip | **16.dp** |
| CaptureDetailScreen ClassificationChip | 8.dp |
| NoteDetailScreen ClassificationChip | 8.dp |
| SearchResultItem 분류 태그 | **4.dp** |

**현재 상태**: 동일 역할의 칩이 3가지 다른 radius(4/8/16dp)를 사용한다. ClassificationDropdown의 16.dp는 pill 형태를 의도한 것으로 보이나 다른 분류 칩(8.dp)과 불일치한다.

**권장 개선안**:
- 인터랙티브 칩: 8.dp (KairosChip 기준으로 통일)
- 비인터랙티브 태그(검색 결과 등): 4.dp는 너무 각져 보이므로 6.dp로 조정
- ClassificationDropdown도 8.dp로 통일 (또는 의도적 pill이면 별도 디자인 토큰으로 분리)

### 1-3. 카드/컨테이너 border 사용 불일치 (P2)

| 화면 | border 사용 |
|------|-------------|
| SettingsCard | 0.5.dp, borderLight |
| CalendarCard | 1.dp, border |
| ScheduleCard | 1.dp, border |
| TaskItem | 1.dp, border |
| CaptureDetail 원문 박스 | 1.dp, border |
| NoteDetail 원본텍스트 박스 | 1.dp, border |
| HistoryItem | 없음 (background만) |
| SearchResultItem | 없음 (background만) |
| TrashItem (Card) | 없음 (CardDefaults) |

**현재 상태**: 같은 역할의 리스트 아이템인데 HistoryItem/SearchResult에는 border가 없고 TaskItem에는 있다.

**권장 개선안**: 카드형 리스트 아이템에 대해 border 유/무를 통일. 미니멀 디자인 기조에서는 border 없이 elevation 또는 background 색상 차이만으로 구분하는 것이 더 깔끔하다.

---

## 2. 타이포그래피 시스템 (P1)

### 2-1. Typography 정의 미비

**현재 상태**: `Type.kt`에 `bodyLarge`만 정의되어 있고 나머지는 주석 처리. 화면별로 fontSize/fontWeight를 직접 하드코딩하고 있어, 화면 전체의 타이포그래피 일관성을 시스템적으로 보장하기 어렵다.

**실제 사용 패턴 분석**:
- 화면 제목: 24.sp SemiBold (또는 20.sp)
- 섹션 레이블: 12.sp SemiBold (SectionHeader 등)
- 리스트 아이템 제목: 14~15.sp Medium
- 본문: 15~16.sp Normal
- 보조 텍스트: 13.sp
- 캡션/태그: 11~12.sp
- 뱃지: 10.sp

**권장 개선안**: Material3 Typography를 확장하여 디자인 토큰화.
```
headlineLarge = 24.sp SemiBold  (메인 탭 제목)
headlineSmall = 20.sp SemiBold  (서브 화면 제목)
titleMedium   = 15.sp Medium    (리스트 아이템 제목)
bodyLarge     = 16.sp Normal    (입력 필드, 온보딩 설명)
bodyMedium    = 14.sp Normal    (카드 본문)
bodySmall     = 13.sp Normal    (보조 텍스트, 설명)
labelLarge    = 13.sp Medium    (칩, 버튼)
labelMedium   = 12.sp SemiBold  (섹션 레이블)
labelSmall    = 11.sp Medium    (태그, 캡션)
```

---

## 3. 인터랙션 및 피드백

### 3-1. 리플(Ripple) 효과 비활성화 패턴 (P1)

**현재 상태**: 대부분의 아이콘 버튼과 텍스트 버튼이 `indication = null`로 리플 효과를 명시적으로 제거하고 있다.

**해당 위치**:
- CaptureTopBar: 벨/히스토리/설정 아이콘 (3곳)
- TrashTopBar: 뒤로 가기, 비우기 (2곳)
- HistoryTopBar: 뒤로 가기 (1곳)
- OnboardingScreen: 건너뛰기, 다음/시작 버튼, Google 연결 버튼 (3곳)
- KairosBottomNav: 전체 탭 아이템 (1곳)
- CalendarCard: 날짜 셀, 토글 버튼 (2곳)
- AIStatusSheet: 전체 확인, 확인 버튼, 배경 (3곳)
- CaptureToolBar: 전송 버튼 (1곳)

**문제점**: 터치 피드백이 없으면 사용자가 탭이 인식되었는지 알 수 없다. 특히 네비게이션 탭, 뒤로 가기, 전송 같은 핵심 액션에서 피드백이 없는 것은 UX 품질 저하.

**권장 개선안**:
- 아이콘 버튼은 `IconButton`으로 래핑하여 기본 리플 + 48dp 터치 타겟 확보
- 텍스트 버튼은 `TextButton` 사용 검토
- 커스텀 버튼(전송, 온보딩 다음)은 최소한 `indication = rememberRipple(bounded = true)` 적용
- 달력 날짜 셀은 리플 없이 색상 변화 애니메이션으로 피드백 제공 (현재 적절)

### 3-2. 터치 타겟 크기 미달 (P0)

**현재 상태**: 여러 인터랙티브 요소가 최소 터치 타겟 48dp를 미달한다.

| 요소 | 실제 크기 | 비고 |
|------|-----------|------|
| CaptureTopBar 아이콘들 | 24.dp | padding 없이 size만 지정 |
| TrashTopBar 뒤로 가기 | 24.dp | padding 없이 직접 clickable |
| HistoryTopBar 뒤로 가기 | 24.dp | 동일 |
| ScheduleCard 삭제 아이콘 | 18.dp | 매우 작음 |
| CalendarCard 토글 아이콘 | 18.dp | 부모 Row clickable로 커버 |
| SearchScreen 지우기 버튼 | 20.dp (IconButton 안) | IconButton 내부라 48dp 확보됨 |
| 이미지 제거 버튼 | 20.dp | padding 2.dp만 |
| TaskCheckbox | 22.dp | 최소 기준 미달 |
| 폴더 추가 버튼 | 32.dp | 최소 기준 미달 |

**권장 개선안**:
- 단독 아이콘은 `IconButton` (기본 48dp 터치 영역)으로 감싸거나
- `Modifier.padding(12.dp)` 추가로 48dp 이상 확보
- ScheduleCard 삭제 아이콘: 18dp + padding으로 최소 48dp 확보 필수
- TaskCheckbox: 22dp는 너무 작음, 최소 24dp + padding으로 48dp 확보
- 이미지 제거 버튼: 터치 영역이 20dp로 실수 탭 유발 가능

### 3-3. 화면 전환 애니메이션 부재 (P2)

**현재 상태**: NavGraph에서 화면 전환 시 별도 애니메이션이 설정되지 않은 것으로 보인다. HorizontalPager(메인 3탭)는 자연스러운 스와이프 전환이 있지만, 서브 화면(Settings, History, Trash, Search 등) 진입/퇴장 시 default composable 전환.

**권장 개선안**:
- 서브 화면 진입: slideInHorizontally + fadeIn
- 서브 화면 퇴장: slideOutHorizontally + fadeOut
- 바텀시트(AIStatusSheet): 이미 오버레이로 구현되어 있으나, slideIn/slideOut 애니메이션 추가 권장

---

## 4. 빈 상태(Empty State) 처리 일관성 (P1)

### 4-1. 빈 상태 표현 비교

| 화면 | 아이콘 | 주 텍스트 | 보조 텍스트 |
|------|--------|-----------|-------------|
| NotesContent (빈 상태) | StickyNote2 (48dp) | "아직 노트가 없습니다" | "캡처한 내용 중..." |
| SearchScreen (초기) | Search (48dp) | "캡처를 검색하세요" | 없음 |
| SearchScreen (결과 없음) | 없음 | "결과 없음" | "다른 키워드로..." |
| HistoryScreen (빈) | 없음 | "기록이 없습니다" | 없음 |
| TrashScreen (빈) | 없음 | "휴지통이 비어 있습니다" | 없음 |
| CalendarScreen 일정 없음 | 없음 | "일정이 없습니다" | 없음 |
| CalendarScreen 할일 없음 | 없음 | "할 일이 없습니다" | 없음 |
| AIStatusSheet (빈) | 없음 | "미확인 분류가 없습니다" | 없음 |

**현재 상태**: NotesContent만 아이콘+주 텍스트+보조 텍스트 3레벨 구조이고, 나머지는 텍스트만.

**권장 개선안**:
- 모든 빈 상태에 아이콘(48dp, textMuted) + 주 텍스트(15.sp) + 보조 텍스트(13.sp) 3레벨 통일
- `EmptyStateView` 공통 컴포넌트로 추출 (icon, title, subtitle 파라미터)
- CTA 버튼 추가 검토: TrashScreen "비어 있음" → 안내만, CalendarScreen → "캡처에서 일정을 입력해보세요" 유도

---

## 5. 다크모드 세부 조정 (P1)

### 5-1. 하드코딩된 색상

**현재 상태**: 일부 색상이 KairosTheme을 사용하지 않고 하드코딩되어 있어 다크모드에서 부자연스러울 수 있다.

| 위치 | 하드코딩 색상 | 문제점 |
|------|-------------|--------|
| CaptureDetailScreen CalendarSyncSection | `Color(0xFF4CAF50)`, `Color(0xFFFFA726)`, `Color(0xFFEF5350)` | KairosTheme.success/warning/danger와 불일치 |
| ScheduleTimeline SyncStatusBadge | `Color(0xFF4CAF50)`, `Color(0xFFFFA726)`, `Color(0xFFEF5350)` | 동일 |
| OnboardingPageGoogle "연결됨" | `Color(0xFF4CAF50)` | 동일 |

**권장 개선안**:
- `KairosColors`에 이미 `success`, `warning`, `danger` 토큰이 정의되어 있으므로 이를 사용
- `Color(0xFF4CAF50)` -> `colors.success` (단, KairosLight.success = 0xFF10B981로 값이 다르므로, 의도적으로 다른 녹색이면 `calendarSynced` 같은 별도 토큰 추가)
- `Color(0xFFFFA726)` -> `colors.warning` (값은 같은 계열이지만 정확한 코드가 다름: 0xFFF59E0B)

### 5-2. 다크모드에서 elevation/shadow

**현재 상태**: `KairosBottomNav`에 `shadow(elevation = 16.dp)` 적용. 다크모드에서 shadow는 거의 보이지 않으며, 대신 border나 surface tint로 깊이감을 표현해야 한다.

**권장 개선안**: 다크모드에서는 border(1.dp, borderLight) 추가 또는 surface elevation 컬러로 분리

---

## 6. 레이아웃 및 간격 일관성 (P1)

### 6-1. 수평 패딩 불일치

| 화면 | 콘텐츠 horizontal padding |
|------|--------------------------|
| CaptureContent | 20.dp (텍스트 입력), 16.dp (하단 툴바) |
| CalendarContent | 20.dp |
| NotesContent | 16.dp (헤더), 16.dp (리스트) |
| HistoryScreen | 16.dp |
| TrashScreen | 16.dp |
| SearchScreen | 8.dp (헤더), 16.dp (리스트) |
| SettingsScreen | 16.dp (카드), 20.dp (SectionHeader) |
| CaptureDetailScreen | 20.dp |
| NoteDetailScreen | 20.dp |

**권장 개선안**:
- 메인 콘텐츠 영역: 20.dp 통일 (현재 CalendarContent, CaptureDetail, NoteDetail에서 사용)
- 리스트 아이템: 16.dp contentPadding (현재 적절)
- 또는 디자인 토큰으로 `ScreenHorizontalPadding = 20.dp`, `ListContentPadding = 16.dp` 정의

### 6-2. 리스트 아이템 간격

| 리스트 | verticalArrangement.spacedBy |
|--------|------------------------------|
| NotesContent | 2.dp |
| HistoryScreen | 8.dp |
| TrashScreen | 8.dp |
| SearchScreen | 8.dp |
| AIStatusSheet | 0.dp |
| CalendarScreen TaskList | 8.dp |

**현재 상태**: NotesContent만 2.dp로 매우 촘촘함. 노트 리스트는 divider로 구분하므로 의도적이나, 다른 리스트와 패턴이 상이하다.

**권장 개선안**: 카드형 리스트는 8.dp, divider형 리스트는 0~2.dp로 패턴 2가지로 정리 (현재 적절하나 문서화 필요)

---

## 7. 정보 계층(Information Hierarchy) (P2)

### 7-1. CaptureContent 빈 상태 시 CTA 부재

**현재 상태**: 홈 화면(CaptureContent)에서 캡처가 없을 때 특별한 빈 상태 UI가 없다. placeholder 텍스트("떠오르는 생각을 캡처하세요...")가 존재하지만, 최초 사용자에게는 추가 안내가 필요할 수 있다.

**권장 개선안**: "Just Capture" 철학에 따르면 현재 미니멀한 접근이 적절하다. 단, 온보딩 완료 후 최초 홈 진입 시 간단한 힌트 애니메이션(입력창 강조 등) 추가 고려.

### 7-2. Calendar 화면 정보 밀도

**현재 상태**: Calendar 화면에 "일정" 섹션과 "할 일" 섹션이 세로로 나열. 일정이 많을 경우 할 일 섹션이 스크롤 아래로 밀린다.

**권장 개선안**: 현재 구조가 적절하나, 각 섹션의 접힘/펼침(collapsible) 기능 추가 고려 (Phase 2b 이후)

---

## 8. 에러 상태 처리 (P1)

### 8-1. 네트워크 오류 피드백

**현재 상태**: 에러는 Snackbar로만 처리 (CaptureContent, HistoryScreen, TrashScreen, SearchScreen). Snackbar는 일시적이므로 사용자가 놓칠 수 있다.

**권장 개선안**:
- 네트워크 의존 화면(캡처 전송 실패)에서는 인라인 에러 메시지 표시 고려
- Snackbar + 재시도 액션 조합은 현재 잘 되어 있음 (HistoryScreen 삭제 → "실행 취소")

### 8-2. CaptureDetailScreen/NoteDetailScreen 에러 표시

**현재 상태**: 로딩 실패 시 화면 중앙에 에러 텍스트만 표시. "뒤로 가기" 외에 사용자가 취할 수 있는 액션이 없다.

**권장 개선안**: "다시 시도" 버튼 추가

---

## 9. 접근성(Accessibility) (P1)

### 9-1. contentDescription 누락

| 위치 | 요소 | contentDescription |
|------|------|-------------------|
| NotesContent FolderFilterChips 폴더칩 | KairosChip | 없음 (텍스트로 유추 가능하나 명시 권장) |
| CalendarCard 날짜 셀 | Box + Text | 없음 |
| OnboardingBottomBar 인디케이터 | Box | 없음 |
| TaskCheckbox | Box | 없음 (semantics 필요) |

**권장 개선안**:
- TaskCheckbox: `Modifier.semantics { role = Role.Checkbox; stateDescription = if (isChecked) "완료됨" else "미완료" }` 추가
- 날짜 셀: `Modifier.semantics { contentDescription = "${date.monthValue}월 ${date.dayOfMonth}일" }` 추가

### 9-2. 색상 대비 확인

| 조합 | 전경색 | 배경색 | 대비비 (약) | WCAG AA |
|------|--------|--------|-------------|---------|
| textMuted on background (Light) | #AAAAAA on #FAFAFA | ~2.3:1 | 미달 |
| textMuted on card (Light) | #AAAAAA on #FFFFFF | ~2.3:1 | 미달 |
| placeholder on background (Light) | #CCCCCC on #FAFAFA | ~1.6:1 | 미달 |
| chipText on chipBg (Light) | #666666 on #F0F0F0 | ~4.5:1 | 충족 |
| textSecondary on background (Light) | #888888 on #FAFAFA | ~3.5:1 | 미달 (AA 4.5:1) |

**현재 상태**: textMuted(#AAAAAA)와 placeholder(#CCCCCC)는 WCAG AA(4.5:1) 기준을 미달한다. 이는 미니멀 디자인에서 흔하지만, 접근성 면에서 개선 필요.

**권장 개선안**:
- textMuted를 #999999 이하로 조정 (약 3:1, 대형 텍스트용)
- placeholder를 #AAAAAA 이하로 조정
- 비핵심 정보(날짜, 시간 보조 텍스트)에만 textMuted 사용 제한

---

## 10. 모션 및 상태 변화 (P2)

### 10-1. 리스트 아이템 추가/삭제 애니메이션

**현재 상태**: LazyColumn에서 아이템 추가/삭제 시 별도 animateItem 설정 없음.

**권장 개선안**: `Modifier.animateItem()` 추가 (Compose 1.7+에서 지원)

### 10-2. CalendarCard 주간/월간 전환

**현재 상태**: AnimatedContent로 fadeIn/fadeOut + SizeTransform 적용됨. 잘 구현되어 있음.

### 10-3. SwipeableCard

**현재 상태**: SwipeToDismissBox 사용, 배경색 및 아이콘 스케일 애니메이션 적용됨. 적절하게 구현됨.

---

## 11. 화면별 세부 리뷰

### 11-1. CaptureContent (Home) - 핵심 화면

**잘 된 점**:
- "Just Capture" 철학에 충실: 전체 화면이 입력 영역
- 전송 버튼의 활성/비활성 시각적 구분 명확
- 이미지 미리보기 + 제거 버튼 배치 적절
- 임시 저장(saveDraft) 라이프사이클 연동

**개선 필요**:
- [P0] 상단 바 아이콘(벨/히스토리/설정) 터치 타겟 24dp 미달
- [P1] 이미지 첨부 아이콘(Icons.Outlined.Image) 터치 타겟 24dp, 독립 clickable에 리플 없음
- [P2] 날짜 표시(DateDisplay)가 화면 상단에 고정인데, 긴 텍스트 입력 시 스크롤과 함께 사라지지 않음 (현재 구조에서는 OK)

### 11-2. NotesContent

**잘 된 점**:
- 폴더 필터 칩이 수평 스크롤로 자연스러움
- 사용자 폴더 롱프레스 컨텍스트 메뉴 잘 구현
- 빈 상태 뷰가 필터 유무에 따라 다른 메시지 표시

**개선 필요**:
- [P1] NoteListItem divider가 아이템 내부에 있어 마지막 아이템에도 divider 표시됨 -> items의 마지막 아이템은 divider 생략 또는 `LazyColumn`의 `divider` 패턴 사용
- [P1] 노트 리스트 아이템에 카드 배경이 없어서 클릭 영역 인지가 어려움 (현재 clip(12.dp) + clickable만)

### 11-3. CalendarContent

**잘 된 점**:
- CalendarCard 주간/월간 전환이 스와이프 제스처 + 버튼 토글 두 가지로 제공
- ScheduleTimeline 타임라인 UI가 시각적으로 잘 구성됨
- SyncStatusBadge 색상 코딩이 직관적
- Snackbar "실행 취소" 패턴 잘 적용

**개선 필요**:
- [P0] ScheduleCard 삭제 아이콘 18dp - 매우 작은 터치 타겟
- [P1] CalendarCard의 좌우 스와이프(달 변경)가 월간 뷰에서만 작동하는데, 사용자가 이를 발견하기 어려움 - 달 변경 화살표 버튼 추가 고려
- [P2] 일요일이 빨간색이 아닌 기본 색상 (미니멀 디자인 의도일 수 있음)

### 11-4. SettingsScreen

**잘 된 점**:
- SettingsCard 컴포넌트로 그룹핑이 잘 되어 있음
- SectionHeader 재사용
- Switch 커스텀 색상 적용

**개선 필요**:
- [P1] Google Calendar 섹션에 개발자용 디버그 항목("OAuth code 교환", "토큰 직접 저장", "이벤트 조회 테스트")이 일반 사용자에게 노출됨 -> DEBUG 빌드에서만 표시하거나 "개발자 도구" 섹션으로 이동
- [P1] "개발자 도구" 섹션이 일반 사용자에게도 보임 -> 숨김 처리 (7회 탭 등)

### 11-5. OnboardingScreen

**잘 된 점**:
- 4페이지 구성이 적절 (소개 → AI 분류 → Google Calendar → 첫 캡처)
- 페이지 인디케이터 애니메이션 (활성 페이지 width 24dp vs 8dp)
- "건너뛰기" 텍스트가 비간섭적

**개선 필요**:
- [P1] Google Calendar 연결 페이지에서 "연결됨" 상태 배경이 하드코딩(Color(0xFF4CAF50))
- [P2] 온보딩 1,2페이지에 일러스트 대신 텍스트만 사용 -> 향후 일러스트/로티 애니메이션 추가 고려

### 11-6. TrashScreen

**잘 된 점**:
- 복원/삭제 버튼 레이아웃 명확
- "비우기" 텍스트 버튼이 항목 존재 시에만 표시

**개선 필요**:
- [P0] "비우기"(전체 삭제)에 확인 다이얼로그 없음 -> 위험한 동작이므로 반드시 확인 필요
- [P1] "완전 삭제" 버튼에 확인 다이얼로그 없음 -> 비가역 동작이므로 확인 필요
- [P1] 빈 상태 뷰에 아이콘 없음

### 11-7. SearchScreen

**잘 된 점**:
- 자동 포커스로 즉시 입력 가능
- FilterChipRow 재사용
- 초기/결과없음/결과있음 3가지 상태 구분

**개선 필요**:
- [P2] 검색 결과 아이템이 카드 형태이나, HistoryItem과 스타일이 다름 (SearchResult는 Row, History는 Column)

### 11-8. AIStatusSheet

**잘 된 점**:
- 오버레이 배경 반투명 처리
- 확인 버튼, 전체 확인, 분류 변경 드롭다운 등 필요 액션 제공
- heightIn(max=400.dp)로 과도한 확장 방지

**개선 필요**:
- [P2] 진입/퇴장 애니메이션 없음 -> slideInVertically(from top) 추가 권장

---

## 12. 위젯 (P2)

### 12-1. CaptureWidgetProvider

**현재 상태**: RemoteViews 기반 전통 위젯. 단순히 앱을 여는 용도. Glance AppWidget이 아닌 XML 레이아웃 사용.

**권장 개선안**: Phase 2a에서 Glance 위젯이 계획된 것으로 보이나, 현재 CaptureWidgetProvider는 전통 AppWidgetProvider. Glance로 마이그레이션하면 테마 일관성 향상.

---

## 우선순위별 요약

### P0 (즉시 수정 - 사용자 경험/안전 직결)
1. **터치 타겟 크기 미달** - CaptureTopBar 아이콘, ScheduleCard 삭제, TaskCheckbox, 이미지 제거 버튼
2. **TrashScreen "비우기" 확인 다이얼로그 누락** - 비가역 대량 삭제 동작

### P1 (다음 스프린트)
3. 화면 제목 스타일/패딩 통일 (Notes 헤더 padding)
4. 칩 cornerRadius 통일
5. Typography 시스템 정의 (Type.kt 확장)
6. 리플 효과 복원 (핵심 액션 버튼)
7. 빈 상태 뷰 통일 (공통 EmptyStateView 컴포넌트)
8. 하드코딩 색상 -> KairosTheme 토큰 사용
9. SettingsScreen 개발자 항목 숨김 처리
10. 에러 상태에 "다시 시도" 액션 추가
11. 접근성: contentDescription, semantics 보완
12. 색상 대비 개선 (textMuted, placeholder)
13. NoteListItem 마지막 divider 제거
14. TrashScreen "완전 삭제" 확인 다이얼로그
15. Calendar 월간뷰 달 변경 UI 힌트

### P2 (향후 개선)
16. 화면 전환 애니메이션 추가
17. 카드 border 사용 통일
18. 수평 패딩 디자인 토큰 정의
19. 리스트 아이템 추가/삭제 애니메이션
20. AIStatusSheet 진입/퇴장 애니메이션
21. 다크모드 BottomNav shadow 처리
22. 온보딩 일러스트 추가
23. Glance 위젯 마이그레이션

---

## 부록: 잘 구현된 점

- **디자인 시스템**: KairosColors + CompositionLocal 기반 커스텀 색상 시스템이 체계적
- **다크모드**: 라이트/다크 팔레트가 완전히 분리되어 있고, 대부분의 UI가 KairosTheme.colors 사용
- **"Just Capture" 철학**: 홈 화면이 전체 화면 입력 영역으로, 캡처까지의 단계가 최소
- **스와이프 삭제**: SwipeToDismissBox + Snackbar 실행 취소 패턴이 일관되게 적용
- **CalendarCard**: 주간/월간 전환, 스와이프 제스처, 날짜 선택 애니메이션이 잘 구현됨
- **분류 변경**: ClassificationDropdown이 재사용 가능한 컴포넌트로 잘 분리됨
- **3단계 삭제**: SoftDelete -> Trash -> HardDelete 모델이 UI에 잘 반영됨
