# KAIROS Magic Inbox - Phase 3 구현 계획 (서버 중심 아키텍처)

## 문서 정보
- **버전**: 3.0
- **작성일**: 2026-01-23
- **아키텍처**: 서버 중심 (Thin Client)
- **예상 개발 기간**: 7일

---

## 1. 개요

### 1.1 Phase 3 목표

**고급 AI 기능 및 외부 서비스 연동** (모두 서버 처리)
- **M09**: AI 요약 - 긴 텍스트 자동 요약
- **M10**: 스마트 태그 제안 - 과거 데이터 기반 태그 추천
- **M11**: Google Calendar 연동 - SCHEDULE 타입 자동 동기화
- **M12**: Todoist 연동 - TODO 타입 자동 동기화

### 1.2 아키텍처 원칙

**클라이언트 역할**:
- 요약 결과 표시 (접기/펼치기 UI)
- 태그 제안 칩 표시 및 선택
- 외부 서비스 연동 상태 표시
- 설정 화면 (OAuth 인증 UI)
- 서버 API 호출 및 응답 처리

**서버 역할**:
- AI 요약 생성 (>500자 텍스트)
- 과거 캡처 데이터 분석 → 태그 패턴 학습
- 태그 제안 생성
- Google Calendar API 연동 (이벤트 생성/동기화)
- Todoist API 연동 (태스크 생성/동기화)
- OAuth 토큰 관리 (안전한 서버 저장)

---

## 2. 서버 API 명세

### 2.1 M09: AI 요약 API

#### POST `/api/v1/captures/{captureId}/summarize`

**요청**:
```json
{
  "captureId": "uuid-string"
}
```

**응답**:
```json
{
  "captureId": "uuid-string",
  "summary": "이 회의에서는 Q1 목표 달성 현황을 논의했으며, 마케팅 예산을 20% 증액하기로 결정했다.",
  "originalLength": 1250,
  "summaryLength": 85,
  "compressionRatio": 0.068,
  "generatedAt": "2026-01-23T10:30:00Z"
}
```

**요약 트리거 조건** (서버 측):
- 텍스트 길이 > 500자
- 이미지 OCR 결과 > 300자
- 웹 클립 본문 > 800자

---

### 2.2 M10: 스마트 태그 제안 API

#### POST `/api/v1/captures/{captureId}/suggest-tags`

**요청**:
```json
{
  "captureId": "uuid-string",
  "currentContent": "팀 회의 일정 잡기",
  "currentType": "TODO"
}
```

**응답**:
```json
{
  "captureId": "uuid-string",
  "suggestedTags": [
    {
      "tag": "work",
      "confidence": 0.92,
      "reason": "과거 TODO 타입 캡처 80%가 'work' 태그 사용"
    },
    {
      "tag": "meeting",
      "confidence": 0.85,
      "reason": "'회의' 키워드 포함 시 89% 확률로 사용됨"
    },
    {
      "tag": "urgent",
      "confidence": 0.68,
      "reason": "유사한 캡처에서 빈번히 사용됨"
    }
  ],
  "generatedAt": "2026-01-23T10:30:00Z"
}
```

**태그 학습** (서버 백그라운드 작업):
- 사용자의 과거 캡처 분석 (최근 3개월)
- 태그-콘텐츠 패턴 학습
- 캡처 타입별 태그 빈도 분석
- 키워드-태그 상관관계 분석

---

### 2.3 M11: Google Calendar 연동 API

#### POST `/api/v1/external/google-calendar/auth`

**OAuth 인증 URL 요청**:
```json
{
  "userId": "user-uuid"
}
```

