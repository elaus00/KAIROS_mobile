# KAIROS 디자인 가이드

> **Version**: 2.0
> **최종 수정**: 2026-02-12
>
> 이 문서는 KAIROS 모바일 앱의 디자인 시스템과 UX 원칙을 정의한다.
> 새로운 UI 작업 시 반드시 이 가이드를 참조해야 한다.
>
> **참조 소스**: `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`
> **상위 문서**: `docs/direction/philosophy_principles.md`, `.claude/skills/apple_hig_guide/SKILL.md`

---

## 1. 디자인 철학

### 1.1 KAIROS 핵심 원칙

| 원칙 | 설명 | UI 적용 |
|------|------|---------|
| **Just Capture** | 앱 실행 → 즉시 입력. 어디에 적을지 고민 불필요 | 입력창이 첫 화면, 최소 입력 단계 |
| **Auto Organize** | 분류·태깅·목적지 결정은 AI가 처리 | 사용자에게 분류 선택을 강요하지 않음 |
| **Straight to Action** | 일정은 캘린더로, 할 일은 투두로 자동 연결 | 기록에서 행동까지 자동 흐름 |

### 1.2 시각적 정체성

- **미니멀리스트 모노크롬 디자인**: 무채색 기조, 유채색은 danger/success/warning 3가지만 허용
- **콘텐츠 존중 (Deference)**: UI는 콘텐츠를 돕되 경쟁하지 않음. 사용자의 기록이 항상 주인공
- **명료성 (Clarity)**: 모든 요소는 즉시 이해 가능해야 함. 장식적 요소 최소화, 기능이 디자인을 주도
- **깊이감 (Depth)**: 시각적 레이어와 모션으로 계층 구조와 공간 관계 전달

### 1.3 설계 판단 기준

기능 추가/UI 변경 시 하나라도 Yes이면 재고한다:

| 기준 | 질문 | 위반 시 결과 |
|------|------|-------------|
| **Friction** | 기록까지의 단계를 늘리는가? | 사용자가 기록을 포기 |
| **Choice** | 사용자에게 선택을 강요하는가? | 판단 에너지 소모 |
| **Burden** | 정리 부담을 사용자에게 전가하는가? | 정리 부채 누적 |
| **Flow** | 핵심 루프(기록 → 정리 → 실행)를 방해하는가? | 제품 가치 훼손 |
| **Necessity** | 이것 없이도 앱이 동작하는가? | 불필요한 복잡성 |
| **Quality** | AI 분류 품질을 타협하는가? | 서비스 신뢰 훼손 |

### 1.4 사용자 경험 3대 질문

모든 화면에서 사용자가 아래 세 가지에 즉시 답할 수 있어야 한다:

1. **나는 어디에 있는가?** — 현재 위치를 명확히 표시
2. **무엇을 할 수 있는가?** — 가능한 행동이 즉시 보여야 함
3. **어디로 갈 수 있는가?** — 이동 경로가 명확해야 함

---

## 2. 구현 원칙

### 2.1 Theme 우선 원칙 (하드코딩 금지)

**모든 시각적 속성은 Theme 시스템을 통해 정의하고 사용한다.** 하드코딩된 값은 금지.

```kotlin
// ✅ 올바른 사용법 — Theme 토큰 사용
val colors = KairosTheme.colors
Text(color = colors.text)
Box(Modifier.background(colors.card))

// ❌ 금지 — 하드코딩 색상
Text(color = Color(0xFF111111))
Box(Modifier.background(Color.White))

// ❌ 금지 — Material3 기본 색상 직접 사용
Text(color = MaterialTheme.colorScheme.onSurface)
```

적용 범위:
- **색상**: `KairosTheme.colors` 토큰만 사용. `Color(0xFF...)` 하드코딩 금지
- **타이포그래피**: `KairosTypography`에 정의된 스타일만 사용. 인라인 `fontSize`, `fontWeight` 하드코딩 최소화
- **간격**: 8dp 그리드 시스템 준수. 매직 넘버 대신 일관된 간격 값 사용
- **모서리 반경**: 카드 12dp, 칩 8dp, 바텀 네비 20dp 등 정의된 값 사용

