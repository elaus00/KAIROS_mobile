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
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.model.SyncQueueItem
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * TEMP 상태 캡처 주기적 재분류 Worker
 * 15분 주기로 TEMP 캡처를 SyncQueue에 등록하여 재분류 시도
 */
@HiltWorker
class ReclassifyTempWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val captureRepository: CaptureRepository,
    private val syncQueueRepository: SyncQueueRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "ReclassifyTempWorker 시작")

        val tempCaptures = captureRepository.getTempCaptures()
        if (tempCaptures.isEmpty()) {
            Log.d(TAG, "재분류 대상 TEMP 캡처 없음")
            return Result.success()
        }

        Log.d(TAG, "TEMP 캡처 ${tempCaptures.size}개 재분류 큐 등록")

        var enqueueCount = 0
        for (capture in tempCaptures) {
            try {
                val syncItem = SyncQueueItem(
                    action = SyncAction.CLASSIFY,
                    payload = capture.id
                )
                syncQueueRepository.enqueue(syncItem)
                enqueueCount++
            } catch (e: Exception) {
                Log.e(TAG, "SyncQueue 등록 실패: ${capture.id}", e)
            }
        }

        Log.d(TAG, "ReclassifyTempWorker 완료: $enqueueCount 건 등록")

        // SyncQueue에 등록했으므로 ClassifyCaptureWorker 실행
        if (enqueueCount > 0) {
            ClassifyCaptureWorker.enqueue(
                WorkManager.getInstance(applicationContext)
            )
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "ReclassifyTempWorker"
        private const val WORK_NAME = "reclassify_temp"

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
