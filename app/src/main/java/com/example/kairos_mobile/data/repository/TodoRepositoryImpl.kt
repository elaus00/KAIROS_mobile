package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.mapper.TodoMapper
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 할 일 Repository 구현체
 */
@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val todoMapper: TodoMapper
) : TodoRepository {

    override suspend fun createTodo(todo: Todo) {
        todoDao.insert(todoMapper.toEntity(todo))
    }

    override suspend fun getTodoById(todoId: String): Todo? {
        return todoDao.getById(todoId)?.let { todoMapper.toDomain(it) }
    }

    override fun getActiveTodos(): Flow<List<Todo>> {
        return todoDao.getActiveTodos()
            .map { entities -> entities.map { todoMapper.toDomain(it) } }
    }

    override fun getCompletedTodos(): Flow<List<Todo>> {
        return todoDao.getCompletedTodos()
            .map { entities -> entities.map { todoMapper.toDomain(it) } }
    }

    override suspend fun toggleCompletion(todoId: String) {
        val now = System.currentTimeMillis()
        todoDao.toggleCompletion(todoId, now, now)
    }

    override suspend fun updateSortOrder(todoId: String, sortOrder: Int, sortSource: String) {
        todoDao.updateSortOrder(todoId, sortOrder, sortSource, System.currentTimeMillis())
    }

    override suspend fun deleteByCaptureId(captureId: String) {
        todoDao.deleteByCaptureId(captureId)
    }
}
