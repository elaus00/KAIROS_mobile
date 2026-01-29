package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.domain.repository.TodoRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

/**
 * 투두 생성 UseCase
 */
class CreateTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 새 투두 생성
     * @param content 투두 내용
     * @param title 제목 (선택)
     * @param dueDate 마감일 (선택)
     * @param dueTime 마감 시간 (선택)
     * @param priority 우선순위
     * @param labels 라벨 리스트
     */
    suspend operator fun invoke(
        content: String,
        title: String? = null,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        priority: TodoPriority = TodoPriority.NONE,
        labels: List<String> = emptyList()
    ): Result<Todo> {
        if (content.isBlank()) {
            return Result.Error(IllegalArgumentException("투두 내용을 입력해주세요"))
        }

        val now = Instant.now()
        val todo = Todo(
            id = UUID.randomUUID().toString(),
            content = content,
            title = title,
            sourceInsightId = null,
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

        return todoRepository.createTodo(todo)
    }
}
