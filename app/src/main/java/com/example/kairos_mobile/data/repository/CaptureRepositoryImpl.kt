package com.example.kairos_mobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.CaptureQueueDao
import com.example.kairos_mobile.data.mapper.CaptureMapper
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.mapper.ContentTypeMapper
import com.example.kairos_mobile.data.processor.OcrProcessor
import com.example.kairos_mobile.data.processor.WebClipper
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateRequest
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Destination
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ConfigRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 Repository 구현체 (API v2.1)
 * 핵심 비즈니스 로직 포함
 */
@Singleton
class CaptureRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val dao: CaptureQueueDao,
    private val configRepository: ConfigRepository,
    private val todoRepository: TodoRepository,
    private val captureMapper: CaptureMapper,
    private val classificationMapper: ClassificationMapper,
    private val ocrProcessor: OcrProcessor,
    private val webClipper: WebClipper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CaptureRepository {

    companion object {
        private const val TAG = "CaptureRepository"
    }

    /**
     * 캡처 제출
     * - 먼저 로컬 DB에 저장 (절대 유실 방지)
     * - 네트워크 있으면 분류 + 라우팅 처리
     * - 네트워크 없으면 로컬 저장만 (나중에 WorkManager가 동기화)
     */
    override suspend fun submitCapture(content: String): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. 캡처 객체 생성
            val capture = Capture(
                content = content,
                syncStatus = SyncStatus.PENDING
            )

            // 2. 먼저 로컬에 저장 (절대 유실 방지)
            dao.insertCapture(captureMapper.toEntity(capture))
            Log.d(TAG, "Capture saved locally: ${capture.id}")

            // 3. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Capture queued for later sync")
                return@withContext Result.Success(capture)
            }

            // 4. 온라인: AI 분류 시도
            val classification = when (val result = classifyCapture(content)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 5. destination에 따른 라우팅
            val routingResult = routeByDestination(capture.id, classification, content)
            if (routingResult is Result.Error) {
                Log.e(TAG, "Routing failed", routingResult.exception)
                // 라우팅 실패: 분류 결과는 저장하고, 상태는 PENDING 유지
                val captureWithClassification = capture.copy(classification = classification)
                dao.updateCapture(captureMapper.toEntity(captureWithClassification))
                return@withContext Result.Success(captureWithClassification)
            }

            // 6. 성공: 동기화 완료 상태로 업데이트
            val syncedCapture = capture.copy(
                classification = classification,
                syncStatus = SyncStatus.SYNCED
            )
            dao.updateCapture(captureMapper.toEntity(syncedCapture))
            Log.d(TAG, "Capture synced successfully: ${capture.id}")
            Result.Success(syncedCapture)
        } catch (e: Exception) {
            Log.e(TAG, "submitCapture failed", e)
            Result.Error(e)
        }
    }

    /**
     * AI 분류 수행 (API v2.1)
     */
    override suspend fun classifyCapture(content: String): Result<Classification> = withContext(dispatcher) {
        try {
            val request = ClassifyRequest(
                content = content,
                contentType = "text"
            )
            val response = api.classify(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val classification = classificationMapper.toDomain(response.body()!!)
                Log.d(TAG, "Classification success: ${classification.type} -> ${classification.destination}")
                Result.Success(classification)
            } else {
                val error = Exception("Classification failed: ${response.code()}")
                Log.e(TAG, "Classification API error", error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "classifyCapture failed", e)
            Result.Error(e)
        }
    }

    /**
     * destination에 따른 라우팅 처리
     */
    private suspend fun routeByDestination(
        captureId: String,
        classification: Classification,
        content: String
    ): Result<Boolean> {
        return when (classification.destination) {
            Destination.TODO -> {
                // Todo 생성
                Log.d(TAG, "Routing to TODO: $captureId")
                when (val result = todoRepository.createTodoFromCapture(captureId, classification)) {
                    is Result.Success -> {
                        Log.d(TAG, "Todo created: ${result.data.id}")
                        Result.Success(true)
                    }
                    is Result.Error -> result
                    is Result.Loading -> Result.Loading
                }
            }
            Destination.OBSIDIAN -> {
                // Obsidian 노트 생성
                Log.d(TAG, "Routing to OBSIDIAN: $captureId")
                createObsidianNote(classification, content)
            }
        }
    }

    /**
     * Obsidian 노트 생성 (API v2.1)
     */
    override suspend fun createObsidianNote(
        classification: Classification,
        content: String
    ): Result<Boolean> = withContext(dispatcher) {
        try {
            val request = NoteCreateRequest(
                title = classification.title,
                content = content,
                type = CaptureType.toApiValue(classification.type),
                tags = classification.tags,
                folder = classification.suggestedPath
            )

            val response = api.createNote(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Obsidian note created: ${response.body()?.path}")
                Result.Success(true)
            } else {
                val errorMsg = response.body()?.error ?: "Note creation failed: ${response.code()}"
                val error = Exception(errorMsg)
                Log.e(TAG, "Obsidian API error", error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "createObsidianNote failed", e)
            Result.Error(e)
        }
    }

    /**
     * 오프라인 큐에서 대기중인 캡처 조회
     */
    override fun getPendingCaptures(): Flow<List<Capture>> {
        return dao.getCapturesByStatus(SyncStatus.PENDING.name)
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    /**
     * 오프라인 큐 동기화
     * WorkManager에서 주기적으로 호출
     */
    override suspend fun syncOfflineQueue(): Result<Int> = withContext(dispatcher) {
        try {
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Sync skipped: No network")
                return@withContext Result.Error(Exception("No network"))
            }

            val entities = dao.getCapturesByStatus(SyncStatus.PENDING.name).first()
            var syncedCount = 0

            Log.d(TAG, "Syncing ${entities.size} pending captures")

            for (entity in entities) {
                try {
                    val capture = captureMapper.toDomain(entity)

                    // 분류가 없으면 분류 수행
                    val classification = capture.classification ?: when (val result = classifyCapture(capture.content)) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Log.e(TAG, "Sync classification failed for ${capture.id}", result.exception)
                            continue
                        }
                        is Result.Loading -> continue
                    }

                    // destination에 따른 라우팅
                    when (routeByDestination(capture.id, classification, capture.content)) {
                        is Result.Success -> {
                            dao.updateSyncStatus(capture.id, SyncStatus.SYNCED.name)
                            syncedCount++
                            Log.d(TAG, "Synced capture: ${capture.id}")
                        }
                        is Result.Error -> {
                            dao.incrementRetryCount(capture.id, System.currentTimeMillis())
                            Log.e(TAG, "Sync failed for ${capture.id}")
                        }
                        is Result.Loading -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing capture ${entity.id}", e)
                }
            }

            Log.d(TAG, "Sync completed: $syncedCount captures synced")
            Result.Success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "syncOfflineQueue failed", e)
            Result.Error(e)
        }
    }

    /**
     * 특정 캡처의 동기화 상태 업데이트
     */
    override suspend fun updateCaptureStatus(
        captureId: String,
        status: SyncStatus,
        error: String?
    ) {
        withContext(dispatcher) {
            if (error != null) {
                dao.updateSyncStatusWithError(captureId, status.name, error)
            } else {
                dao.updateSyncStatus(captureId, status.name)
            }
            Log.d(TAG, "Updated capture $captureId status to $status")
        }
    }

    /**
     * 이미지 캡처 + OCR
     */
    override suspend fun submitImageCapture(imageUri: Uri): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. OCR로 텍스트 추출
            val extractedText = when (val result = ocrProcessor.extractText(imageUri)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "OCR failed", result.exception)
                    return@withContext Result.Error(result.exception)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 2. Capture 객체 생성 (source = IMAGE)
            val capture = Capture(
                content = extractedText,
                source = CaptureSource.IMAGE,
                imageUri = imageUri.toString(),
                syncStatus = SyncStatus.PENDING
            )

            // 3. 로컬에 저장
            dao.insertCapture(captureMapper.toEntity(capture))
            Log.d(TAG, "Image capture saved locally: ${capture.id}")

            // 4. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Image capture queued for later sync")
                return@withContext Result.Success(capture)
            }

            // 5. AI 분류 및 라우팅
            val classification = when (val result = classifyCaptureWithSource(extractedText, CaptureSource.IMAGE)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(capture.id, classification, extractedText)) {
                is Result.Success -> {
                    val syncedCapture = capture.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateCapture(captureMapper.toEntity(syncedCapture))
                    Log.d(TAG, "Image capture synced successfully: ${capture.id}")
                    Result.Success(syncedCapture)
                }
                is Result.Error -> {
                    val captureWithClassification = capture.copy(classification = classification)
                    dao.updateCapture(captureMapper.toEntity(captureWithClassification))
                    Result.Success(captureWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitImageCapture failed", e)
            Result.Error(e)
        }
    }

    /**
     * 음성 입력
     */
    override suspend fun submitVoiceCapture(audioText: String, audioUri: Uri?): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. Capture 객체 생성 (source = VOICE)
            val capture = Capture(
                content = audioText,
                source = CaptureSource.VOICE,
                audioUri = audioUri?.toString(),
                syncStatus = SyncStatus.PENDING
            )

            // 2. 로컬에 저장
            dao.insertCapture(captureMapper.toEntity(capture))
            Log.d(TAG, "Voice capture saved locally: ${capture.id}")

            // 3. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Voice capture queued for later sync")
                return@withContext Result.Success(capture)
            }

            // 4. AI 분류 및 라우팅
            val classification = when (val result = classifyCaptureWithSource(audioText, CaptureSource.VOICE)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(capture.id, classification, audioText)) {
                is Result.Success -> {
                    val syncedCapture = capture.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateCapture(captureMapper.toEntity(syncedCapture))
                    Log.d(TAG, "Voice capture synced successfully: ${capture.id}")
                    Result.Success(syncedCapture)
                }
                is Result.Error -> {
                    val captureWithClassification = capture.copy(classification = classification)
                    dao.updateCapture(captureMapper.toEntity(captureWithClassification))
                    Result.Success(captureWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitVoiceCapture failed", e)
            Result.Error(e)
        }
    }

    /**
     * 웹 클립
     */
    override suspend fun submitWebClip(url: String): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. 웹 메타데이터 추출
            val webMetadata = when (val result = webClipper.extractMetadata(url)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Web metadata extraction failed", result.exception)
                    return@withContext Result.Error(result.exception)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 2. 콘텐츠 조합 (제목 + 설명)
            val content = buildString {
                webMetadata.title?.let { append("$it\n\n") }
                webMetadata.description?.let { append(it) }
            }.ifBlank { url }

            // 3. Capture 객체 생성 (source = WEB_CLIP)
            val capture = Capture(
                content = content,
                source = CaptureSource.WEB_CLIP,
                webMetadata = webMetadata,
                syncStatus = SyncStatus.PENDING
            )

            // 4. 로컬에 저장
            dao.insertCapture(captureMapper.toEntity(capture))
            Log.d(TAG, "Web clip saved locally: ${capture.id}")

            // 5. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Web clip queued for later sync")
                return@withContext Result.Success(capture)
            }

            // 6. AI 분류 및 라우팅
            val classification = when (val result = classifyCaptureWithSource(content, CaptureSource.WEB_CLIP)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(capture.id, classification, content)) {
                is Result.Success -> {
                    val syncedCapture = capture.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateCapture(captureMapper.toEntity(syncedCapture))
                    Log.d(TAG, "Web clip synced successfully: ${capture.id}")
                    Result.Success(syncedCapture)
                }
                is Result.Error -> {
                    val captureWithClassification = capture.copy(classification = classification)
                    dao.updateCapture(captureMapper.toEntity(captureWithClassification))
                    Result.Success(captureWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitWebClip failed", e)
            Result.Error(e)
        }
    }

    /**
     * 소스 타입을 포함한 AI 분류 수행
     */
    private suspend fun classifyCaptureWithSource(
        content: String,
        source: CaptureSource
    ): Result<Classification> = withContext(dispatcher) {
        try {
            val request = ClassifyRequest(
                content = content,
                contentType = ContentTypeMapper.toCaptureApiContentType(source)
            )
            val response = api.classify(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val classification = classificationMapper.toDomain(response.body()!!)
                Log.d(TAG, "Classification success: ${classification.type} -> ${classification.destination}")
                Result.Success(classification)
            } else {
                val error = Exception("Classification failed: ${response.code()}")
                Log.e(TAG, "Classification API error", error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "classifyCaptureWithSource failed", e)
            Result.Error(e)
        }
    }

    // ========== 스마트 처리 기능 ==========

    /**
     * AI 요약 생성
     */
    override suspend fun generateSummary(
        captureId: String,
        content: String
    ): Result<String> = withContext(dispatcher) {
        // 임시: 첫 200자 반환
        val summary = if (content.length > 200) {
            content.take(200) + "..."
        } else {
            content
        }
        Result.Success(summary)
    }

    /**
     * 스마트 태그 제안
     */
    override suspend fun suggestTags(
        content: String,
        classification: String?
    ): Result<List<SuggestedTag>> = withContext(dispatcher) {
        // 임시: 빈 리스트 반환
        Result.Success(emptyList())
    }

    // ========== 검색 및 히스토리 기능 ==========

    /**
     * 검색 쿼리로 캡처 항목 조회
     */
    override suspend fun searchCaptures(
        query: SearchQuery,
        offset: Int,
        limit: Int
    ): Result<List<Capture>> = withContext(dispatcher) {
        try {
            val types = if (query.types.isEmpty()) null else ""
            val typesList = if (query.types.isEmpty()) null else query.types.map { it.name }
            val sources = if (query.sources.isEmpty()) null else ""
            val sourcesList = if (query.sources.isEmpty()) null else query.sources.map { it.name }
            val startDate = query.dateRange?.start
            val endDate = query.dateRange?.end

            var captures: List<Capture>? = null
            dao.searchCaptures(
                searchText = query.text,
                types = types,
                typesList = typesList,
                sources = sources,
                sourcesList = sourcesList,
                startDate = startDate,
                endDate = endDate,
                limit = limit,
                offset = offset
            ).collect { entities ->
                captures = entities.map { captureMapper.toDomain(it) }
            }

            Result.Success(captures ?: emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "searchCaptures failed", e)
            Result.Error(e)
        }
    }

    /**
     * 모든 캡처 항목 조회 (페이징 지원)
     */
    override fun getAllCaptures(offset: Int, limit: Int): Flow<List<Capture>> {
        return dao.getAllCapturesPaged(limit, offset)
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    /**
     * 날짜별로 그룹화된 캡처 조회
     */
    override fun getCapturesGroupedByDate(): Flow<Map<String, List<Capture>>> {
        return dao.getRecentCaptures(limit = 100)
            .map { entities ->
                val captures = entities.map { captureMapper.toDomain(it) }
                groupCapturesByDate(captures)
            }
    }

    /**
     * 특정 ID의 캡처 조회
     */
    override suspend fun getCaptureById(id: String): Result<Capture?> = withContext(dispatcher) {
        try {
            val entity = dao.getCaptureById(id)
            val capture = entity?.let { captureMapper.toDomain(it) }
            Result.Success(capture)
        } catch (e: Exception) {
            Log.e(TAG, "getCaptureById failed", e)
            Result.Error(e)
        }
    }

    /**
     * 전체 캡처 개수 조회
     */
    override fun getTotalCount(): Flow<Int> {
        return dao.getTotalCount()
    }

    // ========== PRD v4.0: 추가 기능 ==========

    /**
     * 최근 캡처 조회 (Home 화면용)
     */
    override fun getRecentCaptures(limit: Int): Flow<List<Capture>> {
        return dao.getRecentCaptures(limit)
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    /**
     * 캡처 저장 (타입 자동 분류)
     */
    override suspend fun saveCapture(content: String): Result<Capture> {
        return submitCapture(content)
    }

    /**
     * 캡처 저장 (타입 지정)
     */
    override suspend fun saveCaptureWithType(content: String, type: CaptureType): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. 캡처 객체 생성
            val capture = Capture(
                content = content,
                syncStatus = SyncStatus.PENDING
            )

            // 2. 로컬에 저장
            dao.insertCapture(captureMapper.toEntity(capture))
            Log.d(TAG, "Capture saved locally with type $type: ${capture.id}")

            // 3. 네트워크 확인 없이 로컬 저장만 (타입이 이미 지정됨)
            // 나중에 백그라운드에서 동기화
            Result.Success(capture)
        } catch (e: Exception) {
            Log.e(TAG, "saveCaptureWithType failed", e)
            Result.Error(e)
        }
    }

    /**
     * 캡처들을 날짜별로 그룹화
     */
    private fun groupCapturesByDate(captures: List<Capture>): Map<String, List<Capture>> {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterdayStart = calendar.apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val thisWeekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return captures.groupBy { capture ->
            when {
                capture.timestamp >= todayStart -> "Today"
                capture.timestamp >= yesterdayStart -> "Yesterday"
                capture.timestamp >= thisWeekStart -> "This Week"
                else -> dateFormat.format(capture.timestamp)
            }
        }.toSortedMap(compareBy { key ->
            when (key) {
                "Today" -> 0
                "Yesterday" -> 1
                "This Week" -> 2
                else -> 3
            }
        })
    }
}
