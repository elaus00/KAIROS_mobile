package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 투두 완료 상태 토글 UseCase
 */
class ToggleTodoCompletionUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 투두 완료 상태 토글
     * @param id 투두 ID
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        return todoRepository.toggleCompletion(id)
    }
}
