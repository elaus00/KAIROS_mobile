# Test-Driven Development (TDD)

테스트 주도 개발 워크플로우를 강제하여 품질 높은 코드를 작성하는 스킬입니다.

## 핵심 원칙

**"Write the test first. Watch it fail. Write minimal code to pass."**

테스트가 실패하는 것을 직접 확인하는 것이 필수입니다. 이것이 테스트가 실제로 의도한 동작을 검증한다는 것을 증명합니다.

## 철칙 (Iron Law)

**프로덕션 코드는 반드시 실패하는 테스트가 먼저 있어야 합니다.**

테스트 전에 작성된 코드는 완전히 삭제해야 합니다. 재사용하거나 참조해서는 안 됩니다.

## Red-Green-Refactor 사이클

### 1. RED - 실패하는 테스트 작성
- 원하는 동작을 보여주는 최소한의 테스트 하나 작성
- 테스트는 명확하고 단일 동작만 검증

### 2. VERIFY RED - 실패 확인 (필수!)
- 테스트를 실행하여 올바르게 실패하는지 확인
- 이 단계를 건너뛰면 안 됨 - 실패를 확인해야 테스트가 유효함을 증명

### 3. GREEN - 통과하는 최소 코드 작성
- 테스트를 통과시키는 가장 간단한 코드 구현
- 과도한 엔지니어링 금지

### 4. VERIFY GREEN - 통과 확인
- 모든 테스트가 통과하는지 확인
- 새 테스트와 기존 테스트 모두 확인

### 5. REFACTOR - 리팩토링
- 코드 정리 및 개선
- 테스트는 계속 통과해야 함

## TDD를 적용해야 하는 경우

**항상 적용:**
- 새로운 기능 개발
- 버그 수정
- 리팩토링
- 동작 변경

**예외는 팀 리더 승인 필요**

## 좋은 테스트의 요구사항

**단일 동작:**
- 테스트 하나당 하나의 동작만 검증
- 여러 assert가 있더라도 같은 동작의 다른 측면 검증

**서술적 이름:**
- 무엇을 테스트하는지 명확히 표현
- `test_submit_should_save_to_database()` (Good)
- `testSubmit()` (Bad)

**실제 코드 사용:**
- Mock은 최소한으로만 사용
- 실제 구현을 테스트해야 의미 있음

**명확한 API 시연:**
- 테스트가 원하는 API 사용법을 보여줘야 함

## 거부해야 할 합리화

**"나중에 테스트 작성하면 되지"**
→ 나중에 작성한 테스트는 즉시 통과하므로 아무것도 증명하지 못함

**"현실적으로 불가능해"**
→ TDD는 오히려 개발 속도를 높임. 디버깅 시간이 대폭 감소

**"수동 테스트로 충분해"**
→ 수동 테스트는 체계적이지 않고 반복할 수 없음

**"이미 작성한 코드 아까워"**
→ 검증되지 않은 코드는 기술 부채. 삭제하는 것이 더 나음

## 검증 체크리스트

코드 작업 완료 전 확인:

- [ ] 모든 함수에 테스트가 있음
- [ ] 각 테스트는 구현 전에 실패했음
- [ ] 모든 테스트가 통과함
- [ ] 출력이 깨끗함 (로그 스팸 없음)
- [ ] 실제 코드를 테스트함 (과도한 Mock 없음)
- [ ] 엣지 케이스가 커버됨

## Kotlin/Android 예시

### Example 1: Repository 테스트

```kotlin
class CaptureRepositoryTest {

    @Test
    fun `submitCapture should save to database`() {
        // RED: 먼저 테스트 작성 (실패 확인)
        val repository = CaptureRepositoryImpl(mockDao, mockApi)
        val capture = Capture(content = "Test")

        // When
        val result = runBlocking { repository.submitCapture(capture) }

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(mockDao).insertCapture(any())
    }
}
```

### Example 2: ViewModel 테스트

```kotlin
class CaptureViewModelTest {

    @Test
    fun `onSubmit should update isLoading state`() = runTest {
        // RED: 먼저 테스트 작성
        val viewModel = CaptureViewModel(mockUseCase)

        // When
        viewModel.onSubmit()

        // Then
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }
}
```

### Example 3: UseCase 테스트

```kotlin
class SubmitCaptureUseCaseTest {

    @Test
    fun `invoke should return error for empty content`() {
        // RED: 먼저 테스트 작성 (실패 확인)
        val useCase = SubmitCaptureUseCase(mockRepository)

        // When
        val result = runBlocking { useCase("") }

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
    }
}
```

## TDD 워크플로우 실전

**1단계: 테스트 작성**
```kotlin
@Test
fun `extractText should return text from image`() {
    val processor = OcrProcessor()
    val imageUri = mockUri

    val result = runBlocking { processor.extractText(imageUri) }

    assertThat(result).isInstanceOf(Result.Success::class.java)
    assertThat((result as Result.Success).data).isNotEmpty()
}
```

**2단계: 실행 → 실패 확인 ✓**

**3단계: 최소 구현**
```kotlin
class OcrProcessor {
    suspend fun extractText(imageUri: Uri): Result<String> {
        return Result.Success("") // 일단 빈 문자열
    }
}
```

**4단계: 실행 → 통과 확인 ✓**

**5단계: 리팩토링 + 실제 구현**
```kotlin
class OcrProcessor @Inject constructor() {
    private val recognizer = TextRecognition.getClient(...)

    suspend fun extractText(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(inputImage).await()
            Result.Success(result.text)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

## 일반적인 실수

**테스트 없이 코드 먼저 작성**
→ TDD가 아님. 코드 삭제하고 다시 시작

**실패 확인 안 함**
→ 테스트가 유효한지 알 수 없음. 반드시 실패를 확인

**한 번에 여러 기능 테스트**
→ 작은 단위로 나눠서 진행

**Mock 과도 사용**
→ 실제 통합 테스트도 작성해야 함

---

TDD는 단순한 테스팅 기법이 아니라, 더 나은 설계와 더 신뢰할 수 있는 코드를 만드는 개발 방법론입니다. 처음에는 느리게 느껴질 수 있지만, 장기적으로는 개발 속도와 코드 품질을 모두 향상시킵니다.
