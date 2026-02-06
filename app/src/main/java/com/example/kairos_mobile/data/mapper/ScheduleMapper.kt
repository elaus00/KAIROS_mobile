package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
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

    fun toEntity(schedule: Schedule): ScheduleEntity {
        return ScheduleEntity(
            id = schedule.id,
            captureId = schedule.captureId,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            location = schedule.location,
            isAllDay = schedule.isAllDay,
            confidence = schedule.confidence.name,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt
        )
    }
}
