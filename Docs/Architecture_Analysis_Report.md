# KAIROS Mobile 프로젝트 아키텍처 종합 평가 보고서

## 📊 Executive Summary

**프로젝트 성숙도**: **7.0/10** (Good - Production-ready with critical fixes)

**Overall Assessment**: KAIROS Mobile은 **견고한 아키텍처 기반** 위에 구축되었으나, **운영 환경 배포 전 필수 개선사항**이 존재합니다. Clean Architecture 원칙을 잘 따르고 있으며, Offline-first 전략이 우수하나, **Error handling과 Testing이 치명적 약점**입니다.

| 평가 영역 | 점수 | 상태 | 비고 |
|---------|------|------|------|
| **Architecture Design** | 8.5/10 | ✅ Excellent | 1개 위반사항 수정 필요 |
| **Data Layer** | 8.0/10 | ✅ Good | Sync 활성화 필요 |
| **DI & Modularity** | 8.7/10 | ✅ Excellent | Test module 추가 필요 |
| **Error Handling** | 4.8/10 | ❌ Critical | Crash reporting 부재 |
| **Testing** | 1.0/10 | ❌ Critical | 95% 미테스트 |
| **Code Quality** | 7.5/10 | ✅ Good | 중복 코드 정리 필요 |

---

## 🚨 CRITICAL PATH - 즉시 수정 필요 (Top 3)

### 1. ❌ Crash Reporting & Monitoring 부재 (Priority: P0)

**문제**:
- 운영 환경 오류 추적 불가능
- 사용자 crash 발생 시 원인 파악 불가
- Error rate, API 성능 모니터링 부재

**Business Impact**:
- 사용자 이탈 증가 (crash 후 원인 모름)
- 버그 재현 불가 → 수정 지연
- App Store 평점 하락 위험

**해결 방안** (1-2일):
```kotlin
// 1. Firebase Crashlytics 추가
// build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}

// 2. KairosApplication.kt 수정
@HiltAndroidApp
class KairosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Global exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("thread", thread.name)
                recordException(throwable)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        // Error tracking
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}

// 3. Repository에 error logging 추가
catch (e: Exception) {
    Log.e(TAG, "Operation failed", e)
    FirebaseCrashlytics.getInstance().recordException(e)
    Result.Error(e)
}
```

---

### 2. ❌ Testing Coverage 극히 낮음 (Priority: P0)

**문제**:
- 전체 코드의 **95%가 테스트 없음**
- Repository (455 lines), ViewModel (416 lines) 모두 0% coverage
- Refactoring 시 regression 위험 높음

**Business Impact**:
- 버그 발견이 운영 환경에서만 가능
- 코드 변경 시 side effect 예측 불가
- 개발 속도 저하 (수동 테스트 반복)

**해결 방안** (1주):

**Week 1: Use Case Tests (6 tests)**
```kotlin
// domain/usecase/SubmitCaptureUseCaseTest.kt
class SubmitCaptureUseCaseTest {
    
    private val mockRepository: CaptureRepository = mock()
    private lateinit var useCase: SubmitCaptureUseCase
    
    @Before
    fun setup() {
        useCase = SubmitCaptureUseCase(mockRepository)
    }
    
    @Test
    fun `빈 content 입력 시 에러 반환`() = runTest {
        // When
        val result = useCase("")
        
        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IllegalArgumentException)
    }
    
    @Test
    fun `유효한 content 입력 시 repository 호출`() = runTest {
        // Given
        val content = "Valid content"
        whenever(mockRepository.submitCapture(content))
            .thenReturn(Result.Success(mockCapture))
        
        // When
        val result = useCase(content)
        
        // Then
        verify(mockRepository).submitCapture(content)
        assertTrue(result is Result.Success)
    }
}
```

