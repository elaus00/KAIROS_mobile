package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 캡처에서 투두 생성 UseCase
 */
class CreateTodoFromCaptureUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 캡처에서 투두 생성
     * @param captureId 원본 캡처 ID
     * @param classification AI 분류 결과
     */
    suspend operator fun invoke(
        captureId: String,
        classification: Classification
    ): Result<Todo> {
        return todoRepository.createTodoFromCapture(captureId, classification)
    }
}
