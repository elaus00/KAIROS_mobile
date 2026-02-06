package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.EntityDto
import com.example.kairos_mobile.data.remote.dto.v2.ScheduleInfoDto
import com.example.kairos_mobile.data.remote.dto.v2.TodoInfoDto
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.EntityType
import com.example.kairos_mobile.domain.model.ExtractedEntity
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.ScheduleInfo
import com.example.kairos_mobile.domain.model.TodoInfo
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
            todoInfo = response.todoInfo?.let { toDomain(it) }
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
            startTime = dto.startTime,
            endTime = dto.endTime,
            location = dto.location,
            isAllDay = dto.isAllDay
        )
    }

    private fun toDomain(dto: TodoInfoDto): TodoInfo {
        return TodoInfo(deadline = dto.deadline)
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
