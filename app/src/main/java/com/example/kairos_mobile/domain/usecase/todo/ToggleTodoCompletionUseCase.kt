package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 할 일 완료 상태 토글 UseCase
 */
@Singleton
class ToggleTodoCompletionUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val trackEventUseCase: TrackEventUseCase
) {
    suspend operator fun invoke(todoId: String) {
        // 완료 전환 전에 todo 조회 (완료 전 상태 확인)
        val todo = todoRepository.getTodoById(todoId)
        val wasCompleted = todo?.isCompleted ?: false

        todoRepository.toggleCompletion(todoId)

        // 미완료 → 완료 전환 시에만 이벤트 발행
        if (!wasCompleted && todo != null) {
            val timeSinceCreation = System.currentTimeMillis() - todo.createdAt
            trackEventUseCase(
                eventType = "todo_completed",
                eventData = """{"time_since_creation_ms":$timeSinceCreation}"""
            )
        }
    }
}
