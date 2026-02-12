# KAIROS 디자인 가이드

> 이 문서는 KAIROS 모바일 앱의 디자인 시스템을 정의한다.
> 새로운 UI 작업 시 반드시 이 가이드를 참조해야 한다.
>
> **참조 소스**: `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`

---

## 1. 디자인 철학

- **미니멀리스트 모노크롬 디자인**: 무채색 기조, 유채색은 danger/success/warning 3가지만 허용
- **Just Capture 철학**: 캡처까지의 단계를 늘리지 않는 간결한 UI
- **Material Design 기본값 사용 금지**: Material3 ColorScheme을 직접 참조하지 않고, 반드시 `KairosTheme.colors`를 통해 색상에 접근
- **모든 색상은 `KairosColors` 시스템을 통해 관리**: `KairosTheme.colors.xxx` 형태로만 사용

---

## 2. 색상 시스템

### 2.1 라이트 테마 (`KairosLight`)

| 토큰 | Hex | 용도 |
|-------|-----|------|
| `background` | `#FAFAFA` | 메인 배경 |
| `card` | `#FFFFFF` | 카드 배경 |
| `border` | `#EEEEEE` | 기본 보더 |
| `borderLight` | `#F0F0F0` | 연한 보더 |
| `text` | `#111111` | 기본 텍스트 |
| `textSecondary` | `#888888` | 보조 텍스트 |
| `textMuted` | `#737373` | 희미한 텍스트 (WCAG AA 4.54:1) |
| `placeholder` | `#737373` | 플레이스홀더 (WCAG AA 4.54:1) |
| `accent` | `#111111` | 강조 색상 (모노크롬) |
| `accentBg` | `#F5F5F5` | 강조 배경 |
| `chipBg` | `#F0F0F0` | 칩 배경 |
| `chipText` | `#666666` | 칩 텍스트 |
| `danger` | `#EF4444` | 위험/삭제 (유일한 빨간 컬러) |
| `success` | `#10B981` | 성공/연동됨 (녹색) |
| `warning` | `#F59E0B` | 경고 (황색) |
| `icon` | `#555555` | 아이콘 색상 |
| `iconMuted` | `#AAAAAA` | 비활성 아이콘 |
| `divider` | `#F0F0F0` | 구분선 |

### 2.2 다크 테마 (`KairosDark`)

| 토큰 | Hex | 용도 |
|-------|-----|------|
| `background` | `#0A0A0A` | 메인 배경 |
| `card` | `#1A1A1A` | 카드 배경 |
| `border` | `#2A2A2A` | 기본 보더 |
| `borderLight` | `#222222` | 연한 보더 |
| `text` | `#FFFFFF` (80% 투명도) | 기본 텍스트 |
| `textSecondary` | `#888888` | 보조 텍스트 |
| `textMuted` | `#7A7A7A` | 희미한 텍스트 (WCAG AA 4.61:1) |
| `placeholder` | `#7A7A7A` | 플레이스홀더 (WCAG AA 4.61:1) |
| `accent` | `#FFFFFF` (80% 투명도) | 강조 색상 |
| `accentBg` | `#252525` | 강조 배경 |
| `chipBg` | `#2A2A2A` | 칩 배경 |
| `chipText` | `#999999` | 칩 텍스트 |
| `danger` | `#EF4444` | 위험/삭제 (유일한 빨간 컬러) |
| `success` | `#10B981` | 성공/연동됨 (녹색) |
| `warning` | `#F59E0B` | 경고 (황색) |
| `icon` | `#AAAAAA` | 아이콘 색상 |
| `iconMuted` | `#555555` | 비활성 아이콘 |
| `divider` | `#2A2A2A` | 구분선 |

### 2.3 색상 접근 방법

```kotlin
// 올바른 사용법
val colors = KairosTheme.colors
Text(color = colors.text)
Box(Modifier.background(colors.card))

// 금지: Material3 기본 색상 직접 사용
// Text(color = MaterialTheme.colorScheme.onSurface)  // 금지
// Box(Modifier.background(MaterialTheme.colorScheme.surface))  // 금지
```

### 2.4 색상 금지 사항

- Material3 기본 테마 색상(`MaterialTheme.colorScheme.*`) 직접 사용 금지
- 하드코딩 `Color(0xFF...)` 값 사용 금지 (반드시 `KairosTheme.colors` 토큰 사용)
- `AlertDialog`, `OutlinedTextField` 등 Material3 컴포넌트에 반드시 `containerColor`, `textColor` 등 KairosTheme 색상 명시 적용

