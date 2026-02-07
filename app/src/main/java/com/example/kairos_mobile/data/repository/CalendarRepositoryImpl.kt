package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenExchangeRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenRequest
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
        val response = api.createCalendarEvent(request)
        val body = response.body()
        if (response.isSuccessful && body?.status == "ok" && body.data != null) {
            val eventId = body.data.googleEventId
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNCED.name, eventId)
            return eventId
        } else {
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNC_FAILED.name, null)
            throw parseCalendarError(body?.error?.code, body?.error?.message)
        }
    }

    override suspend fun deleteFromCalendar(googleEventId: String) {
        val response = api.deleteCalendarEvent(googleEventId)
        val body = response.body()
        if (!response.isSuccessful || body?.status != "ok" || body.data?.deleted != true) {
            throw parseCalendarError(body?.error?.code, body?.error?.message)
        }
    }

    override suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, googleEventId: String?) {
        scheduleDao.updateCalendarSync(scheduleId, status.name, googleEventId)
    }

    override suspend fun exchangeCalendarToken(code: String, redirectUri: String): Boolean {
        val response = api.exchangeCalendarToken(
            CalendarTokenExchangeRequest(
                deviceId = deviceIdProvider.getOrCreateDeviceId(),
                code = code,
                redirectUri = redirectUri
            )
        )
        val body = response.body()
        if (!response.isSuccessful || body?.status != "ok" || body.data == null) {
            throw parseCalendarError(body?.error?.code, body?.error?.message)
        }
        return body.data.connected
    }

    override suspend fun saveCalendarToken(accessToken: String, refreshToken: String?, expiresIn: Long?): Boolean {
        val response = api.saveCalendarToken(
            CalendarTokenRequest(
                deviceId = deviceIdProvider.getOrCreateDeviceId(),
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn
            )
        )
        val body = response.body()
        if (!response.isSuccessful || body?.status != "ok" || body.data == null) {
            throw parseCalendarError(body?.error?.code, body?.error?.message)
        }
        return body.data.connected
    }

    override suspend fun getCalendarEvents(startDate: LocalDate, endDate: LocalDate): List<RemoteCalendarEvent> {
        val response = api.getCalendarEvents(startDate.toString(), endDate.toString())
        val body = response.body()
        if (!response.isSuccessful || body?.status != "ok" || body.data == null) {
            throw parseCalendarError(body?.error?.code, body?.error?.message)
        }
        return body.data.events.map { event ->
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

    private fun parseCalendarError(code: String?, message: String?): Throwable {
        val resolved = message ?: "캘린더 요청 실패"
        return when (code) {
            "GOOGLE_AUTH_REQUIRED" -> CalendarApiException.GoogleAuthRequired(resolved)
            "GOOGLE_TOKEN_EXPIRED" -> CalendarApiException.GoogleTokenExpired(resolved)
            "GOOGLE_API_ERROR" -> CalendarApiException.GoogleApiError(resolved)
            else -> CalendarApiException.Unknown(resolved)
        }
    }
}