**Week 2: Repository Tests (10 tests)**
```kotlin
// data/repository/CaptureRepositoryImplTest.kt
class CaptureRepositoryImplTest {
    
    private val mockApi: KairosApi = mock()
    private val mockDao: CaptureQueueDao = mock()
    private val mockConfig: ConfigRepository = mock()
    private lateinit var repository: CaptureRepositoryImpl
    
    @Test
    fun `오프라인 시 로컬 저장 후 PENDING 상태 반환`() = runTest {
        // Given
        whenever(mockConfig.isNetworkAvailable()).thenReturn(false)
        
        // When
        val result = repository.submitCapture("test")
        
        // Then
        verify(mockDao).insertCapture(any())
        assertTrue((result as Result.Success).data.syncStatus == SyncStatus.PENDING)
    }
}
```

**Target**: 2주 내 **50% coverage** 달성

---

### 3. ❌ Presentation Layer의 Architecture 위반 (Priority: P0)

**문제**:
```kotlin
// CaptureViewModel.kt - VIOLATION
import com.example.kairos_mobile.data.processor.VoiceRecognizer  // ❌

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val voiceRecognizer: VoiceRecognizer  // ❌ Data layer 직접 의존
) : ViewModel()
```

**Business Impact**:
- ViewModel 테스트 불가 (data layer mock 필요)
- 아키텍처 무너짐 (다른 개발자가 패턴 따라함)
- 코드 결합도 증가 → 유지보수 어려움

**해결 방안** (1일):

```kotlin
// 1. domain/usecase/RecordVoiceUseCase.kt 생성
@Singleton
class RecordVoiceUseCase @Inject constructor(
    private val voiceRecognizer: VoiceRecognizer
) {
    fun startRecording(): kotlin.Result<Unit> =
        voiceRecognizer.startRecording()
    
    fun stopRecording(): kotlin.Result<Unit> =
        voiceRecognizer.stopRecording()
    
    suspend fun uploadAndTranscribe(): Result<String> =
        voiceRecognizer.uploadAndTranscribe()
    
    fun cleanup() = voiceRecognizer.cleanup()
}

// 2. CaptureViewModel.kt 수정
@HiltViewModel
class CaptureViewModel @Inject constructor(
    // ... 기존 use cases
    private val recordVoiceUseCase: RecordVoiceUseCase  // ✅ Domain layer
) : ViewModel() {
    
    fun onStartVoiceRecording() {
        val result = recordVoiceUseCase.startRecording()  // ✅
        // ...
    }
}
```

---

## 🎯 전략적 개선 로드맵

### Phase 1: 긴급 수정 (1-2주)

**Week 1: Critical Fixes**
1. ✅ Firebase Crashlytics 통합 (1일)
2. ✅ `RecordVoiceUseCase` 생성 (1일)
3. ✅ Use Case unit tests 작성 (3일)

**Week 2: Infrastructure Setup**
4. ✅ WorkManager sync 활성화 (1일)
5. ✅ Database indexes 추가 (1일)
6. ✅ Repository unit tests 작성 (3일)

**Expected Outcome**:
- Crash tracking 가능
- Architecture 위반 해결
- 30% test coverage 달성

---

### Phase 2: 구조 개선 (3-4주)

**Week 3: Error Handling Refactoring**
1. Error hierarchy 생성
```kotlin
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class NetworkError(val code: Int, override val message: String) : AppError(message)
    data class DatabaseError(override val message: String) : AppError(message)
    data class ValidationError(override val message: String) : AppError(message)
    data class ProcessingError(override val message: String) : AppError(message)
}
```

2. Retry logic with exponential backoff
```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 100,
    maxDelayMs: Long = 2000,
    factor: Double = 2.0,
    block: suspend () -> Result<T>
): Result<T> {
    var currentDelay = initialDelayMs
    repeat(maxRetries) { attempt ->
        when (val result = block()) {
            is Result.Success -> return result
            is Result.Error -> {
                if (attempt == maxRetries - 1) return result
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
            Result.Loading -> {}
        }
    }
    return Result.Error(Exception("Max retries exceeded"))
}
```

