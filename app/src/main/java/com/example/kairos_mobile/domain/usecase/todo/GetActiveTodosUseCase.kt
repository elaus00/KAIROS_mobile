package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 활성 할 일 조회 UseCase
 */
@Singleton
class GetActiveTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> {
        return todoRepository.getActiveTodos()
    }
}
