package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Todo Entity <-> Domain 변환 매퍼
 */
object TodoMapper {

    private val gson = Gson()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Entity -> Domain 변환
     */
    fun toDomain(entity: TodoEntity): Todo {
        return Todo(
            id = entity.id,
            content = entity.content,
            title = entity.title,
            sourceInsightId = entity.sourceInsightId,
            dueDate = entity.dueDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            },
            dueTime = entity.dueTime?.let {
                try {
                    LocalTime.parse(it, timeFormatter)
                } catch (e: Exception) {
                    null
                }
            },
            priority = TodoPriority.fromValue(entity.priority),
            labels = parseLabels(entity.labels),
            manualOrder = entity.manualOrder,
            isCompleted = entity.isCompleted,
            completedAt = entity.completedAt?.let { Instant.ofEpochMilli(it) },
            createdAt = Instant.ofEpochMilli(entity.createdAt),
            updatedAt = Instant.ofEpochMilli(entity.updatedAt)
        )
    }

    /**
     * Domain -> Entity 변환
     */
    fun toEntity(domain: Todo): TodoEntity {
        return TodoEntity(
            id = domain.id,
            content = domain.content,
            title = domain.title,
            sourceInsightId = domain.sourceInsightId,
            dueDate = domain.dueDate?.let {
                it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            dueTime = domain.dueTime?.format(timeFormatter),
            priority = domain.priority.value,
            labels = serializeLabels(domain.labels),
            manualOrder = domain.manualOrder,
            isCompleted = domain.isCompleted,
            completedAt = domain.completedAt?.toEpochMilli(),
            createdAt = domain.createdAt.toEpochMilli(),
            updatedAt = domain.updatedAt.toEpochMilli()
        )
    }

    /**
     * JSON 문자열에서 라벨 리스트 파싱
     */
    private fun parseLabels(labelsJson: String?): List<String> {
        if (labelsJson.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(labelsJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 라벨 리스트를 JSON 문자열로 직렬화
     */
    private fun serializeLabels(labels: List<String>): String? {
        if (labels.isEmpty()) return null
        return gson.toJson(labels)
    }
}
