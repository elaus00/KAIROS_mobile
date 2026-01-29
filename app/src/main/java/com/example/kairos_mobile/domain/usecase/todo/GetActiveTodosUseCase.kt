package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 활성 투두 조회 UseCase
 */
class GetActiveTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 완료되지 않은 투두 리스트 조회
     */
    operator fun invoke(): Flow<List<Todo>> {
        return todoRepository.getActiveTodos()
    }
}
