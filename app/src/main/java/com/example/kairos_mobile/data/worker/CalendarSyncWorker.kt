package com.example.kairos_mobile.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.CaptureRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 캘린더 동기화 재시도 Worker
 * 1시간 주기로 SYNC_FAILED 상태인 일정을 재시도
 */
@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduleDao: ScheduleDao,
    private val calendarRepository: CalendarRepository,
    private val captureRepository: CaptureRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "CalendarSyncWorker 시작")

        // SYNC_FAILED 상태인 일정 조회
        val failedSchedules = scheduleDao.getByCalendarSyncStatus(CalendarSyncStatus.SYNC_FAILED.name)

        if (failedSchedules.isEmpty()) {
            Log.d(TAG, "재시도 대상 없음")
            return Result.success()
        }

        Log.d(TAG, "SYNC_FAILED 일정 ${failedSchedules.size}개 재시도")

        var retryCount = 0
        for (entity in failedSchedules) {
            try {
                val capture = captureRepository.getCaptureById(entity.captureId) ?: continue
                val title = capture.aiTitle ?: capture.originalText.take(30)
                val startTime = entity.startTime ?: continue

                calendarRepository.syncToCalendar(
                    scheduleId = entity.id,
                    title = title,
                    startTime = startTime,
                    endTime = entity.endTime,
                    location = entity.location,
                    isAllDay = entity.isAllDay
                )
                retryCount++
            } catch (e: Exception) {
                Log.e(TAG, "캘린더 동기화 재시도 실패: ${entity.id}", e)
            }
        }

        Log.d(TAG, "CalendarSyncWorker 완료: $retryCount 건 재시도 성공")
        return Result.success()
    }

    companion object {
        private const val TAG = "CalendarSyncWorker"
        private const val WORK_NAME = "calendar_sync_retry"

        /**
         * 1시간 주기 캘린더 동기화 재시도 작업 등록
         */
        fun enqueuePeriodicWork(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<CalendarSyncWorker>(
                1, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