**응답**:
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?client_id=...",
  "state": "random-state-token"
}
```

#### POST `/api/v1/external/google-calendar/callback`

**OAuth 콜백 처리** (서버가 받음):
```json
{
  "code": "authorization-code",
  "state": "random-state-token"
}
```

**응답**:
```json
{
  "success": true,
  "userId": "user-uuid",
  "connected": true
}
```

#### POST `/api/v1/captures/{captureId}/sync-to-calendar`

**SCHEDULE 타입 캡처를 Google Calendar에 동기화**:

**요청**:
```json
{
  "captureId": "uuid-string"
}
```

**응답**:
```json
{
  "captureId": "uuid-string",
  "calendarEventId": "google-event-id",
  "calendarEventUrl": "https://calendar.google.com/event?eid=...",
  "title": "팀 회의",
  "startTime": "2026-01-25T14:00:00Z",
  "endTime": "2026-01-25T15:00:00Z",
  "syncedAt": "2026-01-23T10:30:00Z"
}
```

---

### 2.4 M12: Todoist 연동 API

#### POST `/api/v1/external/todoist/auth`

**Todoist API 토큰 등록**:
```json
{
  "userId": "user-uuid",
  "apiToken": "todoist-api-token"
}
```

**응답**:
```json
{
  "success": true,
  "userId": "user-uuid",
  "connected": true
}
```

#### POST `/api/v1/captures/{captureId}/sync-to-todoist`

**TODO 타입 캡처를 Todoist에 동기화**:

**요청**:
```json
{
  "captureId": "uuid-string"
}
```

**응답**:
```json
{
  "captureId": "uuid-string",
  "todoistTaskId": "todoist-task-id",
  "todoistTaskUrl": "https://todoist.com/app/task/...",
  "title": "프로젝트 보고서 작성",
  "dueDate": "2026-01-30",
  "priority": 3,
  "syncedAt": "2026-01-23T10:30:00Z"
}
```

---

### 2.5 자동 동기화 API

#### GET `/api/v1/external/sync-status`

**외부 서비스 연동 상태 조회**:

**응답**:
```json
{
  "googleCalendar": {
    "connected": true,
    "lastSyncedAt": "2026-01-23T09:00:00Z",
    "pendingEvents": 2
  },
  "todoist": {
    "connected": true,
    "lastSyncedAt": "2026-01-23T09:00:00Z",
    "pendingTasks": 5
  }
}
```

---

## 3. 클라이언트 구현 범위

### 3.1 Domain Layer 확장

#### SummarizedContent.kt (새 파일)
```kotlin
package com.example.kairos_mobile.domain.model

/**
 * 서버에서 생성된 AI 요약 정보
 */
data class SummarizedContent(
    val captureId: String,
    val summary: String,
    val originalLength: Int,
    val summaryLength: Int,
    val compressionRatio: Float,
    val generatedAt: String
)
```

#### TagSuggestion.kt (새 파일)
```kotlin
package com.example.kairos_mobile.domain.model

/**
 * 서버가 제안한 태그
 */
data class TagSuggestion(
    val tag: String,
    val confidence: Float,
    val reason: String
)
```

#### ExternalServiceStatus.kt (새 파일)
```kotlin
package com.example.kairos_mobile.domain.model

/**
 * 외부 서비스 연동 상태
 */
data class ExternalServiceStatus(
    val googleCalendar: ServiceConnection,
    val todoist: ServiceConnection
)

data class ServiceConnection(
    val connected: Boolean,
    val lastSyncedAt: String?,
    val pendingCount: Int
)
```

#### Capture.kt 확장
```kotlin
data class Capture(
    // 기존 필드들...

    // M09: AI 요약
    val summary: String? = null,
    val hasSummary: Boolean = false,

    // M10: 태그 제안
    val suggestedTags: List<TagSuggestion> = emptyList(),

    // M11 & M12: 외부 서비스 동기화
    val externalSyncStatus: Map<String, ExternalSyncInfo> = emptyMap()
)

data class ExternalSyncInfo(
    val service: String,        // "google_calendar" or "todoist"
    val externalId: String,     // 외부 서비스의 이벤트/태스크 ID
    val externalUrl: String,    // 외부 서비스 URL
    val syncedAt: String
)
```

### 3.2 Data Layer

#### KairosApi.kt 확장
```kotlin
interface KairosApi {
    // 기존 엔드포인트들...

    // M09: AI 요약
    @POST("/api/v1/captures/{captureId}/summarize")
    suspend fun summarizeCapture(
        @Path("captureId") captureId: String
    ): Response<SummarizeResponse>

    // M10: 스마트 태그 제안
    @POST("/api/v1/captures/{captureId}/suggest-tags")
    suspend fun suggestTags(
        @Path("captureId") captureId: String,
        @Body request: SuggestTagsRequest
    ): Response<SuggestTagsResponse>