새로운 색상/스타일이 필요한 경우:
1. `KairosColors`에 시맨틱 토큰을 추가한다
2. Light/Dark 양쪽 값을 정의한다
3. 이 문서의 색상 시스템 테이블에 기록한다

### 2.2 라이브러리 우선 원칙 (커스텀 XML/Canvas 최소화)

**직접 XML drawable이나 Canvas로 그리기보다 Compose 라이브러리/Material3 컴포넌트를 우선 사용한다.**

```kotlin
// ✅ 올바른 사용법 — Material3 아이콘 라이브러리 사용
Icon(
    imageVector = Icons.Outlined.CalendarToday,
    contentDescription = "캘린더",
    tint = colors.icon
)

// ❌ 지양 — 커스텀 XML drawable
Icon(
    painter = painterResource(R.drawable.ic_custom_calendar),
    contentDescription = "캘린더"
)
```

| 구분 | 우선 사용 | 지양 |
|------|----------|------|
| **아이콘** | Material Icons (Extended 포함) | 커스텀 XML drawable |
| **버튼** | Material3 `Button`, `TextButton`, `IconButton` + KairosTheme 색상 오버라이드 | `Box` + `clickable`로 버튼 직접 구현 |
| **입력** | Material3 `TextField`, `OutlinedTextField` + 색상 오버라이드 | `BasicTextField` + 직접 데코레이션 |
| **다이얼로그** | Material3 `AlertDialog` + 색상 오버라이드 | 커스텀 팝업 직접 구현 |
| **시트** | Material3 `ModalBottomSheet` | 직접 바텀 시트 구현 |
| **카드** | Compose `Box` + `RoundedCornerShape` + theme 색상 | XML CardView |
| **리스트** | Compose `LazyColumn` | XML RecyclerView |
| **애니메이션** | Compose `animate*AsState`, `AnimatedVisibility`, `AnimatedContent` | XML 애니메이션 리소스 |
| **그래프/차트** | 검증된 Compose 차트 라이브러리 (Vico 등) | Canvas 직접 그리기 |

커스텀 구현이 허용되는 경우:
- 라이브러리에 없는 고유한 인터랙션 (예: 캘린더 타임라인)
- 성능 최적화가 필요한 경우 (프로파일링으로 확인 후)
- 앱 고유 브랜드 요소 (로고 등)

### 2.3 Material3 컴포넌트 사용 규칙

Material3 컴포넌트를 사용하되, **반드시 KairosTheme 색상을 명시적으로 오버라이드**한다:

```kotlin
// ✅ Material3 컴포넌트 + KairosTheme 색상 오버라이드
AlertDialog(
    onDismissRequest = { /* ... */ },
    containerColor = colors.card,
    titleContentColor = colors.text,
    textContentColor = colors.textSecondary,
    // ...
)

OutlinedTextField(
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.border,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        cursorColor = colors.accent,
        focusedPlaceholderColor = colors.placeholder,
        unfocusedPlaceholderColor = colors.placeholder,
    ),
    // ...
)
```

---

## 3. 색상 시스템

### 3.1 라이트 테마 (`KairosLight`)

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

### 3.2 다크 테마 (`KairosDark`)

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

### 3.3 색상 원칙

- 같은 색상을 서로 다른 의미로 사용하지 않는다
- 색상만으로 정보를 전달하지 않는다 — 아이콘, 텍스트 라벨로 보완 (색맹 사용자 고려)
- Light/Dark 모드 모두에서 테스트한다
- 텍스트 대비: 최소 **4.5:1** (WCAG AA)

---

## 4. 타이포그래피

### 4.1 스타일 목록 (`KairosTypography`)

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

### 4.2 폰트 크기 체계

```
11sp → 12sp → 13sp → 14sp → 15sp → 16sp → 20sp → 24sp
```

