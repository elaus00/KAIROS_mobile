# KAIROS Magic Inbox - Phase 2 구현 계획 (서버 중심 아키텍처)

## 문서 정보
- **버전**: 3.0
- **작성일**: 2026-01-23
- **아키텍처**: 서버 중심 (Thin Client)
- **예상 개발 기간**: 5일

---

## 1. 개요

### 1.1 현재 상태
- **Phase 1 완료**: 텍스트 Quick Capture + AI 분류 + Obsidian 연동
- **Glassmorphism UI 완료**: 애니메이션 배경, Glass 스타일 컴포넌트

### 1.2 Phase 2 목표
멀티모달 입력 지원 (서버 처리)
- **M05**: 이미지 캡처 + 서버측 OCR
- **M06**: 음성 녹음 + 서버측 STT
- **M07**: 공유 인텐트 처리
- **M08**: URL 입력 + 서버측 웹 크롤링

### 1.3 아키텍처 원칙

**클라이언트 역할** (Thin Client):
- UI/UX 렌더링
- 파일 캡처 (이미지, 음성)
- 멀티파트 파일 업로드
- 서버 응답 표시
- 오프라인 큐잉 (Room Database)
- 동기화 관리

**서버 역할** (KAIROS Backend):
- OCR (이미지 → 텍스트)
- STT (음성 → 텍스트)
- 웹 크롤링 및 메타데이터 추출
- AI 콘텐츠 분류
- Obsidian 노트 생성
- 모든 비즈니스 로직

---

## 2. 서버 API 명세

### 2.1 멀티모달 Capture 엔드포인트

#### POST `/api/v1/captures/multimodal`

**요청 (Multipart Form Data)**:
```kotlin
// Content-Type: multipart/form-data

fields:
- "source": String           // "TEXT", "IMAGE", "VOICE", "WEB_CLIP"
- "content": String?         // TEXT/WEB_CLIP의 경우 텍스트 내용
- "imageFile": File?         // IMAGE의 경우 이미지 파일
- "audioFile": File?         // VOICE의 경우 오디오 파일
- "metadata": String (JSON)  // 추가 메타데이터
```

**응답**:
```json
{
  "captureId": "uuid-string",
  "source": "IMAGE",
  "extractedContent": "OCR로 추출된 텍스트...",
  "classification": {
    "type": "TODO",
    "destinationPath": "Obsidian/tasks/",
    "title": "회의록 정리하기",
    "tags": ["work", "meeting"],
    "confidence": 0.92
  },
  "obsidianNote": {
    "path": "tasks/20260123_회의록_정리하기.md",
    "created": true
  },
  "processedAt": "2026-01-23T10:30:00Z"
}
```

**에러 응답**:
```json
{
  "error": "OCR_FAILED",
  "message": "이미지에서 텍스트를 추출할 수 없습니다",
  "code": 400
}
```

### 2.2 기존 엔드포인트 활용

기존 Phase 1 엔드포인트는 그대로 유지:
- `POST /api/v1/classify` - 텍스트 분류만 필요한 경우
- `POST /api/v1/obsidian/note` - Obsidian 노트 생성만 필요한 경우

---

## 3. 클라이언트 구현 범위

### 3.1 Domain Layer 확장

#### CaptureSource.kt (새 파일)
```kotlin
package com.example.kairos_mobile.domain.model

/**
 * 캡처 소스 타입
 */
enum class CaptureSource {
    TEXT,       // 텍스트 입력
    IMAGE,      // 이미지 캡처 (서버에서 OCR)
    VOICE,      // 음성 녹음 (서버에서 STT)
    WEB_CLIP    // URL 입력 (서버에서 크롤링)
}
```

#### Capture.kt 확장
```kotlin
data class Capture(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val source: CaptureSource = CaptureSource.TEXT,

    // 멀티모달 메타데이터
    val imageUri: String? = null,      // 로컬 이미지 URI
    val audioUri: String? = null,      // 로컬 오디오 URI
    val webUrl: String? = null,        // 입력된 URL

    // 서버 응답 데이터
    val extractedContent: String? = null,  // 서버가 추출한 텍스트

    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val classification: Classification? = null,
    val error: String? = null
)
```

### 3.2 Data Layer

