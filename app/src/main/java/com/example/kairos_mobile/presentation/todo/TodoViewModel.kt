package com.example.kairos_mobile.presentation.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.domain.usecase.todo.CreateTodoUseCase
import com.example.kairos_mobile.domain.usecase.todo.DeleteTodoUseCase
import com.example.kairos_mobile.domain.usecase.todo.GetTodosGroupedByDueDateUseCase
import com.example.kairos_mobile.domain.usecase.todo.ToggleTodoCompletionUseCase
import com.example.kairos_mobile.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * TodoScreen ViewModel
 */
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val getTodosGroupedByDueDateUseCase: GetTodosGroupedByDueDateUseCase,
    private val createTodoUseCase: CreateTodoUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        observeTodos()
    }

    /**
     * 투두 목록 관찰
     */
    private fun observeTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 그룹화된 활성 투두 관찰
            launch {
                getTodosGroupedByDueDateUseCase().collect { grouped ->
                    _uiState.update {
                        it.copy(
                            groupedTodos = grouped,
                            isLoading = false
                        )
                    }
                }
            }

            // 완료된 투두 관찰
            launch {
                todoRepository.getCompletedTodos().collect { completed ->
                    _uiState.update { it.copy(completedTodos = completed) }
                }
            }
        }
    }

    /**
     * 투두 완료 상태 토글
     */
    fun onToggleCompletion(todoId: String) {
        viewModelScope.launch {
            when (val result = toggleTodoCompletionUseCase(todoId)) {
                is Result.Success -> {
                    // Flow가 자동으로 업데이트됨
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message ?: "완료 상태 변경 실패")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * 투두 삭제
     */
    fun onDeleteTodo(todoId: String) {
        viewModelScope.launch {
            when (val result = deleteTodoUseCase(todoId)) {
                is Result.Success -> {
                    // Flow가 자동으로 업데이트됨
                    _uiState.update { it.copy(selectedTodo = null) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message ?: "삭제 실패")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * 완료됨 섹션 토글
     */
    fun onToggleCompletedSection() {
        _uiState.update { it.copy(isCompletedExpanded = !it.isCompletedExpanded) }
    }

    /**
     * 투두 상세 보기
     */
    fun onSelectTodo(todo: Todo?) {
        _uiState.update { it.copy(selectedTodo = todo) }
    }

    /**
     * 새 투두 추가 모드 시작
     */
    fun onStartAddingTodo() {
        _uiState.update {
            it.copy(
                isAddingTodo = true,
                newTodoContent = "",
                newTodoDueDate = null,
                newTodoDueTime = null,
                newTodoPriority = TodoPriority.NONE
            )
        }
    }

    /**
     * 새 투두 추가 모드 종료
     */
    fun onCancelAddingTodo() {
        _uiState.update {
            it.copy(
                isAddingTodo = false,
                newTodoContent = "",
                newTodoDueDate = null,
                newTodoDueTime = null,
                newTodoPriority = TodoPriority.NONE
            )
        }
    }

    /**
     * 새 투두 내용 변경
     */
    fun onNewTodoContentChanged(content: String) {
        _uiState.update { it.copy(newTodoContent = content) }
    }

    /**
     * 새 투두 마감일 변경
     */
    fun onNewTodoDueDateChanged(date: LocalDate?) {
        _uiState.update { it.copy(newTodoDueDate = date) }
    }

    /**
     * 새 투두 마감 시간 변경
     */
    fun onNewTodoDueTimeChanged(time: LocalTime?) {
        _uiState.update { it.copy(newTodoDueTime = time) }
    }

    /**
     * 새 투두 우선순위 변경
     */
    fun onNewTodoPriorityChanged(priority: TodoPriority) {
        _uiState.update { it.copy(newTodoPriority = priority) }
    }

    /**
     * 새 투두 저장
     */
    fun onSaveNewTodo() {
        val state = _uiState.value
        if (state.newTodoContent.isBlank()) {
            _uiState.update { it.copy(errorMessage = "내용을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            when (val result = createTodoUseCase(
                content = state.newTodoContent,
                dueDate = state.newTodoDueDate,
                dueTime = state.newTodoDueTime,
                priority = state.newTodoPriority
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isAddingTodo = false,
                            newTodoContent = "",
                            newTodoDueDate = null,
                            newTodoDueTime = null,
                            newTodoPriority = TodoPriority.NONE
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message ?: "저장 실패")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
