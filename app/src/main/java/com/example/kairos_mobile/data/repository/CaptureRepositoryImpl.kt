package com.example.kairos_mobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.CaptureQueueDao
import com.example.kairos_mobile.data.mapper.CaptureMapper
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.processor.OcrProcessor
import com.example.kairos_mobile.data.processor.WebClipper
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.ClassificationRequest
import com.example.kairos_mobile.data.remote.dto.ObsidianCreateRequest
import com.example.kairos_mobile.data.remote.dto.SuggestedTag
import com.example.kairos_mobile.data.remote.dto.SummarizeRequest
import com.example.kairos_mobile.data.remote.dto.TagSuggestRequest
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 Repository 구현체
 * 핵심 비즈니스 로직 포함
 */
@Singleton
class CaptureRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val dao: CaptureQueueDao,
    private val configRepository: ConfigRepository,
    private val captureMapper: CaptureMapper,
    private val classificationMapper: ClassificationMapper,
    private val ocrProcessor: OcrProcessor,  // Phase 2: OCR 프로세서
    private val webClipper: WebClipper,      // Phase 2: 웹 클리퍼
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CaptureRepository {

    companion object {
        private const val TAG = "CaptureRepository"
    }

    /**
     * 캡처 제출
     * - 먼저 로컬 DB에 저장 (절대 유실 방지)
     * - 네트워크 있으면 분류 + Obsidian 생성 시도
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
                    // 분류 실패: 로컬에 저장된 상태로 반환
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            // 5. Obsidian 노트 생성 시도
            when (val result = createObsidianNote(classification, content)) {
                is Result.Success -> {
                    // 성공: 동기화 완료 상태로 업데이트
                    val syncedCapture = capture.copy(
                        classification = classification,
                        syncStatus = SyncStatus.SYNCED
                    )
                    dao.updateCapture(captureMapper.toEntity(syncedCapture))
                    Log.d(TAG, "Capture synced successfully: ${capture.id}")
                    Result.Success(syncedCapture)
                }
                is Result.Error -> {
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
                    // 생성 실패: 분류 결과는 저장하고, 상태는 PENDING 유지
                    val captureWithClassification = capture.copy(
                        classification = classification
                    )
                    dao.updateCapture(captureMapper.toEntity(captureWithClassification))
                    Result.Success(captureWithClassification)
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitCapture failed", e)
            Result.Error(e)
        }
    }

    /**
     * AI 분류 수행
     */
    override suspend fun classifyCapture(content: String): Result<Classification> = withContext(dispatcher) {
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
            Log.e(TAG, "classifyCapture failed", e)
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
            // 네트워크 확인
            if (!configRepository.isNetworkAvailable()) {
                Log.d(TAG, "Sync skipped: No network")
                return@withContext Result.Error(Exception("No network"))
            }

            // PENDING 상태의 캡처들 조회
            val pendingEntities = dao.getCapturesByStatus(SyncStatus.PENDING.name)
            var syncedCount = 0

            pendingEntities.collect { entities ->
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

                        // Obsidian 노트 생성
                        when (createObsidianNote(classification, capture.content)) {
                            is Result.Success -> {
                                // 성공: SYNCED 상태로 업데이트
                                dao.updateSyncStatus(capture.id, SyncStatus.SYNCED.name)
                                syncedCount++
                                Log.d(TAG, "Synced capture: ${capture.id}")
                            }
                            is Result.Error -> {
                                // 실패: 재시도 카운트 증가
                                dao.incrementRetryCount(capture.id, System.currentTimeMillis())
                                Log.e(TAG, "Sync failed for ${capture.id}")
                            }
                            is Result.Loading -> {}
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing capture ${entity.id}", e)
                    }
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
     * M05: 이미지 캡처 + OCR
     * 이미지를 OCR로 처리하여 텍스트 추출 후 Capture 제출
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

            // 2. Capture 객체 생성 (source = IMAGE, imageUri 포함)
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

            // 5. AI 분류 및 Obsidian 노트 생성 (기존 로직 재사용)
            val classification = when (val result = classifyCapture(extractedText)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, extractedText)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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
     * M06: 음성 입력
     * 음성 인식 결과 텍스트로 Capture 제출
     */
    override suspend fun submitVoiceCapture(audioText: String, audioUri: Uri?): Result<Capture> = withContext(dispatcher) {
        try {
            // 1. Capture 객체 생성 (source = VOICE, audioUri 포함)
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

            // 4. AI 분류 및 Obsidian 노트 생성
            val classification = when (val result = classifyCapture(audioText)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, audioText)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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
     * M08: 웹 클립
     * URL에서 메타데이터를 추출하여 Capture 제출
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
            }.ifBlank { url }  // 메타데이터 없으면 URL 사용

            // 3. Capture 객체 생성 (source = WEB_CLIP, webMetadata 포함)
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

            // 6. AI 분류 및 Obsidian 노트 생성
            val classification = when (val result = classifyCapture(content)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.e(TAG, "Classification failed", result.exception)
                    return@withContext Result.Success(capture)
                }
                is Result.Loading -> return@withContext Result.Loading
            }

            when (val result = createObsidianNote(classification, content)) {
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
                    Log.e(TAG, "Obsidian note creation failed", result.exception)
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

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * 긴 콘텐츠를 서버 AI로 자동 요약
     */
    override suspend fun generateSummary(
        captureId: String,
        content: String
    ): Result<String> = withContext(dispatcher) {
        try {
            val request = SummarizeRequest(
                captureId = captureId,
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
}
