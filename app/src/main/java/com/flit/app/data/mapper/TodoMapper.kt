package com.flit.app.data.mapper

import com.flit.app.data.local.database.entities.TodoEntity
import com.flit.app.domain.model.DeadlineSource
import com.flit.app.domain.model.SortSource
import com.flit.app.domain.model.Todo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Todo Entity ↔ Domain 변환 Mapper
 */
@Singleton
class TodoMapper @Inject constructor() {

    fun toDomain(entity: TodoEntity): Todo {
        return Todo(
            id = entity.id,
            captureId = entity.captureId,
            deadline = entity.deadline,
            isCompleted = entity.isCompleted,
            completedAt = entity.completedAt,
            sortOrder = entity.sortOrder,
            deadlineSource = entity.deadlineSource?.let { parseDeadlineSource(it) },
            sortSource = parseSortSource(entity.sortSource),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(todo: Todo): TodoEntity {
        return TodoEntity(
            id = todo.id,
            captureId = todo.captureId,
            deadline = todo.deadline,
            isCompleted = todo.isCompleted,
            completedAt = todo.completedAt,
            sortOrder = todo.sortOrder,
            deadlineSource = todo.deadlineSource?.name,
            sortSource = todo.sortSource.name,
            createdAt = todo.createdAt,
            updatedAt = todo.updatedAt
        )
    }

    private fun parseDeadlineSource(value: String): DeadlineSource {
        return try {
            DeadlineSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            if (value.equals("AI", ignoreCase = true)) {
                DeadlineSource.AI_EXTRACTED
            } else {
                DeadlineSource.AI_EXTRACTED
            }
        }
    }

    private fun parseSortSource(value: String): SortSource {
        return try {
            SortSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SortSource.DEFAULT
        }
    }
}
