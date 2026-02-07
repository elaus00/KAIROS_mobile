package com.example.kairos_mobile.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventDto
import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventsRequest
import com.example.kairos_mobile.domain.repository.AnalyticsRepository
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * 분석 이벤트 배치 전송 Worker
 * 1시간마다 미동기화 이벤트를 서버로 전송하고 동기화 완료 처리
 * 7일 이상 된 동기화 완료 이벤트는 자동 삭제
 */
@HiltWorker
class AnalyticsBatchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyticsRepository: AnalyticsRepository,
    private val kairosApi: KairosApi,
    private val deviceIdProvider: DeviceIdProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val events = analyticsRepository.getUnsynced(100)
        if (events.isEmpty()) return Result.success()

        val response = kairosApi.analyticsEvents(
            AnalyticsEventsRequest(
                deviceId = deviceIdProvider.getOrCreateDeviceId(),
                events = events.map {
                    AnalyticsEventDto(
                        eventType = it.eventType,
                        eventData = toEventDataObject(it.eventData),
                        timestamp = toIsoOffsetDateTime(it.timestamp)
                    )
                }
            )
        )
        if (!response.isSuccessful || response.body()?.status != "ok") {
            return Result.retry()
        }

        analyticsRepository.markSynced(events.map { it.id })

        // 7일 이상 된 동기화 완료 이벤트 삭제
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        analyticsRepository.deleteOld(sevenDaysAgo)

        return Result.success()
    }

    companion object {
        private val timestampFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        /**
         * 1시간 주기 배치 작업 등록
         */
        fun enqueuePeriodicWork(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<AnalyticsBatchWorker>(1, TimeUnit.HOURS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "analytics_batch",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun toEventDataObject(raw: String?): JsonObject {
        if (raw.isNullOrBlank()) {
            return JsonObject()
        }
        return runCatching {
            JsonParser.parseString(raw).asJsonObject
        }.getOrElse {
            JsonObject().apply {
                addProperty("raw", raw)
            }
        }
    }

    private fun toIsoOffsetDateTime(epochMs: Long): String {
        return Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .format(timestampFormatter)
    }
}
