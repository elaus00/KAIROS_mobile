package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 마감일별 그룹화된 투두 조회 UseCase
 */
class GetTodosGroupedByDueDateUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 마감일별로 그룹화된 투두 리스트 조회
     * @return 그룹명(오늘, 내일, 이번 주 등)과 투두 리스트 맵
     */
    operator fun invoke(): Flow<Map<String, List<Todo>>> {
        return todoRepository.getTodosGroupedByDueDate()
    }
}