**Week 4: Repository Refactoring**
3. CaptureRepositoryImpl 중복 제거
```kotlin
// 공통 패턴 추출
private suspend fun submitCaptureInternal(
    capture: Capture,
    content: String,
    preprocessor: (suspend () -> String)? = null
): Result<Capture> {
    // 1. 전처리 (OCR/STT/WebClip)
    val processedContent = preprocessor?.invoke() ?: content
    
    // 2. 로컬 저장
    dao.insertCapture(captureMapper.toEntity(capture))
    
    // 3. 네트워크 확인
    if (!configRepository.isNetworkAvailable()) {
        return Result.Success(capture)
    }
    
    // 4. 분류 + Obsidian 생성
    return retryWithBackoff {
        val classification = classifyCapture(processedContent).getOrThrow()
        createObsidianNote(classification, processedContent).getOrThrow()
        Result.Success(capture.copy(
            classification = classification,
            syncStatus = SyncStatus.SYNCED
        ))
    }
}

// 간결해진 메서드들
override suspend fun submitImageCapture(imageUri: Uri): Result<Capture> =
    submitCaptureInternal(
        capture = Capture(source = CaptureSource.IMAGE),
        content = "",
        preprocessor = { ocrProcessor.extractText(imageUri).getOrThrow() }
    )
```

**Expected Outcome**:
- 코드 중복 70% 감소
- Error recovery 자동화
- 50% test coverage 달성

---

### Phase 3: 확장성 확보 (5-8주)

**Week 5-6: Testing Maturity**
1. ViewModel tests (12 tests)
2. Compose UI tests (8 tests)
3. Integration tests (6 tests)
4. **Target: 70% coverage**

**Week 7: Performance Optimization**
1. Database query 최적화 (indexes, pagination)
2. Network timeout 세분화 (endpoint별)
3. Circuit breaker pattern 추가

**Week 8: Monitoring & Analytics**
1. Error metrics tracking
2. Performance monitoring (API response time)
3. User analytics (feature usage)

**Expected Outcome**:
- Production-grade quality
- Scalable architecture
- Complete observability

---

## 📋 Architecture Decision Records (ADRs)

### ✅ 잘된 결정 (Keep)

**1. Offline-First Pattern**
- **결정**: 모든 capture를 로컬 DB에 먼저 저장 후 sync
- **근거**: 데이터 유실 방지, 사용자 경험 개선
- **결과**: SyncStatus tracking으로 신뢰성 확보
- **권장**: 유지하고 WorkManager로 백그라운드 sync 강화

**2. Clean Architecture with Use Cases**
- **결정**: Domain layer 격리, Use case pattern 적용
- **근거**: Testability, Maintainability
- **결과**: 대부분의 layer가 명확히 분리됨
- **권장**: RecordVoiceUseCase 추가로 완벽히 준수

**3. Result<T> Sealed Class**
- **결정**: Exception 대신 Result로 error propagation
- **근거**: Type-safe, composable error handling
- **결과**: 명시적 error 처리, null 회피
- **권장**: Error hierarchy 추가로 더 강화

---

### ⚠️ 재고 필요 (Reconsider)

**1. CaptureRepository 단일 인터페이스**
- **현재**: 10개 메서드가 하나의 인터페이스에
- **문제**: Interface Segregation Principle 위반
- **개선안**:
```kotlin
// 3개 인터페이스로 분리
interface CaptureSubmissionRepository {
    suspend fun submitCapture(content: String): Result<Capture>
    suspend fun submitImageCapture(imageUri: Uri): Result<Capture>
    // ...
}

interface CaptureClassificationRepository {
    suspend fun classifyCapture(content: String): Result<Classification>
    suspend fun suggestTags(...): Result<List<SuggestedTag>>
}

interface CaptureSyncRepository {
    suspend fun syncOfflineQueue(): Result<Int>
    fun getPendingCaptures(): Flow<List<Capture>>
}
```

