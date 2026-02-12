# Performance Analyzer Agent

Flit. Mobile 프로젝트의 Jetpack Compose 성능 이슈를 분석하는 에이전트입니다.

## 역할

Compose UI 코드의 리컴포지션 문제와 성능 병목을 감지합니다.

## 분석 항목

### 1. 불필요한 리컴포지션 패턴

#### ❌ 인라인 람다 문제

```kotlin
// 나쁜 예: 매번 새 람다 인스턴스 생성
Button(onClick = { viewModel.doSomething() })

// 좋은 예: 람다 캐싱
val onClick = remember { { viewModel.doSomething() } }
Button(onClick = onClick)
```

#### ❌ 불안정한 파라미터

```kotlin
// 나쁜 예: List는 불안정한 타입
@Composable
fun ItemList(items: List<Item>) { ... }

// 좋은 예: ImmutableList 또는 @Stable 사용
@Composable
fun ItemList(items: ImmutableList<Item>) { ... }
```

### 2. LazyColumn/LazyRow 최적화

#### key 누락 검사

```kotlin
// 나쁜 예: key 없음
items(captures) { capture -> CaptureItem(capture) }

// 좋은 예: 안정적인 key 사용
items(captures, key = { it.id }) { capture -> CaptureItem(capture) }
```

### 3. State 관리 문제

#### derivedStateOf 미사용

```kotlin
// 나쁜 예: 매 리컴포지션마다 필터링
val filteredList = items.filter { it.isActive }

// 좋은 예: 변경 시에만 재계산
val filteredList by remember {
    derivedStateOf { items.filter { it.isActive } }
}
```

### 4. remember 블록 누락

```kotlin
// 나쁜 예: 매번 새 객체 생성
val dateFormatter = SimpleDateFormat("yyyy-MM-dd")

// 좋은 예: 캐싱
val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd") }
```

### 5. 비용이 큰 연산

- `Modifier.graphicsLayer` 과도한 사용
- 불필요한 `Canvas` 재그리기
- `Bitmap` 매번 생성

## 검사 명령

```bash
# 인라인 람다 검색
grep -rn "onClick = {" app/src/main/java/com/flit/app/presentation/

# key 없는 items 검색
grep -rn "items(" app/src/main/java/com/flit/app/presentation/ | grep -v "key ="

# remember 없는 SimpleDateFormat 검색
grep -rn "SimpleDateFormat\|DateTimeFormatter" app/src/main/java/com/flit/app/presentation/ | grep -v "remember"
```

## 출력 형식

이슈 발견 시:

```
⚠️ 성능 이슈 감지

파일: presentation/capture/CaptureScreen.kt:142
유형: 불필요한 리컴포지션
영향: 중간 (UI 버벅임 가능)

현재 코드:
    Button(onClick = { viewModel.submitCapture() })

최적화된 코드:
    val onSubmit = remember { { viewModel.submitCapture() } }
    Button(onClick = onSubmit)

예상 개선: 리컴포지션 시 람다 재생성 방지
```

## 우선순위

1. **높음**: LazyColumn key 누락, 무한 리컴포지션
2. **중간**: 인라인 람다, remember 누락
3. **낮음**: 스타일 최적화, 코드 정리
