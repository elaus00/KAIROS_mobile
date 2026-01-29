package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.TodoMetadataDto
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Destination
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.TodoMetadata
import com.example.kairos_mobile.domain.model.TodoPriority
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classification DTO <-> Domain 변환 Mapper (API v2.1)
 */
@Singleton
class ClassificationMapper @Inject constructor() {

    /**
     * ClassifyResponse (DTO) → Classification (Domain)
     */
    fun toDomain(response: ClassifyResponse): Classification {
        return Classification(
            type = CaptureType.fromApiValue(response.type),
            destination = Destination.fromApiValue(response.destination),
            confidence = response.confidence,
            reasoning = response.reasoning,
            title = response.title,
            tags = response.tags,
            suggestedFilename = response.suggestedFilename,
            suggestedPath = response.suggestedPath,
            todoMetadata = response.todoMetadata?.let { toDomain(it) }
        )
    }

    /**
     * TodoMetadataDto → TodoMetadata (Domain)
     */
    private fun toDomain(dto: TodoMetadataDto): TodoMetadata {
        return TodoMetadata(
            dueDate = dto.dueDate?.let { parseDateSafe(it) },
            dueTime = dto.dueTime?.let { parseTimeSafe(it) },
            priority = parsePriority(dto.priority),
            labels = dto.labels
        )
    }

    /**
     * 우선순위 문자열 → TodoPriority
     */
    private fun parsePriority(value: String): TodoPriority {
        return when (value.lowercase()) {
            "high" -> TodoPriority.HIGH
            "medium" -> TodoPriority.MEDIUM
            "low" -> TodoPriority.LOW
            else -> TodoPriority.NONE
        }
    }

    /**
     * ISO 날짜 문자열을 LocalDate로 안전하게 파싱
     */
    private fun parseDateSafe(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ISO 시간 문자열을 LocalTime으로 안전하게 파싱
     */
    private fun parseTimeSafe(timeStr: String): LocalTime? {
        return try {
            LocalTime.parse(timeStr)
        } catch (e: Exception) {
            null
        }
    }
}
