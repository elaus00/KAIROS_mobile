package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenExchangeRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenRequest
import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.CalendarApiException
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.RemoteCalendarEvent
import com.example.kairos_mobile.domain.repository.CalendarRepository
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalendarRepository 구현체
 * Mock API를 통한 Google Calendar 동기화
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val scheduleDao: ScheduleDao,
    private val deviceIdProvider: DeviceIdProvider
) : CalendarRepository {

    override suspend fun syncToCalendar(
        scheduleId: String,
        title: String,
        startTime: Long,
        endTime: Long?,
        location: String?,
        isAllDay: Boolean
    ): String {
        val schedule = scheduleDao.getById(scheduleId)
            ?: throw IllegalStateException("일정을 찾을 수 없습니다: $scheduleId")
        val request = CalendarEventRequest(
            captureId = schedule.captureId,
            title = title,
            startTime = toIsoOffsetDateTime(startTime),
            endTime = endTime?.let { toIsoOffsetDateTime(it) },
            location = location,
            isAllDay = isAllDay
        )
        try {
            val data = ApiResponseHandler.safeCall { api.createCalendarEvent(request) }
            val eventId = data.googleEventId
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNCED.name, eventId)
            return eventId
        } catch (e: Exception) {
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNC_FAILED.name, null)
            throw toCalendarException(e)
        }
    }

    override suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, googleEventId: String?) {
        scheduleDao.updateCalendarSync(scheduleId, status.name, googleEventId)
    }

    override suspend fun exchangeCalendarToken(code: String, redirectUri: String): Boolean {
        try {
            val data = ApiResponseHandler.safeCall {
                api.exchangeCalendarToken(
                    CalendarTokenExchangeRequest(
                        deviceId = deviceIdProvider.getOrCreateDeviceId(),
                        code = code,
                        redirectUri = redirectUri
                    )
                )
            }
            return data.connected
        } catch (e: Exception) {
            throw toCalendarException(e)
        }
    }

    override suspend fun saveCalendarToken(accessToken: String, refreshToken: String?, expiresAt: String?): Boolean {
        try {
            val data = ApiResponseHandler.safeCall {
                api.saveCalendarToken(
                    CalendarTokenRequest(
                        deviceId = deviceIdProvider.getOrCreateDeviceId(),
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = expiresAt
                    )
                )
            }
            return data.connected
        } catch (e: Exception) {
            throw toCalendarException(e)
        }
    }

    override suspend fun getCalendarEvents(startDate: LocalDate, endDate: LocalDate): List<RemoteCalendarEvent> {
        try {
            val data = ApiResponseHandler.safeCall {
                api.getCalendarEvents(startDate.toString(), endDate.toString())
            }
            return data.events.map { event ->
                RemoteCalendarEvent(
                    googleEventId = event.googleEventId,
                    title = event.title,
                    startTime = parseIsoDateTimeToEpochMs(event.startTime) ?: 0L,
                    endTime = parseIsoDateTimeToEpochMs(event.endTime),
                    location = event.location,
                    isAllDay = event.isAllDay,
                    source = event.source
                )
            }
        } catch (e: Exception) {
            throw toCalendarException(e)
        }
    }

    private fun toIsoOffsetDateTime(epochMs: Long): String {
        return Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    private fun parseIsoDateTimeToEpochMs(value: String?): Long? {
        if (value.isNullOrBlank()) {
            return null
        }
        return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }
            .recoverCatching { Instant.parse(value).toEpochMilli() }
            .getOrNull()
    }

    /**
     * ApiException → CalendarApiException 변환
     * 캘린더 전용 에러 코드는 CalendarApiException으로 매핑하고,
     * 이미 CalendarApiException이면 그대로 전달
     */
    private fun toCalendarException(e: Exception): Throwable {
        if (e is CalendarApiException) return e
        val code = (e as? ApiException)?.errorCode
        val msg = e.message ?: "캘린더 요청 실패"
        return when (code) {
            "GOOGLE_AUTH_REQUIRED" -> CalendarApiException.GoogleAuthRequired(msg)
            "GOOGLE_TOKEN_EXPIRED" -> CalendarApiException.GoogleTokenExpired(msg)
            "GOOGLE_API_ERROR" -> CalendarApiException.GoogleApiError(msg)
            else -> CalendarApiException.Unknown(msg)
        }
    }
}
