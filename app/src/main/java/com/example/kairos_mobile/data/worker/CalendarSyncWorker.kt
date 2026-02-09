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
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import java.time.LocalDate
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
    private val captureRepository: CaptureRepository,
    private val scheduleRepository: ScheduleRepository
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

        Log.d(TAG, "재시도 완료: $retryCount 건 성공")

        // 양방향 동기화: 서버 변경 → 로컬 반영
        syncRemoteChanges()

        return Result.success()
    }

    /**
     * 서버 캘린더 이벤트를 가져와 로컬 일정과 비교/반영
     * 충돌 없는 변경만 자동 반영
     */
    private suspend fun syncRemoteChanges() {
        try {
            val today = LocalDate.now()
            val endDate = today.plusDays(30)
            val remoteEvents = calendarRepository.getCalendarEvents(today, endDate)

            if (remoteEvents.isEmpty()) {
                Log.d(TAG, "원격 이벤트 없음")
                return
            }

            // 로컬 동기화된 일정 맵 (googleEventId → Schedule)
            val syncedSchedules = scheduleRepository.getSyncedSchedules()
            val localMap = syncedSchedules
                .filter { it.googleEventId != null }
                .associateBy { it.googleEventId!! }

            var updatedCount = 0
            for (event in remoteEvents) {
                val localSchedule = localMap[event.googleEventId] ?: continue

                // 로컬과 원격 시간이 다르면 원격 우선 반영 (충돌 없는 경우)
                val hasTimeChange = localSchedule.startTime != event.startTime
                        || localSchedule.endTime != event.endTime
                val hasLocationChange = localSchedule.location != event.location

                if (hasTimeChange || hasLocationChange) {
                    scheduleRepository.updateFromRemote(
                        scheduleId = localSchedule.id,
                        title = event.title,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        location = event.location
                    )
                    updatedCount++
                    Log.d(TAG, "원격 변경 반영: ${localSchedule.id}")
                }
            }

            Log.d(TAG, "양방향 동기화 완료: $updatedCount 건 업데이트")
        } catch (e: Exception) {
            Log.e(TAG, "양방향 동기화 실패", e)
        }
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