#### KairosApi.kt 확장
```kotlin
interface KairosApi {

    /**
     * 멀티모달 캡처 제출 (서버에서 OCR/STT/크롤링 처리)
     */
    @Multipart
    @POST("/api/v1/captures/multimodal")
    suspend fun submitMultimodalCapture(
        @Part("source") source: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imageFile: MultipartBody.Part?,
        @Part audioFile: MultipartBody.Part?,
        @Part("metadata") metadata: RequestBody?
    ): Response<MultimodalCaptureResponse>

    // 기존 엔드포인트 유지
    @POST("/api/v1/classify")
    suspend fun classifyCapture(@Body request: ClassifyRequest): Response<ClassificationResponse>

    @POST("/api/v1/obsidian/note")
    suspend fun createObsidianNote(@Body request: CreateNoteRequest): Response<CreateNoteResponse>
}
```

#### MultimodalCaptureResponse.kt (새 파일)
```kotlin
data class MultimodalCaptureResponse(
    val captureId: String,
    val source: String,
    val extractedContent: String?,  // 서버가 추출한 텍스트
    val classification: ClassificationResponse,
    val obsidianNote: ObsidianNoteInfo?,
    val processedAt: String
)

data class ObsidianNoteInfo(
    val path: String,
    val created: Boolean
)
```

#### CaptureRepositoryImpl 확장
```kotlin
override suspend fun submitImageCapture(imageUri: Uri): Result<Capture> {
    return withContext(Dispatchers.IO) {
        try {
            // 1. 로컬 저장 (오프라인 대비)
            val localCapture = Capture(
                content = "",  // 서버가 추출할 예정
                source = CaptureSource.IMAGE,
                imageUri = imageUri.toString(),
                syncStatus = SyncStatus.PENDING
            )
            captureQueueDao.insert(localCapture.toEntity())

            // 2. 네트워크 체크
            if (!networkMonitor.isOnline()) {
                return@withContext Result.Success(localCapture)
            }

            // 3. 멀티파트 파일 준비
            val file = uriToFile(imageUri)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("imageFile", file.name, requestBody)

            val sourceBody = "IMAGE".toRequestBody("text/plain".toMediaTypeOrNull())

            // 4. 서버 업로드 및 처리
            val response = kairosApi.submitMultimodalCapture(
                source = sourceBody,
                content = null,
                imageFile = imagePart,
                audioFile = null,
                metadata = null
            )

            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!

                // 5. 서버 응답으로 로컬 업데이트
                val updatedCapture = localCapture.copy(
                    extractedContent = serverData.extractedContent,
                    classification = serverData.classification.toDomain(),
                    syncStatus = SyncStatus.SYNCED
                )
                captureQueueDao.update(updatedCapture.toEntity())

                Result.Success(updatedCapture)
            } else {
                throw ApiException("Server error: ${response.code()}")
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun submitVoiceCapture(audioUri: Uri): Result<Capture> {
    return withContext(Dispatchers.IO) {
        try {
            val localCapture = Capture(
                content = "",
                source = CaptureSource.VOICE,
                audioUri = audioUri.toString(),
                syncStatus = SyncStatus.PENDING
            )
            captureQueueDao.insert(localCapture.toEntity())

            if (!networkMonitor.isOnline()) {
                return@withContext Result.Success(localCapture)
            }

            val file = uriToFile(audioUri)
            val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audioFile", file.name, requestBody)

            val sourceBody = "VOICE".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = kairosApi.submitMultimodalCapture(
                source = sourceBody,
                content = null,
                imageFile = null,
                audioFile = audioPart,
                metadata = null
            )

            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!
                val updatedCapture = localCapture.copy(
                    extractedContent = serverData.extractedContent,
                    classification = serverData.classification.toDomain(),
                    syncStatus = SyncStatus.SYNCED
                )
                captureQueueDao.update(updatedCapture.toEntity())
                Result.Success(updatedCapture)
            } else {
                throw ApiException("Server error: ${response.code()}")
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun submitWebClip(url: String): Result<Capture> {
    return withContext(Dispatchers.IO) {
        try {
            val localCapture = Capture(
                content = url,
                source = CaptureSource.WEB_CLIP,
                webUrl = url,
                syncStatus = SyncStatus.PENDING
            )
            captureQueueDao.insert(localCapture.toEntity())

            if (!networkMonitor.isOnline()) {
                return@withContext Result.Success(localCapture)
            }

            val sourceBody = "WEB_CLIP".toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = url.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = kairosApi.submitMultimodalCapture(
                source = sourceBody,
                content = contentBody,
                imageFile = null,
                audioFile = null,
                metadata = null
            )

            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!
                val updatedCapture = localCapture.copy(
                    extractedContent = serverData.extractedContent,
                    classification = serverData.classification.toDomain(),
                    syncStatus = SyncStatus.SYNCED
                )
                captureQueueDao.update(updatedCapture.toEntity())
                Result.Success(updatedCapture)
            } else {
                throw ApiException("Server error: ${response.code()}")
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 3.3 Database 마이그레이션

#### CaptureQueueEntity.kt (Version 2)
```kotlin
@Entity(tableName = "capture_queue")
data class CaptureQueueEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "source") val source: String = "TEXT",           // 새 필드
    @ColumnInfo(name = "image_uri") val imageUri: String? = null,       // 새 필드
    @ColumnInfo(name = "audio_uri") val audioUri: String? = null,       // 새 필드
    @ColumnInfo(name = "web_url") val webUrl: String? = null,           // 새 필드
    @ColumnInfo(name = "extracted_content") val extractedContent: String? = null,  // 서버 추출 텍스트
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: String,
    @ColumnInfo(name = "classification_type") val classificationType: String?,
    @ColumnInfo(name = "classification_path") val classificationPath: String?,
    @ColumnInfo(name = "classification_title") val classificationTitle: String?,
    @ColumnInfo(name = "tags") val tags: String?,  // JSON
    @ColumnInfo(name = "confidence") val confidence: Float?,
    @ColumnInfo(name = "metadata") val metadata: String?,  // JSON
    @ColumnInfo(name = "error") val error: String?
)
```

#### KairosDatabase.kt 마이그레이션
```kotlin
@Database(
    entities = [CaptureQueueEntity::class],
    version = 2,
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    abstract fun captureQueueDao(): CaptureQueueDao

    companion object {
        const val DATABASE_NAME = "kairos_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN source TEXT NOT NULL DEFAULT 'TEXT'"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN image_uri TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN audio_uri TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_url TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN extracted_content TEXT"
                )
            }
        }
    }
}
```

### 3.4 Presentation Layer

#### CaptureMode.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.capture

/**
 * UI 캡처 모드 (사용자가 선택하는 입력 방식)
 */
enum class CaptureMode {
    TEXT,      // 텍스트 직접 입력
    IMAGE,     // 카메라/갤러리
    VOICE,     // 음성 녹음
    WEB_CLIP   // URL 입력
}
```

