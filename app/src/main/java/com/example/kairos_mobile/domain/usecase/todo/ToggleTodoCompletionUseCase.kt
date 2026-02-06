package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 할 일 완료 상태 토글 UseCase
 */
@Singleton
class ToggleTodoCompletionUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todoId: String) {
        todoRepository.toggleCompletion(todoId)
    }
}
