package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 인사이트에서 투두 생성 UseCase
 */
class CreateTodoFromInsightUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 인사이트에서 투두 생성
     * @param insightId 원본 인사이트 ID
     * @param classification AI 분류 결과
     */
    suspend operator fun invoke(
        insightId: String,
        classification: Classification
    ): Result<Todo> {
        return todoRepository.createTodoFromInsight(insightId, classification)
    }
}