#### CaptureUiState 확장
```kotlin
data class CaptureUiState(
    val inputText: String = "",
    val captureMode: CaptureMode = CaptureMode.TEXT,  // 현재 선택된 모드
    val selectedImageUri: Uri? = null,
    val selectedAudioUri: Uri? = null,
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,
    val showSuccessFeedback: Boolean = false,
    val errorMessage: String? = null,
    val pendingCount: Int = 0,
    val isOffline: Boolean = false
)
```

#### CaptureViewModel 확장
```kotlin
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val submitImageCaptureUseCase: SubmitImageCaptureUseCase,
    private val submitVoiceCaptureUseCase: SubmitVoiceCaptureUseCase,
    private val submitWebClipUseCase: SubmitWebClipUseCase,
    // ...
) : ViewModel() {

    fun onCaptureModeChanged(mode: CaptureMode) {
        _uiState.update { it.copy(captureMode = mode) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = submitImageCaptureUseCase(uri)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            showSuccessFeedback = true,
                            isLoading = false,
                            selectedImageUri = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.exception.message ?: "이미지 처리 실패",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onVoiceRecorded(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = submitVoiceCaptureUseCase(uri)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            showSuccessFeedback = true,
                            isLoading = false,
                            selectedAudioUri = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.exception.message ?: "음성 처리 실패",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onWebUrlEntered(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = submitWebClipUseCase(url)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            showSuccessFeedback = true,
                            isLoading = false,
                            inputText = ""
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.exception.message ?: "웹 클립 처리 실패",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // M07: 공유 인텐트 처리
    fun onSharedTextReceived(sharedText: String) {
        if (isUrl(sharedText)) {
            // URL이면 WEB_CLIP 모드로 처리
            onCaptureModeChanged(CaptureMode.WEB_CLIP)
            onWebUrlEntered(sharedText)
        } else {
            // 일반 텍스트면 TEXT 모드로 처리
            _uiState.update { it.copy(inputText = sharedText) }
        }
    }

    fun onSharedImageReceived(imageUri: Uri) {
        onCaptureModeChanged(CaptureMode.IMAGE)
        onImageSelected(imageUri)
    }

    private fun isUrl(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }
}
```