**2. Processor 네이밍 불일치**
- **현재**: `VoiceRecognizer`, `OcrProcessor`, `WebClipper`
- **문제**: 일관성 부족, 역할 불명확
- **개선안**: 모두 `*Processor`로 통일
```kotlin
VoiceRecognizer → AudioProcessor
OcrProcessor → ImageProcessor
WebClipper → WebProcessor
```

**3. MockKairosApi in Production Code**
- **현재**: Mock implementation이 main source에 있음
- **문제**: Test code와 production code 혼재
- **개선안**: Test source로 이동 + Qualifier 사용
```kotlin
// NetworkModule.kt
@Provides @Singleton @MockApi
fun provideMockApi(): KairosApi = MockKairosApi()

@Provides @Singleton @RealApi
fun provideRealApi(retrofit: Retrofit): KairosApi = 
    retrofit.create(KairosApi::class.java)
```

---

## 🔧 기술 부채 우선순위

### 🔴 HIGH Priority (지금 안 고치면 큰 문제)

1. **Crash Reporting 부재**
   - Impact: 운영 환경 오류 추적 불가
   - Effort: 1일
   - ROI: ★★★★★

2. **Architecture Violation (VoiceRecognizer)**
   - Impact: 아키텍처 무너짐, 테스트 불가
   - Effort: 1일
   - ROI: ★★★★★

3. **Testing Coverage 5%**
   - Impact: Regression 위험, 개발 속도 저하
   - Effort: 2주
   - ROI: ★★★★☆

4. **WorkManager Sync 비활성화**
   - Impact: Offline captures 영구 pending
   - Effort: 1일
   - ROI: ★★★★★

5. **Retry Logic 부재**
   - Impact: 일시적 네트워크 오류 시 sync 실패
   - Effort: 2일
   - ROI: ★★★★☆

---

### 🟡 MEDIUM Priority (곧 문제가 될 것)

6. **Database Indexes 없음**
   - Impact: 쿼리 성능 저하 (데이터 증가 시)
   - Effort: 1일
   - ROI: ★★★☆☆

7. **코드 중복 (4개 submit 메서드)**
   - Impact: 유지보수 비용 증가
   - Effort: 3일
   - ROI: ★★★☆☆

8. **Error Categorization 부재**
   - Impact: 에러 타입별 처리 불가
   - Effort: 2일
   - ROI: ★★★☆☆

9. **Network Timeout 미설정**
   - Impact: API 호출 hang 가능
   - Effort: 1일
   - ROI: ★★★★☆

10. **Result Type 불일치**
    - Impact: 개발자 혼란, 일관성 부족
    - Effort: 1일
    - ROI: ★★☆☆☆

---

### 🟢 LOW Priority (나중에 리팩토링)

11. **CaptureRepository Interface 분리**
    - Impact: ISP 위반, 테스트 복잡도 증가
    - Effort: 3일
    - ROI: ★★☆☆☆

12. **Processor 네이밍 통일**
    - Impact: 가독성 저하
    - Effort: 1일
    - ROI: ★☆☆☆☆

13. **Circuit Breaker Pattern**
    - Impact: 반복 실패 시 배터리 소모
    - Effort: 2일
    - ROI: ★★☆☆☆

---

## 💡 구체적 코드 개선안

### 1. CaptureRepositoryImpl 리팩토링

**Before** (524 lines, 8 dependencies):
```kotlin
class CaptureRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val dao: CaptureQueueDao,
    private val configRepository: ConfigRepository,
    private val captureMapper: CaptureMapper,
    private val classificationMapper: ClassificationMapper,
    private val ocrProcessor: OcrProcessor,
    private val webClipper: WebClipper,
    private val dispatcher: CoroutineDispatcher
) : CaptureRepository {
    // 455 lines of business logic...
}
```

