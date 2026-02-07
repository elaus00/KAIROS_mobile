package com.example.kairos_mobile.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.domain.repository.CaptureRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 휴지통 자동 정리 Worker
 * 매일 1회 실행, 30일 이상 된 휴지통 항목 완전 삭제
 */
@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val captureRepository: CaptureRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "TrashCleanupWorker 시작")

        val threshold = System.currentTimeMillis() - THIRTY_DAYS_MS
        val overdueItems = captureRepository.getTrashedOverdue(threshold)

        if (overdueItems.isEmpty()) {
            Log.d(TAG, "정리 대상 휴지통 항목 없음")
            return Result.success()
        }

        Log.d(TAG, "30일 초과 휴지통 항목 ${overdueItems.size}개 삭제")

        var deletedCount = 0
        for (item in overdueItems) {
            try {
                captureRepository.hardDelete(item.id)
                deletedCount++
            } catch (e: Exception) {
                Log.e(TAG, "휴지통 항목 삭제 실패: ${item.id}", e)
            }
        }

        Log.d(TAG, "TrashCleanupWorker 완료: $deletedCount 건 삭제")
        return Result.success()
    }

    companion object {
        private const val TAG = "TrashCleanupWorker"
        private const val WORK_NAME = "trash_cleanup"
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000

        /**
         * 일일 휴지통 정리 작업 등록
         */
        fun enqueuePeriodicWork(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
                1, TimeUnit.DAYS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