### 3.5 UI Components

기존 Glassmorphism UI 유지하되, 모드 선택 버튼만 추가:

#### GlassCaptureCard.kt 수정
```kotlin
// 기존 코드에서 모드 선택 버튼 추가
Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    GlassModeButton(
        icon = Icons.Default.Image,
        contentDescription = "이미지 캡처",
        onClick = { onModeSelected(CaptureMode.IMAGE) }
    )

    GlassModeButton(
        icon = Icons.Default.Mic,
        contentDescription = "음성 캡처",
        onClick = { onModeSelected(CaptureMode.VOICE) }
    )

    GlassModeButton(
        icon = Icons.Default.Link,
        contentDescription = "웹 클립",
        onClick = { onModeSelected(CaptureMode.WEB_CLIP) }
    )
}
```

### 3.6 권한 처리

#### AndroidManifest.xml
```xml
<!-- 카메라 권한 -->
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-permission android:name="android.permission.CAMERA" />

<!-- 마이크 권한 -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- 공유 인텐트 필터 -->
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
        <data android:mimeType="image/*" />
    </intent-filter>
</activity>
```

#### CaptureScreen.kt - 권한 처리
```kotlin
@Composable
fun CaptureScreen(
    sharedText: String? = null,
    sharedImageUri: Uri? = null,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // M07: 공유 인텐트 처리
    LaunchedEffect(sharedText, sharedImageUri) {
        when {
            sharedText != null -> viewModel.onSharedTextReceived(sharedText)
            sharedImageUri != null -> viewModel.onSharedImageReceived(sharedImageUri)
        }
    }

    // 카메라 권한 launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 카메라 실행
        } else {
            // 권한 거부 처리
        }
    }

    // 이미지 선택 launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // 음성 녹음 권한 launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 음성 녹음 시작
        } else {
            // 권한 거부 처리
        }
    }

    // UI 렌더링...
}
```

### 3.7 MainActivity 공유 인텐트 처리

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedData = handleShareIntent(intent)

        enableEdgeToEdge()
        setContent {
            KAIROS_mobileTheme {
                CaptureScreen(
                    sharedText = sharedData.text,
                    sharedImageUri = sharedData.imageUri
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleShareIntent(intent: Intent): SharedData {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                when {
                    intent.type == "text/plain" -> {
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        SharedData(text = text)
                    }
                    intent.type?.startsWith("image/") == true -> {
                        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        SharedData(imageUri = imageUri)
                    }
                    else -> SharedData()
                }
            }
            else -> SharedData()
        }
    }

    data class SharedData(
        val text: String? = null,
        val imageUri: Uri? = null
    )
}
```

---

## 4. 의존성

### 4.1 libs.versions.toml

```toml
[versions]
# 기존 버전들...
coil-compose = "2.5.0"

