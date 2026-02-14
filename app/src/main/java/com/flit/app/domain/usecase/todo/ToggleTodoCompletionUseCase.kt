package com.flit.app.domain.usecase.todo

import android.util.Log
import com.flit.app.BuildConfig
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.usecase.analytics.TrackEventUseCase
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
    suspend operator fun invoke(todoId: String, trackEvent: Boolean = true) {
        val startedAt = System.currentTimeMillis()
        // 완료 전환 전에 todo 조회 (완료 전 상태 확인)
        val todo = todoRepository.getTodoById(todoId)
        val wasCompleted = todo?.isCompleted ?: false
        logDebug(
            "invoke start todoId=$todoId, trackEvent=$trackEvent, " +
                "beforeExists=${todo != null}, beforeDone=$wasCompleted, beforeUpdatedAt=${todo?.updatedAt}"
        )

        todoRepository.toggleCompletion(todoId)
        val updatedTodo = todoRepository.getTodoById(todoId)
        logDebug(
            "invoke afterToggle todoId=$todoId, afterExists=${updatedTodo != null}, " +
                "afterDone=${updatedTodo?.isCompleted}, afterUpdatedAt=${updatedTodo?.updatedAt}, " +
                "elapsedMs=${System.currentTimeMillis() - startedAt}"
        )

        // 미완료 → 완료 전환 시에만 이벤트 발행
        if (trackEvent && !wasCompleted && todo != null) {
            val timeSinceCreation = System.currentTimeMillis() - todo.createdAt
            runCatching {
                trackEventUseCase(
                    eventType = "todo_completed",
                    eventData = """{"time_since_creation_ms":$timeSinceCreation}"""
                )
            }
        }
    }
}

private const val TOGGLE_USECASE_TAG = "ToggleTodoUseCaseTrace"

private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TOGGLE_USECASE_TAG, message)
    }
}