8단계 크기 체계. 11sp~16sp는 1sp 단위 촘촘하게, 16sp 이후는 큰 점프.

### 4.3 타이포그래피 원칙

- 최소 폰트 크기: **11sp** (그 이하는 가독성 저하)
- 폰트 웨이트와 크기로 계층을 표현 — 무게(weight) 대비가 크기 대비보다 효과적일 수 있다
- 서체 종류를 제한한다 — 시스템 기본 폰트 사용

---

## 5. 간격 시스템

### 5.1 기본 원칙

- **기본 단위**: 8dp (4dp는 세밀한 조정 시 허용)
- **화면 전체 수평 패딩**: 20dp
- **카드 내부 수평 패딩**: 16dp
- **카드 간 수직 간격**: 8dp
- **섹션 간 수직 간격**: 24dp
- **터치 타겟**: 최소 48dp (WCAG 접근성 기준)

### 5.2 자주 사용되는 간격

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

## 6. 공통 컴포넌트

### 6.1 SectionHeader

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
SectionHeader(title = "오늘의 일정")

SectionHeader(
    title = "할 일",
    trailingContent = { Text("3건", color = colors.textMuted) }
)
```

### 6.2 KairosChip

**파일**: `presentation/components/common/KairosChip.kt`

**용도**: AI 분류 결과 표시, 필터 태그

| 상태 | 배경 | 텍스트 색상 |
|------|------|-------------|
| 비선택 | `colors.chipBg` | `colors.chipText` |
| 선택 (라이트) | `colors.accent` (#111111) | `Color.White` |
| 선택 (다크) | `colors.accent` (80% white) | `colors.background` (#0A0A0A) |

**스타일 규칙**:
- 모서리: `RoundedCornerShape(8.dp)`
- 내부 패딩: horizontal 12dp, vertical 6dp
- 폰트: 13sp, `FontWeight.Medium`

### 6.3 KairosBottomNav

**파일**: `presentation/components/common/KairosBottomNav.kt`

**용도**: 앱 하단 플로팅 네비게이션 바

**3탭 구조** (좌 → 우):
1. **NOTES** — `Icons.Outlined.Description` / `Icons.Filled.Description`
2. **HOME** — `Icons.Outlined.Home` / `Icons.Filled.Home` (가운데)
3. **CALENDAR** — `Icons.Outlined.CalendarToday` / `Icons.Filled.CalendarToday`

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

### 6.4 카드 스타일

```kotlin
Box(
    modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(colors.card)
        .border(
            width = 1.dp,
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

---

## 7. UX 패턴

### 7.1 입력 UX

| 원칙 | 적용 |
|------|------|
| 즉시 접근 | 앱 진입 시 입력창이 첫 화면에 위치 |
| 최소 입력 | 텍스트만 입력하면 완료. 부가 정보는 AI가 추론 |
| 입력 보호 | 앱 이탈 시 자동 저장, 재진입 시 복원 |
| 빠른 피드백 | 저장 완료 즉시 시각적 확인 |

### 7.2 분류 UX

| 원칙 | 적용 |
|------|------|
| 자동 우선 | AI가 먼저 분류, 사용자는 필요 시 수정 |
| 시각적 표시 | 분류 결과를 칩으로 명확히 표시 |
| 낮은 수정 마찰 | 칩 탭 → 유형 변경 시트 → 선택 (1~2탭 완료) |
| 안전한 폴백 | 미분류는 Inbox에 저장, 손실 없음 |
| 파생 일관성 | 분류 변경 시 파생 객체 자동 처리 |

### 7.3 리스트 UX

| 원칙 | 적용 |
|------|------|
| AI 제목 우선 | 리스트에는 AI 생성 제목 표시 (원문이 짧으면 원문 그대로) |
| 프리뷰 제공 | 제목과 원문이 다르면 원문 첫 줄을 프리뷰로 표시 |
| 분류 칩 항상 표시 | 어떤 유형인지 즉시 식별 가능 |
| 시간 컨텍스트 | 캡처 시점 또는 마감 시간을 표시 |

### 7.4 삭제 UX

| 원칙 | 적용 |
|------|------|
| 확인 다이얼로그 금지 | "정말 삭제하시겠습니까?" 묻지 않음 |
| 실행 취소 제공 | 삭제 후 Snackbar로 3초간 "실행 취소" 제공 |
| 비파괴적 삭제 | 실행 취소 기간 내 데이터 유지 |

```
Active → SoftDelete(3초 되돌리기) → Trash(30일 보관) → HardDelete(영구 삭제)
```

### 7.5 알림 UX

| 원칙 | 적용 |
|------|------|
| 정보 제공 | 자동 처리 결과를 사후에 알림 (사전 확인 요청 아님) |
| 비침습적 | 앱 내 알림은 콘텐츠를 가리지 않음 |
| 행동 연결 | 푸시 알림 탭 시 해당 항목으로 바로 이동 |
| 일정 제안 1탭 | 일정 제안 알림에서 1탭으로 승인/거부 |

### 7.6 오프라인 UX

| 원칙 | 적용 |
|------|------|
| 캡처 항상 성공 | 네트워크 상태와 무관하게 로컬 저장 |
| 상태 명시 | 오프라인 상태를 앱 내에서 명확히 표시 |
| 자동 복구 | 네트워크 복구 시 AI 분류 자동 재시도 |

---

## 8. 인터랙션 패턴

### 8.1 네비게이션

| 패턴 | 설명 |
|------|------|
| **Bottom Nav (플랫)** | 하단 3탭 (Notes, Home, Calendar). 항상 표시 (모달 제외) |
| **Hierarchical (계층적)** | TopAppBar + Back 버튼. 오른쪽으로 깊어지고, 왼쪽으로 복귀 |
| **Modal (모달)** | 특정 작업에 집중. 시트/다이얼로그. 완료/취소로 닫음 |

원칙:
- Bottom Nav는 순수 네비게이션 용도로만 사용 (시트 트리거 금지)
- 모달은 독립적 작업에만 사용 (계층 이동에 사용 금지)
- 파괴적 액션(삭제 등)은 `colors.danger` 색상 사용

### 8.2 체크박스

- 형태: 둥근 사각형 (`RoundedCornerShape`)
- 터치 타겟: 48dp
- 시각적 크기: 22dp
- 완료 시: `colors.accent` 배경 + 체크 아이콘

### 8.3 스와이프

- 캘린더 확장/축소: 상하 스와이프
- 월 이동: 좌우 스와이프

### 8.4 애니메이션과 모션

| 원칙 | 적용 |
|------|------|
| 의도적 사용 | 모션은 경험 향상 목적만. 그 자체가 목적이 되면 안 됨 |
| 선택적 | 중요 정보를 모션만으로 전달하지 않음 |
| 자연스러운 감속 | 기본 이징 사용, 지속 시간 0.2~0.5초 |
| Reduce Motion 대응 | 접근성 설정 존중 |

Compose 애니메이션 API:
- `AnimatedVisibility`: 요소의 표시/숨김 전환
- `AnimatedContent`: 콘텐츠 교체 전환
- `animateColorAsState`: 색상 전환
- `animateDpAsState`: 크기/간격 전환

---

## 9. UX 라이팅

### 9.1 톤

| 원칙 | 설명 | 예시 |
|------|------|------|
| 간결함 | 불필요한 단어 제거 | ❌ "성공적으로 저장되었습니다" → ✅ "저장됨" |
| 행동 중심 | 상태보다 행동 안내 | ❌ "오프라인 상태입니다" → ✅ "오프라인 — 연결되면 자동 동기화" |
| 긍정적 | 오류도 긍정적으로 표현 | ❌ "분류 실패" → ✅ "Inbox에 저장됨 — 연결 후 자동 분류" |

### 9.2 용어 통일

| 개념 | 용어 | 피해야 할 표현 |
|------|------|--------------|
| 기록 행위 | 캡처 | 메모, 입력, 작성 |
| 임시 저장소 | Inbox | 미분류함, 대기함 |
| 아이디어 분류 | ideas | 아이디어함, 발상함 |
| AI 재분류 | 재정리 | 다시 정리, 리밸런싱 |
| 삭제 복구 | 실행 취소 | 되돌리기, 복원 |
| 일정 확인 요청 | 제안 | 추천, 알림 |
| 분류 유형 표시 | 칩 | 태그, 뱃지, 라벨 |

### 9.3 에러 메시지

| 상황 | 메시지 | 원칙 |
|------|--------|------|
| 네트워크 오류 | "오프라인 — Inbox에 저장됨" | 문제 + 결과를 함께 |
| AI 타임아웃 | "분류 중 — 잠시 후 다시 시도합니다" | 진행 상태 표시 |
| 미지원 파일 | "텍스트와 이미지만 지원됩니다" | 대안 없이 간결하게 |
| 캘린더 연동 실패 | "캘린더 추가 실패 — 다시 시도합니다" | 자동 재시도 안내 |

---

## 10. 접근성

| 요구사항 | 적용 |
|----------|------|
| 터치 타겟 | 모든 인터랙티브 요소 최소 48×48dp |
| 텍스트 대비 | 일반 텍스트 4.5:1 이상, 큰 텍스트 3:1 이상 |
| 최소 폰트 | 11sp (그 이하 금지) |
| 색상 외 정보 전달 | 색상만으로 의미 전달 금지, 아이콘/텍스트로 보완 |
| 콘텐츠 설명 | 모든 아이콘에 `contentDescription` 제공 |

---

## 11. 금지 사항 체크리스트

새로운 UI 코드를 작성하거나 리뷰할 때 아래 항목을 확인:

**Theme/색상**
- [ ] `MaterialTheme.colorScheme.*`를 직접 사용하지 않았는가? (반드시 `KairosTheme.colors` 사용)
- [ ] 하드코딩 `Color(0xFF...)` 값을 사용하지 않았는가? (색상 토큰만 사용)
- [ ] `AlertDialog`에 `containerColor = colors.card` 등 KairosTheme 색상을 지정했는가?
- [ ] `OutlinedTextField`에 KairosTheme 색상을 명시적으로 적용했는가?
- [ ] 새로운 색상이 필요한 경우 `KairosColors`에 토큰으로 추가했는가?

**타이포그래피/간격**
- [ ] 텍스트 스타일은 `KairosTypography`에 정의된 스타일을 사용했는가?
- [ ] 인라인 `fontSize`, `fontWeight` 하드코딩을 최소화했는가?
- [ ] 간격 값은 8dp 그리드 시스템을 따르는가?

**라이브러리 사용**
- [ ] 커스텀 XML drawable 대신 Material Icons를 사용했는가?
- [ ] XML 애니메이션 대신 Compose 애니메이션 API를 사용했는가?
- [ ] `Box` + `clickable`로 버튼을 직접 구현하지 않고, Material3 Button 계열을 사용했는가?
- [ ] Canvas로 직접 그리기 전에 라이브러리 대안을 검토했는가?

**컴포넌트/인터랙션**
- [ ] 카드 모서리는 `RoundedCornerShape(12.dp)`를 사용했는가?
- [ ] 모든 인터랙티브 요소(버튼, 체크박스, 탭)가 48dp 이상 터치 타겟을 확보했는가?
- [ ] 삭제 시 확인 다이얼로그 대신 Snackbar 실행 취소를 사용했는가?
- [ ] Light/Dark 양쪽 모드에서 테스트했는가?

**UX 원칙**
- [ ] 이 UI가 캡처까지의 단계를 늘리지 않는가?
- [ ] 사용자에게 불필요한 선택을 강요하지 않는가?
- [ ] 용어가 UX 라이팅 가이드와 일치하는가?

---

*Document Version: 2.0 | Last Updated: 2026-02-12*