**After** (분리된 구조):
```kotlin
// 1. Service layer 분리
@Singleton
class CaptureProcessingService @Inject constructor(
    private val ocrProcessor: OcrProcessor,
    private val webClipper: WebClipper
) {
    suspend fun processImage(uri: Uri): Result<String> =
        ocrProcessor.extractText(uri)
    
    suspend fun processWebUrl(url: String): Result<WebMetadata> =
        webClipper.extractMetadata(url)
}

@Singleton
class CaptureClassificationService @Inject constructor(
    private val api: KairosApi,
    private val classificationMapper: ClassificationMapper
) {
    suspend fun classify(content: String): Result<Classification> =
        retryWithBackoff {
            val response = api.classifyCapture(ClassificationRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(classificationMapper.toDomain(response.body()!!))
            } else {
                Result.Error(NetworkError(response.code(), "Classification failed"))
            }
        }
}

// 2. Repository는 orchestration만
@Singleton
class CaptureRepositoryImpl @Inject constructor(
    private val dao: CaptureQueueDao,
    private val configRepository: ConfigRepository,
    private val processingService: CaptureProcessingService,
    private val classificationService: CaptureClassificationService,
    private val dispatcher: CoroutineDispatcher
) : CaptureRepository {
    
    override suspend fun submitImageCapture(imageUri: Uri): Result<Capture> =
        withContext(dispatcher) {
            // 1. Process
            val content = processingService.processImage(imageUri).getOrElse {
                return@withContext Result.Error(it)
            }
            
            // 2. Submit
            submitCaptureInternal(
                Capture(content = content, source = CaptureSource.IMAGE)
            )
        }
    
    private suspend fun submitCaptureInternal(capture: Capture): Result<Capture> {
        // Common submission logic (100 lines → reused)
    }
}
```

**Benefits**:
- 의존성 감소: 8개 → 5개
- 코드 크기 감소: 524 lines → ~300 lines
- 테스트 용이성 증가 (각 service 독립 테스트)

---

### 2. Error Handling 표준화

**Before** (불일치):
```kotlin
// VoiceRecognizer - kotlin.Result
fun startRecording(): kotlin.Result<Unit>

// Repository - custom Result
suspend fun classifyCapture(): Result<Classification>

// ViewModel - 직접 exception handling
try { ... } catch (e: Exception) { ... }
```

**After** (통일):
```kotlin
// 1. Error hierarchy
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class NetworkError(val code: Int, override val message: String, cause: Throwable? = null) 
        : AppError(message, cause)
    
    data class DatabaseError(override val message: String, cause: Throwable? = null) 
        : AppError(message, cause)
    
    data class ValidationError(override val message: String) 
        : AppError(message)
    
    data class ProcessingError(val processorType: String, override val message: String, cause: Throwable? = null) 
        : AppError(message, cause)
}

// 2. Result extension functions
inline fun <T> Result<T>.onSuccess(crossinline block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

inline fun <T> Result<T>.onError(crossinline block: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) block(exception)
    return this
}

inline fun <T, R> Result<T>.map(crossinline transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        Result.Loading -> Result.Loading
    }

// 3. 사용 예시
repository.submitCapture(content)
    .onSuccess { capture ->
        _uiState.update { it.copy(showSuccessFeedback = true) }
    }
    .onError { error ->
        val message = when (error) {
            is NetworkError -> "네트워크 오류: ${error.message}"
            is ValidationError -> error.message
            else -> "알 수 없는 오류가 발생했습니다"
        }
        _uiState.update { it.copy(errorMessage = message) }
    }
```

---

### 3. Testing Strategy (70% Coverage 달성)

**2주 계획**:

