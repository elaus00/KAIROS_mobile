package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import com.example.kairos_mobile.domain.model.Todo
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
            createdAt = todo.createdAt,
            updatedAt = todo.updatedAt
        )
    }
}
