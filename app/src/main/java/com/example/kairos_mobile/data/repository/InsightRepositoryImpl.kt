package com.example.kairos_mobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.InsightQueueDao
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.mapper.ContentTypeMapper
import com.example.kairos_mobile.data.mapper.InsightMapper
import com.example.kairos_mobile.data.processor.OcrProcessor
import com.example.kairos_mobile.data.processor.WebClipper
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateRequest
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Destination
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightSource
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.repository.ConfigRepository
import com.example.kairos_mobile.domain.repository.InsightRepository
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
 * 인사이트 Repository 구현체 (API v2.1)
 * 핵심 비즈니스 로직 포함
 */
@Singleton
class InsightRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val dao: InsightQueueDao,
    private val configRepository: ConfigRepository,
    private val todoRepository: TodoRepository,
    private val insightMapper: InsightMapper,
    private val classificationMapper: ClassificationMapper,
    private val ocrProcessor: OcrProcessor,
    private val webClipper: WebClipper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : InsightRepository {

    companion object {
        private const val TAG = "InsightRepository"
    }

    /**
     * 인사이트 제출
     * - 먼저 로컬 DB에 저장 (절대 유실 방지)
     * - 네트워크 있으면 분류 + 라우팅 처리
     * - 네트워크 없으면 로컬 저장만 (나중에 WorkManager가 동기화)
     */
    override suspend fun submitInsight(content: String): Result<Insight> = withContext(dispatcher) {
        try {
            // 1. 인사이트 객체 생성
            val insight = Insight(
                content = content,
                syncStatus = SyncStatus.PENDING
            )

            // 2. 먼저 로컬에 저장 (절대 유실 방지)
            dao.insertInsight(insightMapper.toEntity(insight))
            Log.d(TAG, "Insight saved locally: ${insight.id}")

            // 3. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Insight queued for later sync")
                return@withContext Result.Success(insight)
            }

            // 4. 온라인: AI 분류 시도
            val classification = when (val result = classifyInsight(content)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 5. destination에 따른 라우팅
            val routingResult = routeByDestination(insight.id, classification, content)
            if (routingResult is Result.Error) {
                Log.e(TAG, "Routing failed", routingResult.exception)
                // 라우팅 실패: 분류 결과는 저장하고, 상태는 PENDING 유지
                val insightWithClassification = insight.copy(classification = classification)
                dao.updateInsight(insightMapper.toEntity(insightWithClassification))
                return@withContext Result.Success(insightWithClassification)
            }

            // 6. 성공: 동기화 완료 상태로 업데이트
            val syncedInsight = insight.copy(
                classification = classification,
                syncStatus = SyncStatus.SYNCED
            )
            dao.updateInsight(insightMapper.toEntity(syncedInsight))
            Log.d(TAG, "Insight synced successfully: ${insight.id}")
            Result.Success(syncedInsight)
        } catch (e: Exception) {
            Log.e(TAG, "submitInsight failed", e)
            Result.Error(e)
        }
    }

    /**
     * AI 분류 수행 (API v2.1)
     */
    override suspend fun classifyInsight(content: String): Result<Classification> = withContext(dispatcher) {
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
            Log.e(TAG, "classifyInsight failed", e)
            Result.Error(e)
        }
    }

    /**
     * destination에 따른 라우팅 처리
     */
    private suspend fun routeByDestination(
        insightId: String,
        classification: Classification,
        content: String
    ): Result<Boolean> {
        return when (classification.destination) {
            Destination.TODO -> {
                // Todo 생성
                Log.d(TAG, "Routing to TODO: $insightId")
                when (val result = todoRepository.createTodoFromInsight(insightId, classification)) {
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
                Log.d(TAG, "Routing to OBSIDIAN: $insightId")
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
                type = InsightType.toApiValue(classification.type),
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
     * 오프라인 큐에서 대기중인 인사이트 조회
     */
    override fun getPendingInsights(): Flow<List<Insight>> {
        return dao.getInsightsByStatus(SyncStatus.PENDING.name)
            .map { entities -> entities.map { insightMapper.toDomain(it) } }
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

            val entities = dao.getInsightsByStatus(SyncStatus.PENDING.name).first()
            var syncedCount = 0

            Log.d(TAG, "Syncing ${entities.size} pending insights")

            for (entity in entities) {
                try {
                    val insight = insightMapper.toDomain(entity)

                    // 분류가 없으면 분류 수행
                    val classification = insight.classification ?: when (val result = classifyInsight(insight.content)) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Log.e(TAG, "Sync classification failed for ${insight.id}", result.exception)
                            continue
                        }
                        is Result.Loading -> continue
                    }

                    // destination에 따른 라우팅
                    when (routeByDestination(insight.id, classification, insight.content)) {
                        is Result.Success -> {
                            dao.updateSyncStatus(insight.id, SyncStatus.SYNCED.name)
                            syncedCount++
                            Log.d(TAG, "Synced insight: ${insight.id}")
                        }
                        is Result.Error -> {
                            dao.incrementRetryCount(insight.id, System.currentTimeMillis())
                            Log.e(TAG, "Sync failed for ${insight.id}")
                        }
                        is Result.Loading -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing insight ${entity.id}", e)
                }
            }

            Log.d(TAG, "Sync completed: $syncedCount insights synced")
            Result.Success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "syncOfflineQueue failed", e)
            Result.Error(e)
        }
    }

    /**
     * 특정 인사이트의 동기화 상태 업데이트
     */
    override suspend fun updateInsightStatus(
        insightId: String,
        status: SyncStatus,
        error: String?
    ) {
        withContext(dispatcher) {
            if (error != null) {
                dao.updateSyncStatusWithError(insightId, status.name, error)
            } else {
                dao.updateSyncStatus(insightId, status.name)
            }
            Log.d(TAG, "Updated insight $insightId status to $status")
        }
    }

    /**
     * M05: 이미지 인사이트 + OCR
     */
    override suspend fun submitImageInsight(imageUri: Uri): Result<Insight> = withContext(dispatcher) {
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

            // 2. Insight 객체 생성 (source = IMAGE)
            val insight = Insight(
                content = extractedText,
                source = InsightSource.IMAGE,
                imageUri = imageUri.toString(),
                syncStatus = SyncStatus.PENDING
            )

            // 3. 로컬에 저장
            dao.insertInsight(insightMapper.toEntity(insight))
            Log.d(TAG, "Image insight saved locally: ${insight.id}")

            // 4. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Image insight queued for later sync")
                return@withContext Result.Success(insight)
            }

            // 5. AI 분류 및 라우팅
            val classification = when (val result = classifyInsightWithSource(extractedText, InsightSource.IMAGE)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(insight.id, classification, extractedText)) {
                is Result.Success -> {
                    val syncedInsight = insight.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateInsight(insightMapper.toEntity(syncedInsight))
                    Log.d(TAG, "Image insight synced successfully: ${insight.id}")
                    Result.Success(syncedInsight)
                }
                is Result.Error -> {
                    val insightWithClassification = insight.copy(classification = classification)
                    dao.updateInsight(insightMapper.toEntity(insightWithClassification))
                    Result.Success(insightWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitImageInsight failed", e)
            Result.Error(e)
        }
    }

    /**
     * M06: 음성 입력
     */
    override suspend fun submitVoiceInsight(audioText: String, audioUri: Uri?): Result<Insight> = withContext(dispatcher) {
        try {
            // 1. Insight 객체 생성 (source = VOICE)
            val insight = Insight(
                content = audioText,
                source = InsightSource.VOICE,
                audioUri = audioUri?.toString(),
                syncStatus = SyncStatus.PENDING
            )

            // 2. 로컬에 저장
            dao.insertInsight(insightMapper.toEntity(insight))
            Log.d(TAG, "Voice insight saved locally: ${insight.id}")

            // 3. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Voice insight queued for later sync")
                return@withContext Result.Success(insight)
            }

            // 4. AI 분류 및 라우팅
            val classification = when (val result = classifyInsightWithSource(audioText, InsightSource.VOICE)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(insight.id, classification, audioText)) {
                is Result.Success -> {
                    val syncedInsight = insight.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateInsight(insightMapper.toEntity(syncedInsight))
                    Log.d(TAG, "Voice insight synced successfully: ${insight.id}")
                    Result.Success(syncedInsight)
                }
                is Result.Error -> {
                    val insightWithClassification = insight.copy(classification = classification)
                    dao.updateInsight(insightMapper.toEntity(insightWithClassification))
                    Result.Success(insightWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitVoiceInsight failed", e)
            Result.Error(e)
        }
    }

    /**
     * M08: 웹 클립
     */
    override suspend fun submitWebClip(url: String): Result<Insight> = withContext(dispatcher) {
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

            // 3. Insight 객체 생성 (source = WEB_CLIP)
            val insight = Insight(
                content = content,
                source = InsightSource.WEB_CLIP,
                webMetadata = webMetadata,
                syncStatus = SyncStatus.PENDING
            )

            // 4. 로컬에 저장
            dao.insertInsight(insightMapper.toEntity(insight))
            Log.d(TAG, "Web clip saved locally: ${insight.id}")

            // 5. 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Offline mode: Web clip queued for later sync")
                return@withContext Result.Success(insight)
            }

            // 6. AI 분류 및 라우팅
            val classification = when (val result = classifyInsightWithSource(content, InsightSource.WEB_CLIP)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (routeByDestination(insight.id, classification, content)) {
                is Result.Success -> {
                    val syncedInsight = insight.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateInsight(insightMapper.toEntity(syncedInsight))
                    Log.d(TAG, "Web clip synced successfully: ${insight.id}")
                    Result.Success(syncedInsight)
                }
                is Result.Error -> {
                    val insightWithClassification = insight.copy(classification = classification)
                    dao.updateInsight(insightMapper.toEntity(insightWithClassification))
                    Result.Success(insightWithClassification)
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
    private suspend fun classifyInsightWithSource(
        content: String,
        source: InsightSource
    ): Result<Classification> = withContext(dispatcher) {
        try {
            val request = ClassifyRequest(
                content = content,
                contentType = ContentTypeMapper.toApiContentType(source)
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
            Log.e(TAG, "classifyInsightWithSource failed", e)
            Result.Error(e)
        }
    }

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * NOTE: 현재 API v2.1에는 별도 summarize 엔드포인트가 없음
     * 향후 추가 시 구현 예정
     */
    override suspend fun generateSummary(
        insightId: String,
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
     * M10: 스마트 태그 제안
     * NOTE: 현재 API v2.1에는 별도 tags/suggest 엔드포인트가 없음
     * 향후 추가 시 구현 예정
     */
    override suspend fun suggestTags(
        content: String,
        classification: String?
    ): Result<List<SuggestedTag>> = withContext(dispatcher) {
        // 임시: 빈 리스트 반환
        Result.Success(emptyList())
    }

    // ========== Phase 3: 검색 및 히스토리 기능 ==========

    /**
     * 검색 쿼리로 인사이트 항목 조회
     */
    override suspend fun searchInsights(
        query: SearchQuery,
        offset: Int,
        limit: Int
    ): Result<List<Insight>> = withContext(dispatcher) {
        try {
            val types = if (query.types.isEmpty()) null else ""
            val typesList = if (query.types.isEmpty()) null else query.types.map { it.name }
            val sources = if (query.sources.isEmpty()) null else ""
            val sourcesList = if (query.sources.isEmpty()) null else query.sources.map { it.name }
            val startDate = query.dateRange?.start
            val endDate = query.dateRange?.end

            var insights: List<Insight>? = null
            dao.searchInsights(
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
                insights = entities.map { insightMapper.toDomain(it) }
            }

            Result.Success(insights ?: emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "searchInsights failed", e)
            Result.Error(e)
        }
    }

    /**
     * 모든 인사이트 항목 조회 (페이징 지원)
     */
    override fun getAllInsights(offset: Int, limit: Int): Flow<List<Insight>> {
        return dao.getAllInsightsPaged(limit, offset)
            .map { entities -> entities.map { insightMapper.toDomain(it) } }
    }

    /**
     * 날짜별로 그룹화된 인사이트 조회
     */
    override fun getInsightsGroupedByDate(): Flow<Map<String, List<Insight>>> {
        return dao.getRecentInsights(limit = 100)
            .map { entities ->
                val insights = entities.map { insightMapper.toDomain(it) }
                groupInsightsByDate(insights)
            }
    }

    /**
     * 특정 ID의 인사이트 조회
     */
    override suspend fun getInsightById(id: String): Result<Insight?> = withContext(dispatcher) {
        try {
            val entity = dao.getInsightById(id)
            val insight = entity?.let { insightMapper.toDomain(it) }
            Result.Success(insight)
        } catch (e: Exception) {
            Log.e(TAG, "getInsightById failed", e)
            Result.Error(e)
        }
    }

    /**
     * 전체 인사이트 개수 조회
     */
    override fun getTotalCount(): Flow<Int> {
        return dao.getTotalCount()
    }

    // ========== PRD v4.0: 추가 기능 ==========

    /**
     * 최근 인사이트 조회 (Home 화면용)
     */
    override fun getRecentInsights(limit: Int): Flow<List<Insight>> {
        return dao.getRecentInsights(limit)
            .map { entities -> entities.map { insightMapper.toDomain(it) } }
    }

    /**
     * 인사이트 저장 (타입 자동 분류)
     */
    override suspend fun saveInsight(content: String): Result<Insight> {
        return submitInsight(content)
    }

    /**
     * 인사이트 저장 (타입 지정)
     */
    override suspend fun saveInsightWithType(content: String, type: InsightType): Result<Insight> = withContext(dispatcher) {
        try {
            // 1. 인사이트 객체 생성
            val insight = Insight(
                content = content,
                syncStatus = SyncStatus.PENDING
            )

            // 2. 로컬에 저장
            dao.insertInsight(insightMapper.toEntity(insight))
            Log.d(TAG, "Insight saved locally with type $type: ${insight.id}")

            // 3. 네트워크 확인 없이 로컬 저장만 (타입이 이미 지정됨)
            // 나중에 백그라운드에서 동기화
            Result.Success(insight)
        } catch (e: Exception) {
            Log.e(TAG, "saveInsightWithType failed", e)
            Result.Error(e)
        }
    }

    /**
     * 인사이트들을 날짜별로 그룹화
     */
    private fun groupInsightsByDate(insights: List<Insight>): Map<String, List<Insight>> {
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

        return insights.groupBy { insight ->
            when {
                insight.timestamp >= todayStart -> "Today"
                insight.timestamp >= yesterdayStart -> "Yesterday"
                insight.timestamp >= thisWeekStart -> "This Week"
                else -> dateFormat.format(insight.timestamp)
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