---

## 3. 타이포그래피

### 3.1 스타일 목록 (`KairosTypography`)

| 스타일 | 크기 | 굵기 | 자간 | 용도 |
|--------|------|------|------|------|
| `headlineLarge` | 24sp | SemiBold (600) | 0sp | 화면 제목 |
| `headlineSmall` | 20sp | SemiBold (600) | 0sp | 섹션 제목, 다이얼로그 제목 |
| `titleMedium` | 15sp | Medium (500) | 0sp | 카드 제목, 리스트 아이템 제목 |
| `bodyLarge` | 16sp | Normal (400) | 0.5sp | 본문 텍스트 (큰) |
| `bodyMedium` | 14sp | Normal (400) | 0.25sp | 본문 텍스트 (기본) |
| `bodySmall` | 13sp | Normal (400) | 0sp | 본문 텍스트 (작은), 보조 정보 |
| `labelLarge` | 13sp | Medium (500) | 0sp | 버튼 텍스트, 칩 텍스트 |
| `labelMedium` | 12sp | SemiBold (600) | 0sp | 섹션 헤더, 배지 |
| `labelSmall` | 11sp | Medium (500) | 0sp | 캡션, 타임스탬프 |

### 3.2 폰트 크기 체계

```
11sp → 12sp → 13sp → 14sp → 15sp → 16sp → 20sp → 24sp
```

8단계 크기 체계. 11sp~16sp는 1sp 단위 촘촘하게, 16sp 이후는 큰 점프.

---

## 4. 간격 시스템

### 4.1 기본 원칙

- **기본 단위**: 8dp
- **화면 전체 수평 패딩**: 20dp
- **카드 내부 수평 패딩**: 16dp
- **카드 간 수직 간격**: 8dp
- **섹션 간 수직 간격**: 24dp
- **터치 타겟**: 최소 48dp (WCAG 접근성 기준)

### 4.2 자주 사용되는 간격

| 값 | 용도 |
|----|------|
| 4dp | 아이콘과 텍스트 사이, 미세 간격 |
| 8dp | 카드 간격, 기본 간격 단위 |
| 12dp | 칩 내부 수평 패딩, 섹션 헤더 하단 여백 |
| 16dp | 카드 내부 패딩 |
| 20dp | 화면 수평 패딩 (SectionHeader 기준) |
| 24dp | 섹션 간 간격, 하단 네비게이션 바 하단 패딩 |
| 48dp | 하단 네비게이션 바 수평 패딩, 최소 터치 타겟 |

---

## 5. 공통 컴포넌트

### 5.1 SectionHeader

**파일**: `presentation/components/common/SectionHeader.kt`

**용도**: 리스트/그리드 상단에 섹션 제목을 표시하는 헤더

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `title` | String | (필수) | 섹션 제목 텍스트 |
| `modifier` | Modifier | `Modifier` | 외부 수정자 |
| `fontSize` | TextUnit | 12.sp | 폰트 크기 |
| `trailingContent` | `@Composable (() -> Unit)?` | null | 우측 끝 추가 콘텐츠 |

**스타일 규칙**:
- 색상: `colors.textSecondary`
- 굵기: `FontWeight.SemiBold`
- 자간: 0.5sp
- 패딩: start/end 20dp, bottom 12dp
- 전체 너비 사용 (`fillMaxWidth`)

```kotlin
// 사용 예시
SectionHeader(title = "오늘의 일정")

// trailing 콘텐츠 포함
SectionHeader(
    title = "할 일",
    trailingContent = { Text("3건", color = colors.textMuted) }
)
```

### 5.2 KairosChip

**파일**: `presentation/components/common/KairosChip.kt`

**용도**: AI 분류 결과 표시, 필터 태그

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `text` | String | (필수) | 칩 텍스트 |
| `modifier` | Modifier | `Modifier` | 외부 수정자 |
| `onClick` | `(() -> Unit)?` | null | 클릭 콜백 |
| `selected` | Boolean | false | 선택 상태 |

**상태별 스타일**:

| 상태 | 배경 | 텍스트 색상 |
|------|------|-------------|
| 비선택 | `colors.chipBg` | `colors.chipText` |
| 선택 (라이트) | `colors.accent` (#111111) | `Color.White` |
| 선택 (다크) | `colors.accent` (80% white) | `colors.background` (#0A0A0A) |

**스타일 규칙**:
- 모서리: `RoundedCornerShape(8.dp)`
- 내부 패딩: horizontal 12dp, vertical 6dp
- 폰트: 13sp, `FontWeight.Medium`

### 5.3 KairosBottomNav

**파일**: `presentation/components/common/KairosBottomNav.kt`

**용도**: 앱 하단 플로팅 네비게이션 바

**3탭 구조** (좌 -> 우):
1. **NOTES** - `Icons.Outlined.Description` / `Icons.Filled.Description`
2. **HOME** - `Icons.Outlined.Home` / `Icons.Filled.Home` (가운데)
3. **CALENDAR** - `Icons.Outlined.CalendarToday` / `Icons.Filled.CalendarToday`

**스타일 규칙**:
- 전체 수평 패딩: 48dp
- 바 모서리: `RoundedCornerShape(20.dp)`
- 바 배경: `colors.card`
- 그림자: elevation 16dp, `Color.Black.copy(alpha = 0.15f)`
- 내부 패딩: horizontal 28dp, vertical 10dp
- 탭 간 간격: 36dp
- 아이콘 크기: 22dp
- 선택 아이콘: `colors.text` + Filled 아이콘
- 비선택 아이콘: `colors.textMuted` + Outlined 아이콘
- 아이콘 색상 전환: `animateColorAsState` (200ms tween)

### 5.4 카드 스타일

모든 카드 컴포넌트에 적용하는 공통 스타일:

```kotlin
// 기본 카드 스타일
Box(
    modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(colors.card)
        .border(
            width = 1.dp,  // 또는 0.5.dp (얇은 보더)
            color = colors.border,
            shape = RoundedCornerShape(12.dp)
        )
        .padding(16.dp)
)
```

| 속성 | 값 |
|------|-----|
| 모서리 | `RoundedCornerShape(12.dp)` |
| 배경 | `colors.card` |
| 보더 | `colors.border`, 1dp 또는 0.5dp |
| 내부 패딩 | 16dp |

### 5.5 AlertDialog

Material3 AlertDialog 사용 시 반드시 KairosTheme 색상을 명시적으로 적용:

```kotlin
AlertDialog(
    onDismissRequest = { /* ... */ },
    containerColor = colors.card,        // 필수
    titleContentColor = colors.text,     // 필수
    textContentColor = colors.textSecondary, // 필수
    title = { /* ... */ },
    text = { /* ... */ },
    confirmButton = { /* ... */ },
    dismissButton = { /* ... */ }
)
```

---

## 6. 인터랙션 패턴

### 6.1 체크박스

- 형태: 둥근 사각형 (`RoundedCornerShape`)
- 터치 타겟: 48dp
- 시각적 크기: 22dp
- 완료 시: `colors.accent` 배경 + 체크 아이콘

### 6.2 스와이프

- 캘린더 확장/축소: 상하 스와이프
- 월 이동: 좌우 스와이프

### 6.3 3단계 삭제 모델

```
Active → SoftDelete(3초 되돌리기) → Trash(30일 보관) → HardDelete(영구 삭제)
```

- SoftDelete: 스와이프 삭제 후 3초간 되돌리기 Snackbar 표시
- Trash: 30일 경과 후 `TrashCleanupWorker`가 자동 영구 삭제

### 6.4 확장/축소 애니메이션

- `AnimatedVisibility`: 요소의 표시/숨김 전환
- `AnimatedContent`: 콘텐츠 교체 전환
- 캘린더 주간/월간 뷰 전환에 적용

---

## 7. 금지 사항 체크리스트

새로운 UI 코드를 작성하거나 리뷰할 때 아래 항목을 확인:

- [ ] `MaterialTheme.colorScheme.*`를 직접 사용하지 않았는가? (반드시 `KairosTheme.colors` 사용)
- [ ] `AlertDialog`에 `containerColor = colors.card` 등 KairosTheme 색상을 지정했는가?
- [ ] `OutlinedTextField`에 KairosTheme 색상을 명시적으로 적용했는가?
- [ ] 하드코딩 `Color(0xFF...)` 값을 사용하지 않았는가? (색상 토큰만 사용)
- [ ] 모든 인터랙티브 요소(버튼, 체크박스, 탭)가 48dp 이상 터치 타겟을 확보했는가?
- [ ] 새로운 색상이 필요한 경우 `KairosColors`에 토큰으로 추가했는가?
- [ ] 텍스트 스타일은 `KairosTypography`에 정의된 스타일을 사용했는가?
- [ ] 카드 모서리는 `RoundedCornerShape(12.dp)`를 사용했는가?
