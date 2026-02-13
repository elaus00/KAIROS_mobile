# State Hoisting 리팩토링 인사이트 (2026-02-13)

## 배경: 왜 처음엔 ViewModel 직접 전달 패턴을 사용했는가?

### 초기 개발 시점의 합리적 선택
1. **개발 속도 우선**
   - Flit 프로젝트는 Phase 1~3을 거치며 빠르게 기능을 구축해야 했음
   - Screen에서 ViewModel을 직접 전달하면 보일러플레이트 코드가 줄어듦 (파라미터 명시 불필요)
   - Composable 시그니처가 단순해짐 (`viewModel: XxxViewModel` 하나면 끝)

2. **Hilt 편의성 활용**
   - `@HiltViewModel` + `hiltViewModel()`으로 자동 주입
   - 별도 state wrapping 없이 `viewModel.uiState.collectAsState()` 직접 사용
   - 테스트 시 Hilt 자동 주입 편리함

3. **당시엔 문제가 없었음**
   - Preview가 복잡하지 않았음 (Mock ViewModel 생성 가능)
   - 테스트가 단순했음 (ViewModel 유닛 테스트 중심)
   - 리컴포지션 최적화 요구사항이 낮았음

## 문제: 왜 리팩토링이 필요했는가?

### 1. Preview 작성의 어려움
- ViewModel을 직접 전달하면 Preview에서 ViewModel 인스턴스 생성 필요
- 복잡한 의존성(Repository, UseCase)을 모두 Mock 해야 함
- 여러 상태 시나리오(Empty, Loading, Error, Success)를 보여주기 어려움

**예시: 기존 방식**
```kotlin
@Preview
@Composable
fun CaptureContentPreview() {
    // ViewModel 생성이 불가능 → Preview 포기 or Mock 지옥
    CaptureContent(viewModel = ???)
}
```

### 2. 테스트 복잡도 증가
- UI 테스트(ComposeTestRule)에서 ViewModel 주입 필요
- 스크린샷 테스트에서 다양한 상태 검증 어려움
- State만 바꿔서 테스트하기 어려움 (ViewModel 전체를 교체해야 함)

### 3. 리컴포지션 범위 확대
- ViewModel 내부 state가 변경되면 전체 Screen이 리컴포지션됨
- State만 받는 Content는 해당 state 변경 시만 리컴포지션 가능
- 성능 최적화가 어려움

### 4. 재사용성 제로
- ViewModel이 강하게 결합된 Composable은 다른 곳에서 재사용 불가
- 같은 UI를 다른 데이터 소스로 보여줄 수 없음

## 해결: Screen/Content 분리 패턴

### 구조
```kotlin
// Screen: ViewModel 주입 + State 수집 (얇은 레이어)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CaptureContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail
    )
}

// Content: 순수 UI, State만 받음 (테스트/Preview 가능)
@Composable
fun CaptureContent(
    uiState: CaptureUiState,
    onEvent: (CaptureUiEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // UI 구현
}
```

### 장점
1. **Preview 작성 가능**
   - State 객체만 생성하면 되므로 간단
   - 다양한 상태 시나리오를 쉽게 보여줄 수 있음
   ```kotlin
   @Preview
   @Composable
   fun CaptureContentEmptyPreview() {
       FlitTheme {
           CaptureContent(
               uiState = CaptureUiState(captures = emptyList()),
               onEvent = {},
               onNavigateToDetail = {}
           )
       }
   }
   ```

2. **테스트 단순화**
   - 스크린샷 테스트: State만 주입하면 끝
   - UI 테스트: ViewModel 없이 Content만 테스트 가능
   - 상태별 UI 검증이 명확해짐

3. **리컴포지션 최적화**
   - Content는 uiState가 변경될 때만 리컴포지션
   - Screen은 State 수집만 담당 (최소 리컴포지션)

4. **재사용성 향상**
   - Content는 어디서든 재사용 가능
   - 다른 데이터 소스(Mock, Fake, Real)로 UI 테스트 가능

## 리팩토링 판단 기준

### State Hoisting을 적용해야 할 때
- [ ] Preview 작성이 필요한 경우
- [ ] 스크린샷 테스트가 필요한 경우
- [ ] 다양한 상태 시나리오를 검증해야 할 때
- [ ] UI가 복잡하고 리컴포지션 최적화가 필요할 때
- [ ] 같은 UI를 다른 곳에서 재사용해야 할 때

### ViewModel 직접 전달이 허용되는 경우
- [ ] 매우 단순한 화면 (설정 토글 등)
- [ ] Preview/테스트가 불필요한 경우
- [ ] 일회성 화면으로 재사용 가능성이 없을 때
- [ ] 성능 최적화가 필요 없을 때

## 교훈

### 1. 정답은 없다, 트레이드오프만 있다
- 초기 개발 시 ViewModel 직접 전달은 **옳은 선택**이었음
- 프로젝트가 성숙하면서 요구사항이 변경됨 (Preview, 테스트, 성능)
- 패턴은 **현재 요구사항에 맞춰** 선택해야 함

### 2. 점진적 리팩토링이 핵심
- 모든 화면을 한 번에 바꿀 필요 없음
- 우선순위가 높은 화면부터 (핵심 화면, 복잡한 화면)
- Batch 단위로 나눠서 진행 (Batch A/B/C)

### 3. 테스트 가능성이 핵심 지표
- Preview 작성이 어렵다 = State Hoisting 고려 신호
- 스크린샷 테스트가 필요하다 = State Hoisting 필수
- UI 테스트가 복잡하다 = Content 분리 필요

### 4. 멀티 에이전트 작업 시 주의사항
- Screen/Content 분리는 **기계적인 작업**이므로 에이전트에게 적합
- 하지만 **공유 파일 충돌** 주의 (NavGraph, AppFontScale 등)
- 에이전트별로 독립적인 파일 세트 할당이 최적

## 참조
- Design Guide: `docs/specs/design-guide.md`
- Philosophy & Principles: `docs/direction/philosophy_principles.md`
- Apple HIG: `.claude/skills/apple_hig_guide/SKILL.md`
- 리팩토링 커밋: 2026-02-13 (Batch A/B/C 완료)