    // M11: Google Calendar 인증
    @POST("/api/v1/external/google-calendar/auth")
    suspend fun getGoogleCalendarAuthUrl(
        @Body request: AuthRequest
    ): Response<AuthUrlResponse>

    @POST("/api/v1/external/google-calendar/callback")
    suspend fun handleGoogleCalendarCallback(
        @Body request: OAuthCallbackRequest
    ): Response<OAuthCallbackResponse>

    @POST("/api/v1/captures/{captureId}/sync-to-calendar")
    suspend fun syncToGoogleCalendar(
        @Path("captureId") captureId: String
    ): Response<CalendarSyncResponse>

    // M12: Todoist 인증
    @POST("/api/v1/external/todoist/auth")
    suspend fun registerTodoistToken(
        @Body request: TodoistAuthRequest
    ): Response<TodoistAuthResponse>

    @POST("/api/v1/captures/{captureId}/sync-to-todoist")
    suspend fun syncToTodoist(
        @Path("captureId") captureId: String
    ): Response<TodoistSyncResponse>

    // 외부 서비스 상태 조회
    @GET("/api/v1/external/sync-status")
    suspend fun getExternalSyncStatus(): Response<ExternalSyncStatusResponse>
}
```

#### DTO 파일들 (새 파일)
```kotlin
// SummarizeResponse.kt
data class SummarizeResponse(
    val captureId: String,
    val summary: String,
    val originalLength: Int,
    val summaryLength: Int,
    val compressionRatio: Float,
    val generatedAt: String
)

// SuggestTagsRequest.kt
data class SuggestTagsRequest(
    val currentContent: String,
    val currentType: String
)

// SuggestTagsResponse.kt
data class SuggestTagsResponse(
    val captureId: String,
    val suggestedTags: List<TagSuggestionDto>,
    val generatedAt: String
)

data class TagSuggestionDto(
    val tag: String,
    val confidence: Float,
    val reason: String
)

// AuthRequest.kt
data class AuthRequest(
    val userId: String
)

// AuthUrlResponse.kt
data class AuthUrlResponse(
    val authUrl: String,
    val state: String
)

// OAuthCallbackRequest.kt
data class OAuthCallbackRequest(
    val code: String,
    val state: String
)

// OAuthCallbackResponse.kt
data class OAuthCallbackResponse(
    val success: Boolean,
    val userId: String,
    val connected: Boolean
)

// CalendarSyncResponse.kt
data class CalendarSyncResponse(
    val captureId: String,
    val calendarEventId: String,
    val calendarEventUrl: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val syncedAt: String
)

// TodoistAuthRequest.kt
data class TodoistAuthRequest(
    val userId: String,
    val apiToken: String
)

// TodoistAuthResponse.kt
data class TodoistAuthResponse(
    val success: Boolean,
    val userId: String,
    val connected: Boolean
)

// TodoistSyncResponse.kt
data class TodoistSyncResponse(
    val captureId: String,
    val todoistTaskId: String,
    val todoistTaskUrl: String,
    val title: String,
    val dueDate: String?,
    val priority: Int,
    val syncedAt: String
)

// ExternalSyncStatusResponse.kt
data class ExternalSyncStatusResponse(
    val googleCalendar: ServiceConnectionDto,
    val todoist: ServiceConnectionDto
)

