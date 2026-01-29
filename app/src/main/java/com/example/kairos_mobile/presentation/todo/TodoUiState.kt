package com.example.kairos_mobile.presentation.todo

import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import java.time.LocalDate
import java.time.LocalTime

/**
 * TodoScreen UI 상태
 */
data class TodoUiState(
    // 그룹화된 투두 목록
    val groupedTodos: Map<String, List<Todo>> = emptyMap(),

    // 완료된 투두 목록
    val completedTodos: List<Todo> = emptyList(),

    // 완료됨 섹션 펼침 상태
    val isCompletedExpanded: Boolean = false,

    // 선택된 투두 (상세 보기)
    val selectedTodo: Todo? = null,

    // 새 투두 추가 모드
    val isAddingTodo: Boolean = false,
    val newTodoContent: String = "",
    val newTodoDueDate: LocalDate? = null,
    val newTodoDueTime: LocalTime? = null,
    val newTodoPriority: TodoPriority = TodoPriority.NONE,

    // 로딩 및 에러
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * 활성 투두 총 개수
     */
    val activeCount: Int
        get() = groupedTodos.values.sumOf { it.size }

    /**
     * 완료된 투두 개수
     */
    val completedCount: Int
        get() = completedTodos.size

    /**
     * 투두가 비어있는지 확인
     */
    val isEmpty: Boolean
        get() = groupedTodos.isEmpty() && completedTodos.isEmpty()
}
