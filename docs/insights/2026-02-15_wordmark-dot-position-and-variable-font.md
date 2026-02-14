# FlitWordmark 도트 위치 & 가변 폰트 교훈

> **날짜**: 2026-02-15
> **세션**: UI 개선 (키보드/워드마크/용어)

---

## 1. brand-identity.md의 CSS 설명과 SVG 글리프 좌표 불일치

### 문제
`brand-identity.md`의 dotOffset 값들은 CSS `position:relative; top:Xpx` 동작으로 설명됨.
이를 그대로 Compose `Layout`에 구현하면 도트가 베이스라인 **아래**로 크게 밀려남.

### 원인
SVG 워드마크(`wordmark-dark-on-transparent.svg`)의 실제 글리프 좌표를 분석하면:
- 폰트 좌표계: y=0 = 베이스라인, y 위로 증가
- 도트 circle: center y=61, radius=85 → **도트 중심이 베이스라인 위 61/1000 em**
- 도트 하단: y = 61-85 = -24 → 베이스라인 아래 24/1000 em (아주 살짝)

CSS 설명의 `top:13px`은 "inline-block 하단이 baseline에 정렬 → 아래로 13px 이동"인데,
이 해석이 **SVG 글리프와 일치하지 않음**. CSS 설명이 잘못된 것.

### 올바른 계산법
```
dotOffset = dotSize/2 - 61/1000 * fontSize
```
- 결과: 모든 사이즈에서 0.5~1.5dp (기존 4~19dp 대비 대폭 축소)
- 도트 중심이 베이스라인 약간 위, 하단이 베이스라인 약간 아래 = SVG와 일치

### 교훈
**디자인 스펙 문서의 CSS 설명보다 SVG 원본 좌표가 ground truth**.
구현 시 SVG 좌표를 직접 역산하여 검증해야 함.

---

## 2. Compose 가변 폰트에 FontVariation.Settings 필수

### 문제
```kotlin
// ❌ 기본 weight 400으로 렌더링됨
val SoraFontFamily = FontFamily(Font(R.font.sora_variable))
```

### 해결
```kotlin
// ✅ weight 축 480이 실제 적용됨
@OptIn(ExperimentalTextApi::class)
val SoraFontFamily = FontFamily(
    Font(
        R.font.sora_variable,
        weight = FontWeight(480),
        variationSettings = FontVariation.Settings(
            FontVariation.weight(480)
        )
    )
)
```

- `weight` 파라미터: FontFamily **매칭**용 (어떤 weight 요청이 이 Font를 선택할지)
- `variationSettings`: 실제 **렌더링**용 (폰트 엔진에 weight 축 값 전달)
- BOM 2024.09.00에서 `variationSettings`는 아직 `@ExperimentalTextApi`

### 교훈
가변 폰트를 `Font(resId)`만으로 등록하면 기본 weight로 렌더링됨.
**`variationSettings`를 명시적으로 설정해야** 원하는 weight 축이 적용됨.

---

## 3. Scaffold paddingValues + imePadding() 이중 적용

### 문제
```
Scaffold(bottomBar = BottomNav) { paddingValues ->
    Column(Modifier.padding(paddingValues)) {
        // 내부에서 .imePadding() 사용 시:
        // 총 바텀 패딩 = 바텀 네비 높이 + 키보드 높이 → 이중 적용
    }
}
```

### 해결
```kotlin
Column(
    modifier = Modifier
        .padding(paddingValues)
        .consumeWindowInsets(paddingValues)  // 하위 imePadding()에 소비 정보 전달
)
```

`consumeWindowInsets(paddingValues)` 추가 시, 하위 `imePadding()`이 이미 소비된 인셋을 차감하여
`max(0, imeHeight - bottomNavHeight)`만 추가 패딩으로 적용.

### 교훈
Scaffold 내부에서 `imePadding()` 사용 시 **반드시 `consumeWindowInsets`와 함께** 사용해야 이중 패딩 방지.

---

## 4. Roborazzi(Robolectric) 가변 폰트 렌더링 한계

Roborazzi 스크린샷은 Robolectric 환경에서 렌더링되므로:
- `FontVariation.weight(480)` 축이 정확히 반영되지 않을 수 있음
- 폰트 weight 비교는 **실기기에서만 유효**
- 도트 위치/크기 비교는 Roborazzi에서도 대략 유효
