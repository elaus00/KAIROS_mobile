package com.example.kairos_mobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.InsightQueueDao
import com.example.kairos_mobile.data.mapper.InsightMapper
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.processor.OcrProcessor
import com.example.kairos_mobile.data.processor.WebClipper
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.ai.ClassificationRequest
import com.example.kairos_mobile.data.remote.dto.obsidian.ObsidianCreateRequest
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.data.remote.dto.ai.SummarizeRequest
import com.example.kairos_mobile.data.remote.dto.ai.TagSuggestRequest
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightSource
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.repository.InsightRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.kairos_mobile.domain.repository.ConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인사이트 Repository 구현체
 * 핵심 비즈니스 로직 포함
 */
@Singleton
class InsightRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val dao: InsightQueueDao,
    private val configRepository: ConfigRepository,
    private val insightMapper: InsightMapper,
    private val classificationMapper: ClassificationMapper,
    private val ocrProcessor: OcrProcessor,  // Phase 2: OCR 프로세서
    private val webClipper: WebClipper,      // Phase 2: 웹 클리퍼
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : InsightRepository {

    companion object {
        private const val TAG = "InsightRepository"
    }

    /**
     * 인사이트 제출
     * - 먼저 로컬 DB에 저장 (절대 유실 방지)
     * - 네트워크 있으면 분류 + Obsidian 생성 시도
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
                    // 분류 실패: 로컬에 저장된 상태로 반환
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 5. Obsidian 노트 생성 시도
            when (val result = createObsidianNote(classification, content)) {
                is Result.Success -> {
                    // 성공: 동기화 완료 상태로 업데이트
                    val syncedInsight = insight.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateInsight(insightMapper.toEntity(syncedInsight))
                    Log.d(TAG, "Insight synced successfully: ${insight.id}")
                    Result.Success(syncedInsight)
                }
                is Result.Error -> {
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
                    // 생성 실패: 분류 결과는 저장하고, 상태는 PENDING 유지
                    val insightWithClassification = insight.copy(
                        classification = classification
                    )
                    dao.updateInsight(insightMapper.toEntity(insightWithClassification))
                    Result.Success(insightWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitInsight failed", e)
            Result.Error(e)
        }
    }

    /**
     * AI 분류 수행
     */
    override suspend fun classifyInsight(content: String): Result<Classification> = withContext(dispatcher) {
        try {
            val request = ClassificationRequest(content = content)
            val response = api.classifyCapture(request)

            if (response.isSuccessful && response.body() != null) {
                val classification = classificationMapper.toDomain(response.body()!!)
                Log.d(TAG, "Classification success: ${classification.type}")
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
     * Obsidian 노트 생성
     */
    override suspend fun createObsidianNote(
        classification: Classification,
        content: String
    ): Result<Boolean> = withContext(dispatcher) {
        try {
            val request = ObsidianCreateRequest(
                path = classification.destinationPath,
                title = classification.title,
                content = content,
                tags = classification.tags,
                metadata = classification.metadata
            )

            val response = api.createObsidianNote(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Obsidian note created: ${response.body()?.filePath}")
                Result.Success(true)
            } else {
                val error = Exception("Note creation failed: ${response.code()}")
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
     * first()로 단발 조회하여 작업이 정상 종료되도록 함
     */
    override suspend fun syncOfflineQueue(): Result<Int> = withContext(dispatcher) {
        try {
            // 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Sync skipped: No network")
                return@withContext Result.Error(Exception("No network"))
            }

            // PENDING 상태의 인사이트들 단발 조회 (first()로 Flow 종료)
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

                    // Obsidian 노트 생성
                    when (createObsidianNote(classification, insight.content)) {
                        is Result.Success -> {
                            // 성공: SYNCED 상태로 업데이트
                            dao.updateSyncStatus(insight.id, SyncStatus.SYNCED.name)
                            syncedCount++
                            Log.d(TAG, "Synced insight: ${insight.id}")
                        }
                        is Result.Error -> {
                            // 실패: 재시도 카운트 증가
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
     * 이미지를 OCR로 처리하여 텍스트 추출 후 Insight 제출
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

            // 2. Insight 객체 생성 (source = IMAGE, imageUri 포함)
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

            // 5. AI 분류 및 Obsidian 노트 생성 (기존 로직 재사용)
            val classification = when (val result = classifyInsight(extractedText)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, extractedText)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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
     * 음성 인식 결과 텍스트로 Insight 제출
     */
    override suspend fun submitVoiceInsight(audioText: String, audioUri: Uri?): Result<Insight> = withContext(dispatcher) {
        try {
            // 1. Insight 객체 생성 (source = VOICE, audioUri 포함)
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

            // 4. AI 분류 및 Obsidian 노트 생성
            val classification = when (val result = classifyInsight(audioText)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, audioText)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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
     * URL에서 메타데이터를 추출하여 Insight 제출
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
            }.ifBlank { url }  // 메타데이터 없으면 URL 사용

            // 3. Insight 객체 생성 (source = WEB_CLIP, webMetadata 포함)
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

            // 6. AI 분류 및 Obsidian 노트 생성
            val classification = when (val result = classifyInsight(content)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(insight)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, content)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * 긴 콘텐츠를 서버 AI로 자동 요약
     */
    override suspend fun generateSummary(
        insightId: String,
        content: String
    ): Result<String> = withContext(dispatcher) {
        try {
            val request = SummarizeRequest(
                captureId = insightId,
                content = content,
                maxLength = 200
            )

            val response = api.generateSummary(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val summary = response.body()?.summary ?: ""
                Log.d(TAG, "Summary generated: ${summary.take(50)}...")
                Result.Success(summary)
            } else {
                val error = Exception(response.body()?.error ?: "요약 생성 실패")
                Log.e(TAG, "Summary generation failed", error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "generateSummary failed", e)
            Result.Error(e)
        }
    }

    /**
     * M10: 스마트 태그 제안
     * 과거 패턴 기반 태그를 서버 AI로 제안
     */
    override suspend fun suggestTags(
        content: String,
        classification: String?
    ): Result<List<SuggestedTag>> = withContext(dispatcher) {
        try {
            val request = TagSuggestRequest(
                content = content,
                classification = classification,
                limit = 5
            )

            val response = api.suggestTags(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val tags = response.body()?.tags ?: emptyList()
                Log.d(TAG, "Tags suggested: ${tags.map { it.name }}")
                Result.Success(tags)
            } else {
                val error = Exception(response.body()?.error ?: "태그 제안 실패")
                Log.e(TAG, "Tag suggestion failed", error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "suggestTags failed", e)
            Result.Error(e)
        }
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
            // 타입 필터를 문자열 리스트로 변환
            val types = if (query.types.isEmpty()) null else ""
            val typesList = if (query.types.isEmpty()) null else query.types.map { it.name }

            // 소스 필터를 문자열 리스트로 변환
            val sources = if (query.sources.isEmpty()) null else ""
            val sourcesList = if (query.sources.isEmpty()) null else query.sources.map { it.name }

            // 날짜 범위
            val startDate = query.dateRange?.start
            val endDate = query.dateRange?.end

            // DAO 호출
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
     * Archive 화면에서 사용
     */
    override fun getInsightsGroupedByDate(): Flow<Map<String, List<Insight>>> {
        return dao.getRecentInsights(limit = 100)  // 최근 100개
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

    /**
     * 인사이트들을 날짜별로 그룹화
     * "Today", "Yesterday", "2026-01-25" 등의 키 사용
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
