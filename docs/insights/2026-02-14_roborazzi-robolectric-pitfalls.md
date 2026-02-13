# Roborazzi + Robolectric 스크린샷 테스트 함정 모음

> 2026-02-14 세션에서 발견한 비자명한 문제들

## 1. AndroidKeyStore 미지원

Robolectric은 `AndroidKeyStore`를 지원하지 않는다. `EncryptedSharedPreferences`를 사용하는 코드가 Composable 트리에 있으면 `KeyStoreException: AndroidKeyStore not found`로 크래시한다.

**해결**: Hilt EntryPoint를 통한 Repository 접근 시, EntryPoint 생성뿐 아니라 **Repository 인스턴스 생성까지** `remember` 블록의 try-catch 안에 포함해야 한다. EntryPoint 자체는 Robolectric에서 성공하지만, DI 그래프를 통한 실제 객체 생성 시 AndroidKeyStore 의존성에서 실패한다.

## 2. FocusRequester 타이밍 문제

`LaunchedEffect(Unit) { focusRequester.requestFocus() }`는 Robolectric에서 `IllegalStateException: FocusRequester is not initialized`를 발생시킨다. Modifier.focusRequester()가 적용되기 전에 LaunchedEffect가 실행되기 때문.

**해결**: `try { focusRequester.requestFocus() } catch (_: IllegalStateException) { }` 방어 코드 추가.

## 3. hiltViewModel()을 포함하는 Content는 테스트 불가

Screen/Content 분리를 했더라도, Content 함수 내부에서 자식 Composable이 `hiltViewModel()`을 호출하면 Robolectric에서 렌더링 불가. MainContent가 NotesContent/CalendarContent를 내부에 포함하는 경우가 이에 해당.

**해결**: 해당 컨테이너 스크린은 스크린샷 테스트에서 제외하고, 내부 자식 Content를 개별 테스트로 대체.

## 4. 기본 해상도가 매우 낮음 (320x470)

`robolectric.properties`에 qualifiers를 지정하지 않으면 mdpi 기본 해상도로 렌더링된다.

**해결**: `qualifiers=w412dp-h915dp-xxxhdpi` 설정으로 Pixel 7급 해상도(1648x3660) 확보.

## 5. 멀티 에이전트의 모델 생성자 추측 문제

에이전트가 data class 생성자를 확인하지 않고 추측으로 작성하면 필드명, 타입, 필수 파라미터가 모두 틀릴 수 있다. 특히 Preview와 스크린샷 테스트에서 반복적으로 발생.

**교훈**: 에이전트에게 테스트/Preview 작성을 위임할 때, "실제 data class 정의를 먼저 읽고 생성자를 작성하라"는 명시적 지시가 필요하다.

## 6. Roborazzi API 버전 차이

`RoborazziOptions.CaptureType.LastImage()`나 `RobolectricDeviceQualifiers` 같은 클래스는 특정 버전에서만 존재하거나 아예 존재하지 않는다. LLM이 추측으로 작성하기 쉬운 영역.

**교훈**: Roborazzi 옵션은 최소한으로 설정하고(`RoborazziOptions()` 기본값), 필요한 경우에만 확인된 API를 추가한다.