**Week 1: Foundation (30% coverage)**
```kotlin
// Day 1-2: Use Cases (6 tests)
SubmitCaptureUseCaseTest
SubmitImageCaptureUseCaseTest
SubmitVoiceCaptureUseCaseTest
SubmitWebClipUseCaseTest
GetPendingCapturesUseCaseTest
SyncOfflineQueueUseCaseTest

// Day 3-4: Mappers (2 tests)
CaptureMapperTest
ClassificationMapperTest

// Day 5: Test Utilities
// - Test data builders
// - Fake implementations
// - Base test classes
```

**Week 2: Core Logic (70% coverage)**
```kotlin
// Day 1-3: Repository (10 tests)
CaptureRepositoryImplTest
- submitCapture() online/offline
- classifyCapture() success/failure
- syncOfflineQueue()
- error handling
- retry logic

// Day 4-5: ViewModel (12 tests)
CaptureViewModelTest
- State transitions
- User interactions
- Error handling
- Loading states
```

**Test Template**:
```kotlin
class CaptureRepositoryImplTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var repository: CaptureRepositoryImpl
    private val mockApi: KairosApi = mock()
    private val mockDao: CaptureQueueDao = mock()
    private val mockConfig: ConfigRepository = mock()
    
    @Before
    fun setup() {
        repository = CaptureRepositoryImpl(
            api = mockApi,
            dao = mockDao,
            configRepository = mockConfig,
            // ... other dependencies
        )
    }
    
    @Test
    fun `오프라인 시 로컬 저장 후 pending 상태 반환`() = runTest {
        // Given
        whenever(mockConfig.isNetworkAvailable()).thenReturn(false)
        whenever(mockDao.insertCapture(any())).thenReturn(Unit)
        
        // When
        val result = repository.submitCapture("test content")
        
        // Then
        verify(mockDao).insertCapture(any())
        verify(mockApi, never()).classifyCapture(any())
        assertTrue(result is Result.Success)
        assertEquals(SyncStatus.PENDING, (result as Result.Success).data.syncStatus)
    }
    
    @Test
    fun `온라인 시 분류 실패해도 로컬에는 저장됨`() = runTest {
        // Given
        whenever(mockConfig.isNetworkAvailable()).thenReturn(true)
        whenever(mockApi.classifyCapture(any()))
            .thenReturn(Response.error(500, "".toResponseBody()))
        
        // When
        val result = repository.submitCapture("test")
        
        // Then
        verify(mockDao).insertCapture(any())
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data.classification)
    }
}
```

---

## 🎯 Action Items (2주 내 완료)

### Week 1: Critical Fixes
- [ ] Day 1: Firebase Crashlytics 통합
- [ ] Day 2: RecordVoiceUseCase 생성 + Architecture 위반 수정
- [ ] Day 3-4: Use Case unit tests (6개)
- [ ] Day 5: WorkManager sync 활성화 + Database indexes

### Week 2: Quality Improvements
- [ ] Day 1-2: Error hierarchy + Retry logic 구현
- [ ] Day 3-5: Repository & ViewModel tests (22개)

### Verification
```bash
# Coverage 확인
./gradlew jacocoTestReport
# Target: 50% coverage

# Build 성공 확인
./gradlew assembleDebug testDebugUnitTest

# Crashlytics 연동 확인
# Firebase Console에서 test crash 발생 확인
```

---

## 📌 최종 권고사항

### 즉시 실행 (이번 주)
1. ✅ Firebase Crashlytics 추가 (1일)
2. ✅ RecordVoiceUseCase 생성 (1일)
3. ✅ WorkManager 활성화 (1일)

### 다음 스프린트 (2주)
4. ✅ Testing coverage 50% 달성
5. ✅ Error handling 표준화
6. ✅ Repository 리팩토링

### 장기 목표 (1-2개월)
7. ✅ Testing coverage 70% 달성
8. ✅ Performance monitoring 추가
9. ✅ Code quality automation (lint, detekt)

**현재 아키텍처는 견고하나, 운영 안정성 확보를 위해 Error Handling과 Testing이 시급합니다.**
