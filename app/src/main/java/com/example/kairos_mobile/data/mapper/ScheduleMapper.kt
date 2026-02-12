package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.Schedule
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedule Entity ↔ Domain 변환 Mapper
 */
@Singleton
class ScheduleMapper @Inject constructor() {

    fun toDomain(entity: ScheduleEntity): Schedule {
        return Schedule(
            id = entity.id,
            captureId = entity.captureId,
            startTime = entity.startTime,
            endTime = entity.endTime,
            location = entity.location,
            isAllDay = entity.isAllDay,
            confidence = parseConfidenceLevel(entity.confidence),
            calendarSyncStatus = parseCalendarSyncStatus(entity.calendarSyncStatus),
            calendarEventId = entity.calendarEventId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun parseConfidenceLevel(value: String): ConfidenceLevel {
        return try {
            ConfidenceLevel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ConfidenceLevel.MEDIUM
        }
    }

    private fun parseCalendarSyncStatus(value: String): CalendarSyncStatus {
        return try {
            CalendarSyncStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CalendarSyncStatus.NOT_LINKED
        }
    }

    fun toEntity(schedule: Schedule): ScheduleEntity {
        return ScheduleEntity(
            id = schedule.id,
            captureId = schedule.captureId,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            location = schedule.location,
            isAllDay = schedule.isAllDay,
            confidence = schedule.confidence.name,
            calendarSyncStatus = schedule.calendarSyncStatus.name,
            calendarEventId = schedule.calendarEventId,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt
        )
    }
}