[libraries]
# 이미지 로딩 (업로드된 이미지 미리보기용)
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil-compose" }
```

**제거되는 의존성**:
- ~~ML Kit~~ (서버에서 처리)
- ~~CameraX~~ (Android 기본 카메라 사용)
- ~~Jsoup~~ (서버에서 처리)

---

## 5. 구현 순서 (5일)

### Day 1: 기반 구조
1. Docs 디렉터리 생성 및 문서화
2. CaptureSource enum 생성
3. Capture 모델 확장
4. Database 마이그레이션 (v1 → v2)
5. 마이그레이션 테스트

### Day 2: 서버 API 연동
6. MultimodalCaptureResponse DTO 생성
7. KairosApi 인터페이스 확장
8. Repository 메서드 구현 (submitImageCapture, submitVoiceCapture, submitWebClip)
9. Use Cases 생성

### Day 3: UI - 이미지 & 음성
10. CaptureMode enum 생성
11. CaptureUiState 확장
12. CaptureViewModel 확장
13. 카메라/갤러리 launcher 추가
14. 음성 녹음 launcher 추가
15. GlassCaptureCard 모드 버튼 추가

### Day 4: 공유 인텐트 & 웹 클립
16. AndroidManifest.xml 권한 및 인텐트 필터 추가
17. MainActivity 공유 인텐트 처리
18. 웹 클립 URL 입력 UI
19. CaptureScreen 공유 데이터 처리

### Day 5: 테스팅 & 통합
20. 이미지 업로드 테스트
21. 음성 업로드 테스트
22. 웹 클립 테스트
23. 공유 인텐트 테스트
24. 오프라인 큐잉 테스트
25. 에러 처리 검증
26. UI/UX 개선

---

## 6. 핵심 파일 경로

### 새로 생성할 파일
```
domain/model/CaptureSource.kt
domain/model/CaptureMode.kt
domain/usecase/SubmitImageCaptureUseCase.kt
domain/usecase/SubmitVoiceCaptureUseCase.kt
domain/usecase/SubmitWebClipUseCase.kt
data/remote/dto/MultimodalCaptureResponse.kt
```

### 수정할 파일
```
domain/model/Capture.kt
data/local/database/entities/CaptureQueueEntity.kt
data/local/database/KairosDatabase.kt
data/remote/api/KairosApi.kt
data/repository/CaptureRepositoryImpl.kt
domain/repository/CaptureRepository.kt
presentation/capture/CaptureUiState.kt
presentation/capture/CaptureViewModel.kt
presentation/capture/CaptureScreen.kt
presentation/components/GlassCaptureCard.kt
MainActivity.kt
AndroidManifest.xml
gradle/libs.versions.toml
```

---

## 7. 테스트 시나리오

### 7.1 M05: 이미지 캡처
1. 앱 실행 → IMAGE 버튼 클릭
2. 갤러리에서 텍스트가 포함된 이미지 선택
3. 로딩 인디케이터 표시
4. 서버 응답: OCR 추출 텍스트 + AI 분류
5. 성공 피드백 표시
6. Room DB에 저장 확인

### 7.2 M06: 음성 캡처
1. 앱 실행 → VOICE 버튼 클릭
2. 마이크 권한 승인
3. 음성 녹음 (Android 기본 녹음기)
4. 녹음 완료 → 서버 업로드
5. 서버 응답: STT 텍스트 + AI 분류
6. 성공 피드백 표시

### 7.3 M07: 공유 인텐트
1. Chrome에서 URL 공유 → KAIROS 선택
2. 앱 실행되며 자동으로 URL 처리
3. 서버에서 웹 크롤링 + AI 분류
4. 성공 피드백 표시

### 7.4 M08: 웹 클립
1. 앱 실행 → WEB_CLIP 버튼 클릭
2. URL 입력 (예: https://example.com/article)
3. 서버에서 메타데이터 추출 + AI 요약
4. 성공 피드백 표시

### 7.5 오프라인 모드
1. 비행기 모드 활성화
2. 이미지 캡처 → 로컬 저장만 (PENDING)
3. Wi-Fi 재연결
4. 자동 동기화 → 서버 처리
5. SYNCED 상태로 업데이트

---

## 8. 성공 기준

**Phase 2 완료 체크리스트**:
- [ ] Database v2 마이그레이션 성공
- [ ] 이미지 업로드 및 서버 OCR 정상 작동
- [ ] 음성 업로드 및 서버 STT 정상 작동
- [ ] 공유 인텐트로 앱 실행 및 처리 정상
- [ ] 웹 클립 서버 크롤링 정상 작동
- [ ] 오프라인 큐잉 및 동기화 정상
- [ ] 모든 권한 처리 정상 (카메라, 마이크)
- [ ] Glassmorphism UI 유지
- [ ] 에러 처리 및 피드백 정상
- [ ] 빌드 성공 및 APK 생성

---

## 9. Phase 3 Preview

Phase 3에서 추가될 기능 (모두 서버 처리):
- **M09**: AI 요약 (서버에서 긴 텍스트 자동 요약)
- **M10**: 스마트 태그 제안 (서버에서 과거 데이터 기반 태그 추천)
- **M11**: Google Calendar 연동 (서버에서 SCHEDULE 타입 동기화)
- **M12**: Todoist 연동 (서버에서 TODO 타입 동기화)

클라이언트는 주로:
- 요약 결과 표시
- 태그 제안 UI
- 외부 서비스 연동 상태 표시
- 설정 화면 (OAuth 토큰 관리)
