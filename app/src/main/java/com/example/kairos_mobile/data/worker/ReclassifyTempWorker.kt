package com.example.kairos_mobile.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.data.mapper.ClassificationMapper
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyBatchItemDto
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyBatchRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.classification.ProcessClassificationResultUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * TEMP 상태 캡처 주기적 재분류 Worker
 * 15분 주기로 TEMP 캡처를 /classify/batch로 재분류 시도
 */
@HiltWorker
class ReclassifyTempWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val captureRepository: CaptureRepository,
    private val api: KairosApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val classificationMapper: ClassificationMapper,
    private val processClassificationResultUseCase: ProcessClassificationResultUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "ReclassifyTempWorker 시작")

        val tempCaptures = captureRepository.getTempCaptures()
        if (tempCaptures.isEmpty()) {
            Log.d(TAG, "재분류 대상 TEMP 캡처 없음")
            return Result.success()
        }

        Log.d(TAG, "TEMP 캡처 ${tempCaptures.size}개 배치 재분류 시작")

        var successCount = 0
        for (chunk in tempCaptures.chunked(BATCH_SIZE)) {
            val request = ClassifyBatchRequest(
                items = chunk.map { capture ->
                    ClassifyBatchItemDto(
                        captureId = capture.id,
                        text = capture.originalText
                    )
                },
                deviceId = deviceIdProvider.getOrCreateDeviceId()
            )
            val response = api.classifyBatch(request)
            val body = response.body()

            if (!response.isSuccessful || body?.status != "ok" || body.data == null) {
                Log.w(TAG, "배치 재분류 실패: http=${response.code()}, code=${body?.error?.code}")
                continue
            }

            body.data.results.forEach { result ->
                runCatching {
                    val classification = classificationMapper.toDomain(
                        ClassifyResponse(
                            classifiedType = result.classifiedType,
                            noteSubType = result.noteSubType,
                            confidence = result.confidence,
                            aiTitle = result.aiTitle,
                            tags = result.tags,
                            entities = result.entities,
                            scheduleInfo = result.scheduleInfo,
                            todoInfo = result.todoInfo,
                            splitItems = result.splitItems
                        )
                    )
                    processClassificationResultUseCase(result.captureId, classification)
                    successCount++
                }.onFailure { throwable ->
                    Log.e(TAG, "배치 분류 결과 적용 실패: ${result.captureId}", throwable)
                }
            }

            body.data.failed.forEach { failed ->
                Log.w(TAG, "배치 분류 실패 항목: ${failed.captureId}, error=${failed.error}")
            }
        }

        Log.d(TAG, "ReclassifyTempWorker 완료: $successCount 건 처리")
        return Result.success()
    }

    companion object {
        private const val TAG = "ReclassifyTempWorker"
        private const val WORK_NAME = "reclassify_temp"
        private const val BATCH_SIZE = 20

        /**
         * 15분 주기 재분류 작업 등록
         */
        fun enqueuePeriodicWork(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ReclassifyTempWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
