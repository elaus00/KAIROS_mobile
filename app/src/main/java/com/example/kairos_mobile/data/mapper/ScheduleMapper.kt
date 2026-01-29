package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ScheduleCategory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Schedule Entity <-> Domain 변환 매퍼 (PRD v4.0)
 */
object ScheduleMapper {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Entity -> Domain 변환
     */
    fun toDomain(entity: ScheduleEntity): Schedule {
        return Schedule(
            id = entity.id,
            title = entity.title,
            time = try {
                LocalTime.parse(entity.time, timeFormatter)
            } catch (e: Exception) {
                LocalTime.NOON
            },
            date = LocalDate.ofEpochDay(entity.date),
            location = entity.location,
            category = try {
                ScheduleCategory.valueOf(entity.category)
            } catch (e: Exception) {
                ScheduleCategory.PERSONAL
            },
            googleCalendarId = entity.googleCalendarId,
            sourceCaptureId = entity.sourceCaptureId,
            createdAt = Instant.ofEpochMilli(entity.createdAt),
            updatedAt = Instant.ofEpochMilli(entity.updatedAt)
        )
    }

    /**
     * Domain -> Entity 변환
     */
    fun toEntity(domain: Schedule): ScheduleEntity {
        return ScheduleEntity(
            id = domain.id,
            title = domain.title,
            time = domain.time.format(timeFormatter),
            date = domain.date.toEpochDay(),
            location = domain.location,
            category = domain.category.name,
            googleCalendarId = domain.googleCalendarId,
            sourceCaptureId = domain.sourceCaptureId,
            createdAt = domain.createdAt.toEpochMilli(),
            updatedAt = domain.updatedAt.toEpochMilli()
        )
    }

    /**
     * Entity 리스트 -> Domain 리스트 변환
     */
    fun toDomainList(entities: List<ScheduleEntity>): List<Schedule> {
        return entities.map { toDomain(it) }
    }
}
