# Frontend Design

독창적이고 프로덕션급 프론트엔드 인터페이스를 만드는 스킬입니다.

## 핵심 원칙

**"Create distinctive, production-grade frontend interfaces with high design quality."**

일반적인 AI 미학을 거부하고, 기억에 남는 독창적인 디자인을 추구합니다.

## 설계 사고 프로세스

### 1. 컨텍스트 이해

**질문해야 할 것들:**
- 용도: 무엇을 위한 UI인가?
- 대상 사용자: 누가 사용하는가?
- 기술적 제약: 어떤 플랫폼/프레임워크인가?
- 브랜드: 어떤 느낌을 전달해야 하는가?

### 2. 미학적 방향 선택

**스타일 예시:**
- 미니멀리즘 (Minimalism)
- 맥시멀리즘 (Maximalism)
- 레트로-퓨처리즘 (Retro-Futurism)
- 글래스모피즘 (Glassmorphism)
- 뉴모피즘 (Neumorphism)
- 브루탈리즘 (Brutalism)

**중요:** 스타일을 명확히 선택하고 일관되게 유지

### 3. 차별점 정의

**기억에 남는 요소:**
- 독특한 색상 조합
- 특별한 타이포그래피
- 인상적인 애니메이션
- 비대칭 레이아웃
- 커스텀 일러스트/아이콘

## 구현 기준

### 프로덕션급 코드
- 실제로 작동하는 코드
- 성능 최적화됨
- 접근성 고려
- 반응형 디자인

### 시각적으로 인상적
- 첫눈에 눈길을 사로잡음
- 세부 사항까지 세심함
- 일관된 디자인 시스템

### 일관된 미학
- 모든 요소가 같은 스토리를 전달
- 색상, 타이포그래피, 간격이 조화로움

## 미적 가이드라인

### 타이포그래피

**선택 원칙:**
- 아름답고, 독특하고, 흥미로운 폰트 선택
- 제네릭 폰트 회피 (Arial, Inter, Helvetica 등)

**Jetpack Compose 예시:**
```kotlin
val FlitTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,  // 독특한 선택
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 색상

**응집력 있는 팔레트:**
```kotlin
val DeepPurple80 = Color(0xFFD1C4E9)
val DeepPurple40 = Color(0xFF512DA8)
val Amber400 = Color(0xFFFFB74D)
val Blue400 = Color(0xFF64B5F6)
val Green400 = Color(0xFF81C784)

// CSS 변수처럼 Material Theme 활용
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.secondary
MaterialTheme.colorScheme.tertiary
```

**명확한 악센트색:**
- 주요 CTA에 대비되는 색상
- 브랜드 정체성 반영

### 모션 (Motion)

**애니메이션 원칙:**
- CSS/Compose 애니메이션 우선
- 한 번의 잘 조율된 페이지 로드 애니메이션
- 산발적인 마이크로 인터랙션보다 효과적

**Compose 예시:**
```kotlin
val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
)

Box(modifier = Modifier.alpha(animatedAlpha)) {
    // 콘텐츠
}
```

### 공간 구성 (Spatial Composition)

**독창적 레이아웃:**
- 비대칭 배치
- 요소 간 오버랩
- 대각선 흐름
- 그리드 파괴 요소

**예시:**
```kotlin
Box {
    // 배경 레이어
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .offset(y = 20.dp),  // 의도적 오프셋
        color = MaterialTheme.colorScheme.primaryContainer
    ) {}

    // 전경 카드 (오버랩)
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // 콘텐츠
    }
}
```

### 배경 효과

**분위기 연출:**
- 그래디언트
```kotlin
Brush.verticalGradient(
    colors = listOf(
        Color(0xFF512DA8),
        Color(0xFF7B1FA2)
    )
)
```

- 텍스처/패턴
- 커스텀 셰이프
```kotlin
RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 0.dp,
    bottomStart = 0.dp,
    bottomEnd = 24.dp
)
```

## 피해야 할 것들

### ❌ 제네릭 AI 미학
- 모든 것이 둥근 모서리
- 파스텔 그래디언트 과용
- 센터 정렬만 사용
- 일반적인 카드 레이아웃

### ❌ 예측 가능한 레이아웃
- 같은 Material Components만 사용
- 창의성 없는 그리드
- 평범한 네비게이션

### ❌ 맥락 무시
- 브랜드 정체성과 맞지 않음
- 타겟 사용자 고려 안 함
- 플랫폼 가이드라인 무시

## Jetpack Compose 디자인 패턴

### 1. 커스텀 테마

```kotlin
@Composable
fun FlitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FlitTypography,  // 커스텀 타이포그래피
        content = content
    )
}
```

### 2. 독창적인 컴포넌트

```kotlin
@Composable
fun UniqueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),  // 비대칭
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 1.2.sp  // 독특한 간격
        )
    }
}
```

### 3. 애니메이션 조합

```kotlin
@Composable
fun AnimatedCard(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f
    )

    Card(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}
```

## 디자인 검증 체크리스트

작업 완료 전 확인:

- [ ] 명확한 미학적 방향이 있는가?
- [ ] 제네릭 폰트를 사용하지 않았는가?
- [ ] 색상 팔레트가 응집력 있는가?
- [ ] 독창적인 레이아웃 요소가 있는가?
- [ ] 애니메이션이 의도적이고 조율되었는가?
- [ ] 타겟 사용자와 브랜드에 맞는가?
- [ ] 프로덕션 품질의 코드인가?
- [ ] 접근성 기준을 만족하는가?

---

이 스킬을 사용하여 UI를 구현할 때는 항상 "왜 이 디자인인가?"라는 질문을 스스로에게 던지고, 모든 선택이 의도적이고 맥락에 맞도록 하세요. 제네릭한 AI 디자인을 거부하고, 기억에 남는 독창적인 경험을 만드세요.
