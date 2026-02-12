package com.example.kairos_mobile.data.worker

import android.content.Context
import android.os.Trace
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.UserContextDto
import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.SyncQueueStatus
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.classification.BuildModificationHistoryUseCase
import com.example.kairos_mobile.domain.usecase.classification.ProcessClassificationResultUseCase
import com.example.kairos_mobile.domain.usecase.settings.PreferenceKeys
import com.example.kairos_mobile.domain.usecase.subscription.CheckFeatureUseCase
import com.example.kairos_mobile.presentation.widget.WidgetUpdateHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.kairos_mobile.data.remote.dto.v2.OcrRequest
import java.util.concurrent.TimeUnit

/**
 * SyncQueue PENDING 항목 처리 Worker
 * API 호출 → 분류 결과 적용
 */
@HiltWorker
class ClassifyCaptureWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueRepository: SyncQueueRepository,
    private val captureRepository: CaptureRepository,
    private val api: KairosApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val classificationMapper: ClassificationMapper,
    private val processClassificationResult: ProcessClassificationResultUseCase,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val buildModificationHistoryUseCase: BuildModificationHistoryUseCase,
    private val checkFeatureUseCase: CheckFeatureUseCase
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "ClassifyCaptureWorker"
        private const val WORK_NAME = "classify_capture"
        private const val INITIAL_BACKOFF_MS = 5_000L
        private const val BACKOFF_MULTIPLIER = 3
        private const val TRACE_AI_CLASSIFICATION_COMPLETION = "ai_classification_completion"

        /**
         * 분류 작업 즉시 실행 (SyncQueue 항목 추가 시 호출)
         */
        fun enqueue(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ClassifyCaptureWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5, TimeUnit.SECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "ClassifyCaptureWorker 시작")

        val pendingItems = syncQueueRepository.getPendingItems()
        if (pendingItems.isEmpty()) {
            Log.d(TAG, "처리할 PENDING 항목 없음")
            return Result.success()
        }

        Log.d(TAG, "PENDING 항목 ${pendingItems.size}개 처리 시작")

        var successCount = 0
        var failCount = 0

        for (item in pendingItems) {
            try {
                // PROCESSING 상태로 변경
                syncQueueRepository.updateStatus(item.id, SyncQueueStatus.PROCESSING)

                // 캡처 조회
                val captureId = item.payload
                val capture = captureRepository.getCaptureById(captureId)
                if (capture == null) {
                    Log.w(TAG, "캡처를 찾을 수 없음: $captureId")
                    syncQueueRepository.updateStatus(item.id, SyncQueueStatus.COMPLETED)
                    continue
                }

                Trace.beginSection(TRACE_AI_CLASSIFICATION_COMPLETION)
                try {
                    // 이미지 캡처인 경우 OCR 선처리
                    var textToClassify = capture.originalText
                    val canUseOcr = checkFeatureUseCase("ocr")
                    if (canUseOcr && capture.imageUri != null && capture.originalText.isBlank()) {
                        try {
                            val imageFile = java.io.File(java.net.URI(capture.imageUri))
                            val imageBytes = imageFile.readBytes()
                            val base64Data = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                            val imageType = when (imageFile.extension.lowercase()) {
                                "png" -> "png"
                                "webp" -> "webp"
                                else -> "jpeg"
                            }
                            val ocrRequest = OcrRequest(
                                imageData = base64Data,
                                imageType = imageType
                            )
                            val ocrResponse = ApiResponseHandler.safeCall { api.ocr(ocrRequest) }
                            textToClassify = ocrResponse.text
                            Log.d(TAG, "OCR 완료: $captureId (${textToClassify.length}자)")
                        } catch (e: Exception) {
                            Log.w(TAG, "OCR 실패, 원본 텍스트로 분류: ${e.message}")
                        }
                    }

                    // API 호출
                    val source = when (capture.source.name) {
                        "APP", "SHARE_INTENT", "WIDGET" -> capture.source.name
                        else -> "APP"
                    }
                    // 사용자 분류 프리셋/커스텀 지시 주입
                    val presetId = userPreferenceRepository.getString(PreferenceKeys.KEY_CLASSIFICATION_PRESET_ID, "default")
                    val customInstruction = userPreferenceRepository.getString(PreferenceKeys.KEY_CLASSIFICATION_CUSTOM_INSTRUCTION, "")
                    val modificationHistory = buildModificationHistoryUseCase()
                    val userContext = UserContextDto(
                        modificationHistory = modificationHistory.takeIf { it.isNotEmpty() },
                        presetId = presetId.takeIf { it != "default" },
                        customInstruction = customInstruction.takeIf { it.isNotBlank() }
                    )

                    val request = ClassifyRequest(
                        text = textToClassify,
                        source = source,
                        deviceId = deviceIdProvider.getOrCreateDeviceId(),
                        userContext = userContext
                    )
                    val response = api.classify(request)
                    val data = ApiResponseHandler.unwrap(response)

                    // 분류 결과 적용
                    val classification = classificationMapper.toDomain(data)
                    processClassificationResult(captureId, classification)

                    // 완료 처리
                    syncQueueRepository.updateStatus(item.id, SyncQueueStatus.COMPLETED)
                    successCount++
                    Log.d(TAG, "분류 성공: $captureId → ${classification.type}")
                } catch (e: ApiException) {
                    if (e.isRetryable) {
                        handleRetry(item.id, item.retryCount, item.maxRetries)
                    } else {
                        syncQueueRepository.updateStatus(item.id, SyncQueueStatus.FAILED)
                    }
                    failCount++
                    Log.w(TAG, "API 응답 실패: code=${e.errorCode}, ${e.message}")
                } finally {
                    Trace.endSection()
                }
            } catch (e: Exception) {
                Log.e(TAG, "분류 처리 실패: ${item.payload}", e)
                handleRetry(item.id, item.retryCount, item.maxRetries)
                failCount++
            }
        }

        Log.d(TAG, "ClassifyCaptureWorker 완료: 성공 $successCount, 실패 $failCount")

        // 완료된 항목 정리
        syncQueueRepository.deleteCompleted()

        // 분류 완료 후 위젯 갱신 (새 TODO 생성 가능)
        if (successCount > 0) {
            WidgetUpdateHelper.updateAllWidgets(applicationContext)
        }

        return Result.success()
    }

    /**
     * 재시도 처리 (지수 백오프: 5s → 15s → 45s)
     */
    private suspend fun handleRetry(itemId: String, retryCount: Int, maxRetries: Int) {
        if (retryCount >= maxRetries) {
            syncQueueRepository.updateStatus(itemId, SyncQueueStatus.FAILED)
            Log.w(TAG, "최대 재시도 초과: $itemId")
        } else {
            val delayMs = INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER.toDouble(), retryCount.toDouble()).toLong()
            val nextRetryAt = System.currentTimeMillis() + delayMs
            syncQueueRepository.incrementRetry(itemId, nextRetryAt)
            Log.d(TAG, "재시도 예약: $itemId, ${retryCount + 1}/$maxRetries, ${delayMs}ms 후")
        }
    }

}
