# Compose 드래그 가능한 카드의 클리핑 이슈

**날짜**: 2026-02-15
**상황**: TaskList 드래그 시 카드가 잘리는 문제

## 문제 상황

할 일 카드를 long press 드래그로 순서 변경할 때, 드래그 중인 카드가 원래 위치의 클리핑 영역을 벗어나면서 잘려서 보이는 현상 발생.

## 원인 분석

### 기존 코드 구조

```kotlin
Box(
    modifier = Modifier
        .zIndex(if (isDragging) 1f else 0f)
        .clip(cardShape)  // ← 문제의 원인
        .graphicsLayer {
            shape = cardShape
            clip = false  // graphicsLayer 내부 클리핑만 제어, Modifier.clip()과는 별개
            if (isDragging) {
                translationY = dragOffsetY  // 드래그 중 수직 이동
            }
        }
) {
    SwipeableCard(
        modifier = modifier.clip(cardShape)  // ← 중복 클리핑
    ) {
        TaskItemContent(...)  // 내부에도 .clip() 있음
    }
}
```

### 핵심 원인

1. **중첩된 클리핑**: 외부 Box, SwipeableCard, 내부 콘텐츠에 각각 `.clip()` 적용
2. **translationY와 클리핑의 충돌**:
   - 드래그 중 `graphicsLayer { translationY = ... }`로 카드가 수직 이동
   - 외부 Box의 `.clip(cardShape)`는 원래 위치 기준으로 클리핑 영역 고정
   - 카드가 이동하면 클리핑 영역을 벗어나면서 잘림
3. **`graphicsLayer { clip = false }`의 한계**:
   - graphicsLayer 내부의 `shape` 클리핑만 제어
   - Modifier 체인의 `.clip()`과는 독립적으로 작동

## 해결 방법

### 외부 컨테이너의 클리핑 제거

```kotlin
Box(
    modifier = Modifier
        .zIndex(if (isDragging) 1f else 0f)
        // .clip(cardShape) 제거 — 드래그 시 클리핑 방지
        .graphicsLayer {
            shape = cardShape
            clip = false
            if (isDragging) {
                translationY = dragOffsetY
            }
        }
) {
    SwipeableCard(
        modifier = modifier  // clip 제거 — 내부 Box들이 이미 클리핑 처리
    ) {
        TaskItemContent(
            modifier = Modifier.clip(cardShape)  // 내부 콘텐츠만 클리핑 유지
        )
    }
}
```

### 변경 사항

1. **TaskList 외부 Box** (라인 136): `.clip(cardShape)` 제거
2. **SwipeableCard 외부 Box** (라인 53): `.clip(cardShape)` 제거
3. **내부 콘텐츠 Box**: `.clip()` 유지 (모서리를 둥글게 만드는 용도)

### 결과

- 드래그 중에도 카드가 잘리지 않음
- 내부 Box들이 클리핑하므로 모서리는 여전히 둥글게 유지
- `zIndex`로 드래그 중인 카드가 다른 카드 위에 표시됨

## 교훈

### 1. Compose 클리핑의 계층 이해

- **Modifier.clip()**: Modifier 체인에서 클리핑 적용, 레이아웃 좌표 기준
- **graphicsLayer { clip = ... }**: graphicsLayer 내부에서 `shape`에 대한 클리핑 제어
- 두 가지는 **독립적**으로 작동하며, `graphicsLayer { clip = false }`로 Modifier.clip()을 해제할 수 없음

### 2. 드래그 가능한 UI의 클리핑 전략

- **외부 컨테이너**: 클리핑 없음 (드래그 이동 공간 확보)
- **내부 콘텐츠**: 필요한 곳만 클리핑 (모서리, 배경 등)
- **중복 클리핑 제거**: 여러 레이어에서 같은 shape로 클리핑하는 것은 불필요하며 문제를 일으킬 수 있음

### 3. graphicsLayer와 Modifier의 차이

- `graphicsLayer { translationY = ... }`: 시각적 변환만 적용, 레이아웃은 변경하지 않음
- Modifier 체인의 클리핑은 레이아웃 좌표 기준이므로, graphicsLayer 변환과 함께 사용 시 주의 필요

### 4. SwipeableCard 같은 재사용 컴포넌트

- 재사용 컴포넌트는 외부 클리핑을 최소화하고, 사용처에서 필요에 따라 적용하도록 설계
- 내부 배경/전경만 클리핑 처리하여 외부 컨텍스트(드래그, 애니메이션 등)와의 충돌 방지

## 적용 범위

- **TaskList**: 할 일 카드 드래그 ✅
- **ScheduleTimeline**: 일정 카드는 이미 외부 Box에 `.clip()` 없음 ✅
- 향후 드래그 가능한 카드 컴포넌트 설계 시 참고

## 관련 파일

- `app/src/main/java/com/flit/app/presentation/calendar/components/TaskList.kt`
- `app/src/main/java/com/flit/app/presentation/components/common/SwipeableCard.kt`
- `app/src/main/java/com/flit/app/presentation/calendar/components/ScheduleTimeline.kt`
