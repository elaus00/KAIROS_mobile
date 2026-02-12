package com.flit.app.data.mapper

import com.flit.app.data.remote.dto.v2.ClassifyResponse
import com.flit.app.data.remote.dto.v2.EntityDto
import com.flit.app.data.remote.dto.v2.ScheduleInfoDto
import com.flit.app.data.remote.dto.v2.SplitItemDto
import com.flit.app.data.remote.dto.v2.TodoInfoDto
import com.flit.app.domain.model.Classification
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.ConfidenceLevel
import com.flit.app.domain.model.DeadlineSource
import com.flit.app.domain.model.EntityType
import com.flit.app.domain.model.ExtractedEntity
import com.flit.app.domain.model.NoteSubType
import com.flit.app.domain.model.ScheduleInfo
import com.flit.app.domain.model.SplitItem
import com.flit.app.domain.model.TodoInfo
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classification DTO → Domain 변환 Mapper
 */
@Singleton
class ClassificationMapper @Inject constructor() {

    /**
     * ClassifyResponse (DTO) → Classification (Domain)
     */
    fun toDomain(response: ClassifyResponse): Classification {
        return Classification(
            type = parseClassifiedType(response.classifiedType),
            subType = response.noteSubType?.let { parseNoteSubType(it) },
            confidence = parseConfidence(response.confidence),
            aiTitle = response.aiTitle,
            tags = response.tags,
            entities = response.entities.map { toDomain(it) },
            scheduleInfo = response.scheduleInfo?.let { toDomain(it) },
            todoInfo = response.todoInfo?.let { toDomain(it) },
            splitItems = response.splitItems?.map { toDomain(it) }
        )
    }

    /**
     * SplitItemDto → SplitItem 도메인 모델 변환
     */
    private fun toDomain(dto: SplitItemDto): SplitItem {
        return SplitItem(
            splitText = dto.splitText,
            classifiedType = parseClassifiedType(dto.classifiedType),
            noteSubType = dto.noteSubType?.let { parseNoteSubType(it) },
            confidence = parseConfidence(dto.confidence),
            aiTitle = dto.aiTitle,
            tags = dto.tags,
            scheduleInfo = dto.scheduleInfo?.let { toDomain(it) },
            todoInfo = dto.todoInfo?.let { toDomain(it) }
        )
    }

    private fun toDomain(dto: EntityDto): ExtractedEntity {
        return ExtractedEntity(
            captureId = "",  // 호출자가 설정
            type = parseEntityType(dto.type),
            value = dto.value,
            normalizedValue = dto.normalizedValue
        )
    }

    private fun toDomain(dto: ScheduleInfoDto): ScheduleInfo {
        return ScheduleInfo(
            startTime = parseIsoDateTimeToEpochMs(dto.startTime),
            endTime = parseIsoDateTimeToEpochMs(dto.endTime),
            location = dto.location,
            isAllDay = dto.isAllDay
        )
    }

    private fun toDomain(dto: TodoInfoDto): TodoInfo {
        return TodoInfo(
            deadline = parseIsoDateTimeToEpochMs(dto.deadline),
            deadlineSource = dto.deadlineSource?.let { parseDeadlineSource(it) }
        )
    }

    private fun parseIsoDateTimeToEpochMs(value: String?): Long? {
        if (value.isNullOrBlank()) {
            return null
        }
        return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }
            .recoverCatching { Instant.parse(value).toEpochMilli() }
            .getOrNull()
    }

    private fun parseDeadlineSource(value: String): DeadlineSource {
        return when (value.trim().uppercase()) {
            "AI_EXTRACTED" -> DeadlineSource.AI_EXTRACTED
            "AI_SUGGESTED" -> DeadlineSource.AI_SUGGESTED
            "AI" -> DeadlineSource.AI_EXTRACTED
            "USER" -> DeadlineSource.USER
            else -> DeadlineSource.AI_EXTRACTED
        }
    }

    private fun parseClassifiedType(value: String): ClassifiedType {
        return try {
            ClassifiedType.valueOf(value.uppercase())
        } catch (e: Exception) {
            ClassifiedType.TEMP
        }
    }

    private fun parseNoteSubType(value: String): NoteSubType {
        return try {
            NoteSubType.valueOf(value.uppercase())
        } catch (e: Exception) {
            NoteSubType.INBOX
        }
    }

    private fun parseConfidence(value: String): ConfidenceLevel {
        return try {
            ConfidenceLevel.valueOf(value.uppercase())
        } catch (e: Exception) {
            ConfidenceLevel.MEDIUM
        }
    }

    private fun parseEntityType(value: String): EntityType {
        val normalized = value.trim().uppercase()
        return when (normalized) {
            "LOCATION" -> EntityType.PLACE
            else -> {
                try {
                    EntityType.valueOf(normalized)
                } catch (e: Exception) {
                    EntityType.OTHER
                }
            }
        }
    }
}
