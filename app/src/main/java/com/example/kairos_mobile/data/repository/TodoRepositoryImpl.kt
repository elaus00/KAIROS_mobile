package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.mapper.TodoMapper
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 투두 Repository 구현체
 */
@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {

    override fun getActiveTodos(): Flow<List<Todo>> {
        return todoDao.getActiveTodos().map { entities ->
            entities.map { TodoMapper.toDomain(it) }
        }
    }

    override fun getCompletedTodos(): Flow<List<Todo>> {
        return todoDao.getCompletedTodos().map { entities ->
            entities.map { TodoMapper.toDomain(it) }
        }
    }

    override fun getTodosGroupedByDueDate(): Flow<Map<String, List<Todo>>> {
        return todoDao.getActiveTodos().map { entities ->
            val todos = entities.map { TodoMapper.toDomain(it) }

            // 그룹화
            val grouped = todos.groupBy { it.getDueDateGroupKey() }

            // 정렬된 맵 생성
            val sortedMap = linkedMapOf<String, List<Todo>>()
            Todo.GROUP_ORDER.forEach { groupKey ->
                grouped[groupKey]?.let { groupTodos ->
                    sortedMap[groupKey] = groupTodos
                }
            }

            sortedMap
        }
    }

    override suspend fun createTodo(todo: Todo): Result<Todo> {
        return try {
            val entity = TodoMapper.toEntity(todo)
            todoDao.insert(entity)
            Result.Success(todo)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createTodoFromCapture(
        captureId: String,
        classification: Classification
    ): Result<Todo> {
        return try {
            // 이미 해당 캡처로 생성된 투두가 있는지 확인
            val existing = todoDao.getTodoByCaptureId(captureId)
            if (existing != null) {
                return Result.Success(TodoMapper.toDomain(existing))
            }

            val now = Instant.now()

            // todoMetadata에서 마감일/시간/우선순위 추출 (API v2.1)
            val todoMetadata = classification.todoMetadata
            val dueDate = todoMetadata?.dueDate
            val dueTime = todoMetadata?.dueTime
            val priority = todoMetadata?.priority ?: TodoPriority.NONE
            val labels = todoMetadata?.labels ?: classification.tags

            val todo = Todo(
                id = UUID.randomUUID().toString(),
                content = classification.title.ifBlank {
                    classification.suggestedPath?.substringAfterLast("/") ?: "Untitled"
                },
                title = classification.title.takeIf { it.isNotBlank() },
                sourceCaptureId = captureId,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority,
                labels = labels,
                manualOrder = 0,
                isCompleted = false,
                completedAt = null,
                createdAt = now,
                updatedAt = now
            )

            val entity = TodoMapper.toEntity(todo)
            todoDao.insert(entity)
            Result.Success(todo)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTodo(todo: Todo): Result<Todo> {
        return try {
            val updated = todo.copy(updatedAt = Instant.now())
            val entity = TodoMapper.toEntity(updated)
            todoDao.update(entity)
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleCompletion(id: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            todoDao.toggleCompletion(id, now, now)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteTodo(id: String): Result<Unit> {
        return try {
            todoDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTodoById(id: String): Result<Todo?> {
        return try {
            val entity = todoDao.getById(id)
            Result.Success(entity?.let { TodoMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getTodosByDate(date: LocalDate): Flow<List<Todo>> {
        val startOfDay = date.atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return todoDao.getTodosByDueDate(startOfDay, endOfDay).map { entities ->
            entities.map { TodoMapper.toDomain(it) }
        }
    }

    override fun getActiveCount(): Flow<Int> {
        return todoDao.getActiveCount()
    }
}
