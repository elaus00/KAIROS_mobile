package com.flit.app.data.repository

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.domain.model.CalendarException
import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.domain.model.LocalCalendar
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalendarRepository 구현체
 * Android CalendarProvider 기반 로컬 캘린더 연동
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val scheduleDao: ScheduleDao,
    private val userPreferenceRepository: UserPreferenceRepository
) : CalendarRepository {

    companion object {
        private const val KEY_TARGET_CALENDAR_ID = "target_calendar_id"
    }

    override suspend fun syncToCalendar(
        scheduleId: String,
        title: String,
        startTime: Long,
        endTime: Long?,
        location: String?,
        isAllDay: Boolean
    ): String {
        return try {
            val targetCalendarId = resolveTargetCalendarId()
                ?: throw CalendarException.NoCalendarSelected()

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, targetCalendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.EVENT_LOCATION, location)

                if (isAllDay) {
                    val zoneId = ZoneId.systemDefault()
                    val startDate = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDate()
                    val startOfDayMs = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val endDate = endTime?.let {
                        Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().plusDays(1)
                    } ?: startDate.plusDays(1)
                    val endOfDayMs = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

                    put(CalendarContract.Events.DTSTART, startOfDayMs)
                    put(CalendarContract.Events.DTEND, endOfDayMs)
                    put(CalendarContract.Events.ALL_DAY, 1)
                    put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                } else {
                    put(CalendarContract.Events.DTSTART, startTime)
                    put(CalendarContract.Events.DTEND, endTime ?: (startTime + 60 * 60 * 1000L))
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }
            }

            val eventId = withContext(Dispatchers.IO) {
                ensureCalendarPermission()
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    ?: throw CalendarException.InsertFailed()
                uri.lastPathSegment ?: throw CalendarException.InsertFailed()
            }

            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNCED.name, eventId)
            eventId
        } catch (e: Exception) {
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNC_FAILED.name, null)
            throw when (e) {
                is CalendarException -> e
                is SecurityException -> CalendarException.PermissionDenied()
                else -> CalendarException.Unknown(e.message ?: "캘린더 요청 실패")
            }
        }
    }

    override suspend fun updateSyncStatus(
        scheduleId: String,
        status: CalendarSyncStatus,
        calendarEventId: String?
    ) {
        scheduleDao.updateCalendarSync(scheduleId, status.name, calendarEventId)
    }

    override fun isCalendarPermissionGranted(): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PermissionChecker.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PermissionChecker.PERMISSION_GRANTED
        return readGranted && writeGranted
    }

    override suspend fun getAvailableCalendars(): List<LocalCalendar> = withContext(Dispatchers.IO) {
        ensureCalendarPermission()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.IS_PRIMARY
        )

        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1"
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC"

        val calendars = mutableListOf<LocalCalendar>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val colorIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)
            val primaryIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)

            while (cursor.moveToNext()) {
                calendars += LocalCalendar(
                    id = cursor.getLong(idIndex),
                    displayName = cursor.getString(nameIndex) ?: "Unnamed",
                    accountName = cursor.getString(accountIndex) ?: "",
                    color = cursor.getInt(colorIndex),
                    isPrimary = cursor.getInt(primaryIndex) == 1
                )
            }
        }

        calendars
    }

    override suspend fun setTargetCalendarId(calendarId: Long) {
        userPreferenceRepository.setString(KEY_TARGET_CALENDAR_ID, calendarId.toString())
    }

    override suspend fun getTargetCalendarId(): Long? {
        val raw = userPreferenceRepository.getString(KEY_TARGET_CALENDAR_ID, "")
        return raw.toLongOrNull()
    }

    private suspend fun resolveTargetCalendarId(): Long? {
        val calendars = getAvailableCalendars()
        if (calendars.isEmpty()) {
            return null
        }

        val selected = getTargetCalendarId()
        if (selected != null && calendars.any { it.id == selected }) {
            return selected
        }

        val fallback = calendars.firstOrNull { it.isPrimary } ?: calendars.first()
        setTargetCalendarId(fallback.id)
        return fallback.id
    }

    private fun ensureCalendarPermission() {
        if (!isCalendarPermissionGranted()) {
            throw CalendarException.PermissionDenied()
        }
    }
}