data class ServiceConnectionDto(
    val connected: Boolean,
    val lastSyncedAt: String?,
    val pendingEvents: Int? = null,
    val pendingTasks: Int? = null
)
```

#### CaptureRepositoryImpl 확장
```kotlin
override suspend fun requestSummary(captureId: String): Result<SummarizedContent> {
    return withContext(Dispatchers.IO) {
        try {
            val response = kairosApi.summarizeCapture(captureId)

            if (response.isSuccessful && response.body() != null) {
                val summary = response.body()!!.toDomain()

                // 로컬 DB 업데이트
                captureQueueDao.updateSummary(captureId, summary.summary, true)

                Result.Success(summary)
            } else {
                throw ApiException("Summary generation failed")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun requestTagSuggestions(
    captureId: String,
    content: String,
    type: CaptureType
): Result<List<TagSuggestion>> {
    return withContext(Dispatchers.IO) {
        try {
            val request = SuggestTagsRequest(content, type.name)
            val response = kairosApi.suggestTags(captureId, request)

            if (response.isSuccessful && response.body() != null) {
                val suggestions = response.body()!!.suggestedTags.map { it.toDomain() }

                // 로컬 DB 업데이트
                captureQueueDao.updateSuggestedTags(captureId, suggestions.toJson())

                Result.Success(suggestions)
            } else {
                throw ApiException("Tag suggestion failed")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun syncToGoogleCalendar(captureId: String): Result<CalendarSyncInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val response = kairosApi.syncToGoogleCalendar(captureId)

            if (response.isSuccessful && response.body() != null) {
                val syncInfo = response.body()!!.toDomain()

                // 로컬 DB 업데이트
                captureQueueDao.updateExternalSync(
                    captureId,
                    "google_calendar",
                    syncInfo.calendarEventId,
                    syncInfo.calendarEventUrl,
                    syncInfo.syncedAt
                )

                Result.Success(syncInfo)
            } else {
                throw ApiException("Calendar sync failed")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun syncToTodoist(captureId: String): Result<TodoistSyncInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val response = kairosApi.syncToTodoist(captureId)

            if (response.isSuccessful && response.body() != null) {
                val syncInfo = response.body()!!.toDomain()

                captureQueueDao.updateExternalSync(
                    captureId,
                    "todoist",
                    syncInfo.todoistTaskId,
                    syncInfo.todoistTaskUrl,
                    syncInfo.syncedAt
                )

                Result.Success(syncInfo)
            } else {
                throw ApiException("Todoist sync failed")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

override suspend fun getExternalSyncStatus(): Result<ExternalServiceStatus> {
    return withContext(Dispatchers.IO) {
        try {
            val response = kairosApi.getExternalSyncStatus()

            if (response.isSuccessful && response.body() != null) {
                val status = response.body()!!.toDomain()
                Result.Success(status)
            } else {
                throw ApiException("Failed to get sync status")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 3.3 Database 마이그레이션

#### CaptureQueueEntity.kt (Version 3)
```kotlin
@Entity(tableName = "capture_queue")
data class CaptureQueueEntity(
    // 기존 필드들... (v2)

    // M09: AI 요약
    @ColumnInfo(name = "summary") val summary: String? = null,
    @ColumnInfo(name = "has_summary") val hasSummary: Boolean = false,

    // M10: 태그 제안
    @ColumnInfo(name = "suggested_tags") val suggestedTags: String? = null,  // JSON

    // M11 & M12: 외부 서비스 동기화
    @ColumnInfo(name = "external_sync") val externalSync: String? = null  // JSON
)
```

#### KairosDatabase.kt 마이그레이션
```kotlin
@Database(
    entities = [CaptureQueueEntity::class],
    version = 3,
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    abstract fun captureQueueDao(): CaptureQueueDao

    companion object {
        const val DATABASE_NAME = "kairos_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            // Phase 2 마이그레이션 (기존)
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN summary TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN has_summary INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN suggested_tags TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN external_sync TEXT"
                )
            }
        }
    }
}
```

#### CaptureQueueDao 확장
```kotlin
@Dao
interface CaptureQueueDao {
    // 기존 메서드들...

    @Query("UPDATE capture_queue SET summary = :summary, has_summary = :hasSummary WHERE id = :captureId")
    suspend fun updateSummary(captureId: String, summary: String, hasSummary: Boolean)

    @Query("UPDATE capture_queue SET suggested_tags = :suggestedTags WHERE id = :captureId")
    suspend fun updateSuggestedTags(captureId: String, suggestedTags: String)

    @Query("""
        UPDATE capture_queue
        SET external_sync = :externalSync
        WHERE id = :captureId
    """)
    suspend fun updateExternalSyncJson(captureId: String, externalSync: String)

    // Helper method
    suspend fun updateExternalSync(
        captureId: String,
        service: String,
        externalId: String,
        externalUrl: String,
        syncedAt: String
    ) {
        // JSON 직렬화 로직
        val syncInfo = mapOf(
            "service" to service,
            "externalId" to externalId,
            "externalUrl" to externalUrl,
            "syncedAt" to syncedAt
        )
        val json = Json.encodeToString(syncInfo)
        updateExternalSyncJson(captureId, json)
    }
}
```

### 3.4 Presentation Layer

#### CaptureUiState 확장
```kotlin
data class CaptureUiState(
    // 기존 필드들... (Phase 2)

    // M09: 요약
    val showSummary: Boolean = false,
    val summaryText: String? = null,
    val isLoadingSummary: Boolean = false,

    // M10: 태그 제안
    val suggestedTags: List<TagSuggestion> = emptyList(),
    val selectedTags: Set<String> = emptySet(),

    // M11 & M12: 외부 서비스 상태
    val externalSyncStatus: ExternalServiceStatus? = null,
    val isGoogleCalendarConnected: Boolean = false,
    val isTodoistConnected: Boolean = false
)
```

#### CaptureViewModel 확장
```kotlin
@HiltViewModel
class CaptureViewModel @Inject constructor(
    // 기존 UseCase들...
    private val requestSummaryUseCase: RequestSummaryUseCase,
    private val requestTagSuggestionsUseCase: RequestTagSuggestionsUseCase,
    private val syncToGoogleCalendarUseCase: SyncToGoogleCalendarUseCase,
    private val syncToTodoistUseCase: SyncToTodoistUseCase,
    private val getExternalSyncStatusUseCase: GetExternalSyncStatusUseCase
) : ViewModel() {

    init {
        loadExternalSyncStatus()
    }

    // M09: 요약 요청
    fun onRequestSummary(captureId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSummary = true) }

            when (val result = requestSummaryUseCase(captureId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            summaryText = result.data.summary,
                            showSummary = true,
                            isLoadingSummary = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = "요약 생성 실패: ${result.exception.message}",
                            isLoadingSummary = false
                        )
                    }
                }
            }
        }
    }

    // M10: 태그 제안 요청
    fun onRequestTagSuggestions(captureId: String, content: String, type: CaptureType) {
        viewModelScope.launch {
            when (val result = requestTagSuggestionsUseCase(captureId, content, type)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(suggestedTags = result.data)
                    }
                }
                is Result.Error -> {
                    // 태그 제안 실패는 치명적이지 않으므로 조용히 무시
                }
            }
        }
    }

    fun onTagSelected(tag: String) {
        _uiState.update {
            val newTags = it.selectedTags.toMutableSet().apply {
                if (contains(tag)) remove(tag) else add(tag)
            }
            it.copy(selectedTags = newTags)
        }
    }

    // M11: Google Calendar 동기화
    fun onSyncToGoogleCalendar(captureId: String) {
        viewModelScope.launch {
            when (val result = syncToGoogleCalendarUseCase(captureId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = null,
                            // 성공 피드백 표시
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = "Calendar 동기화 실패: ${result.exception.message}")
                    }
                }
            }
        }
    }

    // M12: Todoist 동기화
    fun onSyncToTodoist(captureId: String) {
        viewModelScope.launch {
            when (val result = syncToTodoistUseCase(captureId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(errorMessage = null)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = "Todoist 동기화 실패: ${result.exception.message}")
                    }
                }
            }
        }
    }

    private fun loadExternalSyncStatus() {
        viewModelScope.launch {
            when (val result = getExternalSyncStatusUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            externalSyncStatus = result.data,
                            isGoogleCalendarConnected = result.data.googleCalendar.connected,
                            isTodoistConnected = result.data.todoist.connected
                        )
                    }
                }
                is Result.Error -> {
                    // 상태 조회 실패는 조용히 처리
                }
            }
        }
    }
}
```

### 3.5 UI Components

#### SummaryCard.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.components

/**
 * AI 요약 표시 카드 (접기/펼치기 가능)
 */
@Composable
fun SummaryCard(
    summaryText: String,
    originalText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .glassCard(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 요약",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summaryText,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = if (expanded) "원문 숨기기" else "원문 보기",
                    fontSize = 12.sp,
                    color = PrimaryNavy
                )
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = originalText,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
```

#### TagSuggestionChips.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.components

/**
 * 스마트 태그 제안 칩 (선택 가능)
 */
@Composable
fun TagSuggestionChips(
    suggestions: List<TagSuggestion>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "추천 태그",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    tag = suggestion.tag,
                    confidence = suggestion.confidence,
                    isSelected = selectedTags.contains(suggestion.tag),
                    onClick = { onTagToggle(suggestion.tag) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    tag: String,
    confidence: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tag,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(confidence * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryNavy.copy(alpha = 0.2f),
            selectedLabelColor = PrimaryNavy
        ),
        modifier = Modifier.glassButton(shape = RoundedCornerShape(50))
    )
}
```

#### ExternalSyncIndicator.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.components

/**
 * 외부 서비스 동기화 상태 표시
 */
@Composable
fun ExternalSyncIndicator(
    captureType: CaptureType,
    externalSyncInfo: ExternalSyncInfo?,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceName = when (captureType) {
        CaptureType.SCHEDULE -> "Google Calendar"
        CaptureType.TODO -> "Todoist"
        else -> return  // 다른 타입은 표시 안 함
    }

    val icon = when (captureType) {
        CaptureType.SCHEDULE -> Icons.Default.CalendarToday
        CaptureType.TODO -> Icons.Default.CheckCircle
        else -> return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = serviceName,
                tint = if (externalSyncInfo != null) AccentGreen else TextMuted,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = if (externalSyncInfo != null) {
                    "$serviceName 동기화 완료"
                } else {
                    "$serviceName 동기화"
                },
                fontSize = 12.sp,
                color = if (externalSyncInfo != null) AccentGreen else TextSecondary
            )
        }

        if (externalSyncInfo == null) {
            TextButton(onClick = onSyncClick) {
                Text(
                    text = "동기화",
                    fontSize = 12.sp,
                    color = PrimaryNavy
                )
            }
        } else {
            IconButton(
                onClick = { /* 외부 URL 열기 */ }
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "열기",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
```

#### SettingsScreen.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.settings

/**
 * 설정 화면 (외부 서비스 연동)
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Google Calendar 연동
            ServiceConnectionCard(
                serviceName = "Google Calendar",
                isConnected = uiState.isGoogleCalendarConnected,
                lastSyncedAt = uiState.googleCalendarLastSync,
                onConnect = viewModel::onConnectGoogleCalendar,
                onDisconnect = viewModel::onDisconnectGoogleCalendar
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Todoist 연동
            ServiceConnectionCard(
                serviceName = "Todoist",
                isConnected = uiState.isTodoistConnected,
                lastSyncedAt = uiState.todoistLastSync,
                onConnect = viewModel::onConnectTodoist,
                onDisconnect = viewModel::onDisconnectTodoist
            )
        }
    }
}

@Composable
private fun ServiceConnectionCard(
    serviceName: String,
    isConnected: Boolean,
    lastSyncedAt: String?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = serviceName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (isConnected && lastSyncedAt != null) {
                        Text(
                            text = "마지막 동기화: $lastSyncedAt",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }

                if (isConnected) {
                    OutlinedButton(onClick = onDisconnect) {
                        Text("연결 해제")
                    }
                } else {
                    Button(onClick = onConnect) {
                        Text("연결")
                    }
                }
            }
        }
    }
}
```

#### SettingsViewModel.kt (새 파일)
```kotlin
package com.example.kairos_mobile.presentation.settings

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val connectGoogleCalendarUseCase: ConnectGoogleCalendarUseCase,
    private val disconnectGoogleCalendarUseCase: DisconnectGoogleCalendarUseCase,
    private val connectTodoistUseCase: ConnectTodoistUseCase,
    private val disconnectTodoistUseCase: DisconnectTodoistUseCase,
    private val getExternalSyncStatusUseCase: GetExternalSyncStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSyncStatus()
    }

    fun onConnectGoogleCalendar() {
        viewModelScope.launch {
            // 서버에서 OAuth URL 받아오기
            // WebView 또는 Custom Tabs로 인증 플로우 시작
        }
    }

    fun onDisconnectGoogleCalendar() {
        viewModelScope.launch {
            disconnectGoogleCalendarUseCase()
            loadSyncStatus()
        }
    }

    fun onConnectTodoist() {
        viewModelScope.launch {
            // Todoist API 토큰 입력 다이얼로그 표시
        }
    }

    fun onDisconnectTodoist() {
        viewModelScope.launch {
            disconnectTodoistUseCase()
            loadSyncStatus()
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            when (val result = getExternalSyncStatusUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleCalendarConnected = result.data.googleCalendar.connected,
                            googleCalendarLastSync = result.data.googleCalendar.lastSyncedAt,
                            isTodoistConnected = result.data.todoist.connected,
                            todoistLastSync = result.data.todoist.lastSyncedAt
                        )
                    }
                }
                is Result.Error -> {
                    // 에러 처리
                }
            }
        }
    }
}

data class SettingsUiState(
    val isGoogleCalendarConnected: Boolean = false,
    val googleCalendarLastSync: String? = null,
    val isTodoistConnected: Boolean = false,
    val todoistLastSync: String? = null
)
```

---

## 4. 구현 순서 (7일)

### Day 1-2: M09 - AI 요약
1. SummarizedContent 도메인 모델 생성
2. SummarizeResponse DTO 생성
3. KairosApi 요약 엔드포인트 추가
4. RequestSummaryUseCase 생성
5. Repository 메서드 구현
6. Database v3 마이그레이션
7. SummaryCard UI 컴포넌트 구현
8. ViewModel 요약 로직 통합
9. 테스트 (긴 텍스트 캡처 → 요약 요청)

### Day 3-4: M10 - 스마트 태그 제안
10. TagSuggestion 도메인 모델 생성
11. SuggestTagsRequest/Response DTO 생성
12. KairosApi 태그 제안 엔드포인트 추가
13. RequestTagSuggestionsUseCase 생성
14. Repository 메서드 구현
15. TagSuggestionChips UI 컴포넌트 구현
16. ViewModel 태그 선택 로직 추가
17. CaptureScreen에 태그 칩 통합
18. 테스트 (캡처 시 자동 태그 제안)

### Day 5: M11 - Google Calendar 연동
19. ExternalServiceStatus 도메인 모델 생성
20. Google Calendar 관련 DTO 생성
21. KairosApi Google Calendar 엔드포인트 추가
22. ConnectGoogleCalendarUseCase 생성
23. SyncToGoogleCalendarUseCase 생성
24. Repository 메서드 구현
25. SettingsScreen UI 구현
26. SettingsViewModel 구현
27. ExternalSyncIndicator 컴포넌트 구현

### Day 6: M12 - Todoist 연동
28. Todoist 관련 DTO 생성
29. KairosApi Todoist 엔드포인트 추가
30. ConnectTodoistUseCase 생성
31. SyncToTodoistUseCase 생성
32. Repository 메서드 구현
33. SettingsScreen Todoist 섹션 추가
34. ExternalSyncIndicator에 Todoist 추가

### Day 7: 통합 테스팅
35. M09 테스트 (긴 텍스트 요약)
36. M10 테스트 (태그 제안 및 선택)
37. M11 테스트 (Google Calendar 연동 및 동기화)
38. M12 테스트 (Todoist 연동 및 동기화)
39. 오프라인 모드에서 동기화 지연 테스트
40. 에러 처리 검증
41. UI/UX 개선

---

## 5. 핵심 파일 경로

### 새로 생성할 파일
```
domain/model/SummarizedContent.kt
domain/model/TagSuggestion.kt
domain/model/ExternalServiceStatus.kt
domain/model/CalendarSyncInfo.kt
domain/model/TodoistSyncInfo.kt

domain/usecase/RequestSummaryUseCase.kt
domain/usecase/RequestTagSuggestionsUseCase.kt
domain/usecase/ConnectGoogleCalendarUseCase.kt
domain/usecase/SyncToGoogleCalendarUseCase.kt
domain/usecase/ConnectTodoistUseCase.kt
domain/usecase/SyncToTodoistUseCase.kt
domain/usecase/GetExternalSyncStatusUseCase.kt

data/remote/dto/SummarizeResponse.kt
data/remote/dto/SuggestTagsRequest.kt
data/remote/dto/SuggestTagsResponse.kt
data/remote/dto/AuthRequest.kt
data/remote/dto/AuthUrlResponse.kt
data/remote/dto/CalendarSyncResponse.kt
data/remote/dto/TodoistAuthRequest.kt
data/remote/dto/TodoistSyncResponse.kt
data/remote/dto/ExternalSyncStatusResponse.kt

presentation/components/SummaryCard.kt
presentation/components/TagSuggestionChips.kt
presentation/components/ExternalSyncIndicator.kt
presentation/settings/SettingsScreen.kt
presentation/settings/SettingsViewModel.kt
presentation/settings/SettingsUiState.kt
```

### 수정할 파일
```
domain/model/Capture.kt
data/local/database/entities/CaptureQueueEntity.kt
data/local/database/KairosDatabase.kt
data/local/database/dao/CaptureQueueDao.kt
data/remote/api/KairosApi.kt
data/repository/CaptureRepositoryImpl.kt
domain/repository/CaptureRepository.kt
presentation/capture/CaptureUiState.kt
presentation/capture/CaptureViewModel.kt
presentation/capture/CaptureScreen.kt
presentation/components/GlassBottomNavigation.kt (Settings 탭 추가)
```

---

## 6. 테스트 시나리오

### 6.1 M09: AI 요약
1. 500자 이상 텍스트 입력
2. Capture 제출 → 서버 자동 요약 생성
3. "요약 보기" 버튼 클릭
4. SummaryCard 표시 (접기/펼치기)
5. 원문 보기/숨기기 토글

### 6.2 M10: 스마트 태그 제안
1. "팀 회의 일정" 텍스트 입력
2. 서버에서 과거 패턴 기반 태그 제안
3. TagSuggestionChips 표시 (work, meeting, urgent)
4. 태그 선택/해제
5. Capture 제출 시 선택된 태그 포함

### 6.3 M11: Google Calendar 연동
1. 설정 → Google Calendar 연결 클릭
2. OAuth 인증 플로우 (Custom Tabs)
3. 연결 완료 → "연결됨" 상태 표시
4. SCHEDULE 타입 캡처 생성
5. "Calendar에 동기화" 버튼 클릭
6. 서버가 Google Calendar 이벤트 생성
7. ExternalSyncIndicator "동기화 완료" 표시
8. "열기" 버튼 → Google Calendar 앱/웹 열림

### 6.4 M12: Todoist 연동
1. 설정 → Todoist 연결 클릭
2. API 토큰 입력 다이얼로그
3. 토큰 제출 → 서버 검증
4. 연결 완료 → "연결됨" 상태 표시
5. TODO 타입 캡처 생성
6. "Todoist에 동기화" 버튼 클릭
7. 서버가 Todoist 태스크 생성
8. ExternalSyncIndicator "동기화 완료" 표시

---

## 7. 성공 기준

**Phase 3 완료 체크리스트**:
- [ ] Database v3 마이그레이션 성공
- [ ] AI 요약 정상 작동 (500자+ 텍스트)
- [ ] 요약 UI (접기/펼치기) 정상
- [ ] 태그 제안 정상 작동
- [ ] 태그 칩 선택/해제 정상
- [ ] Google Calendar OAuth 인증 플로우 정상
- [ ] SCHEDULE → Google Calendar 동기화 정상
- [ ] Todoist API 토큰 등록 정상
- [ ] TODO → Todoist 동기화 정상
- [ ] 설정 화면 정상 작동
- [ ] 외부 서비스 상태 표시 정상
- [ ] 모든 서버 API 에러 처리 정상
- [ ] 빌드 성공 및 APK 생성

---

## 8. 서버 개발 요구사항 (참고)

Phase 3 구현을 위해 서버 팀에 요청할 사항:

1. **M09 API**: POST `/api/v1/captures/{captureId}/summarize`
   - 텍스트 길이 기반 자동 트리거 로직
   - AI 모델 (GPT-4 또는 Claude) 사용
   - 압축률 20% 이하 목표

2. **M10 API**: POST `/api/v1/captures/{captureId}/suggest-tags`
   - 사용자별 과거 캡처 분석 (최근 3개월)
   - 태그-콘텐츠 패턴 학습
   - TF-IDF 또는 Word2Vec 기반 유사도 계산

3. **M11 API**: Google Calendar 통합
   - OAuth 2.0 인증 플로우
   - Google Calendar API v3 사용
   - 이벤트 생성 및 업데이트
   - 양방향 동기화 (옵션)

4. **M12 API**: Todoist 통합
   - Todoist REST API v2 사용
   - API 토큰 기반 인증
   - 태스크 생성 및 업데이트
   - 우선순위 및 due date 파싱

5. **보안 요구사항**:
   - OAuth 토큰 암호화 저장
   - HTTPS 필수
   - Rate limiting 적용
   - API 토큰 만료 처리

---

**문서 버전**: 3.0
**작성일**: 2026-01-23
**예상 개발 기간**: 7일 (Phase 3 only)
