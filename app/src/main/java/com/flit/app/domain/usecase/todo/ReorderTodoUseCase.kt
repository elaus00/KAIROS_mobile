package com.flit.app.domain.usecase.todo

import com.flit.app.domain.model.SortSource
import com.flit.app.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 할 일 순서 변경 UseCase
 * 사용자가 드래그로 순서를 변경하면 sort_source=USER로 업데이트
 */
@Singleton
class ReorderTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todoIds: List<String>) {
        todoIds.forEachIndexed { index, todoId ->
            todoRepository.updateSortOrder(todoId, index, SortSource.USER.name)
        }
    }
}
