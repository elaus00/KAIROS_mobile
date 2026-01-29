package com.example.kairos_mobile.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * CalendarScreen ViewModel (PRD v4.0)
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadDataForSelectedDate()
        loadDatesWithSchedules()
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.SelectDate -> selectDate(event.date)
            is CalendarEvent.ChangeMonth -> changeMonth(event.yearMonth)
            is CalendarEvent.ToggleMonthExpand -> toggleMonthExpand()
            is CalendarEvent.ToggleTaskComplete -> toggleTaskComplete(event.taskId)
            is CalendarEvent.DeleteTask -> deleteTask(event.taskId)
            is CalendarEvent.ClickSchedule -> { /* 네비게이션은 Screen에서 처리 */ }
            is CalendarEvent.ClickTask -> { /* 네비게이션은 Screen에서 처리 */ }
        }
    }

    /**
     * 날짜 선택
     */
    private fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDataForSelectedDate()

        // 선택된 날짜가 다른 월이면 월도 변경
        val newMonth = YearMonth.from(date)
        if (newMonth != _uiState.value.currentMonth) {
            _uiState.update { it.copy(currentMonth = newMonth) }
            loadDatesWithSchedules()
        }
    }

    /**
     * 월 변경
     */
    private fun changeMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth) }
        loadDatesWithSchedules()
    }

    /**
     * 월간 뷰 펼침/접기 토글
     */
    private fun toggleMonthExpand() {
        _uiState.update { it.copy(isMonthExpanded = !it.isMonthExpanded) }
    }

    /**
     * 선택된 날짜의 데이터 로드
     */
    private fun loadDataForSelectedDate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val selectedDate = _uiState.value.selectedDate

            // 일정과 할일을 함께 조회
            combine(
                scheduleRepository.getSchedulesByDate(selectedDate),
                todoRepository.getTodosByDate(selectedDate)
            ) { schedules, tasks ->
                Pair(schedules, tasks)
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "데이터 로드 실패"
                        )
                    }
                }
                .collect { (schedules, tasks) ->
                    _uiState.update {
                        it.copy(
                            schedules = schedules.sortedBy { s -> s.time },
                            tasks = tasks,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * 현재 월의 일정이 있는 날짜 로드 (dot 표시용)
     */
    private fun loadDatesWithSchedules() {
        viewModelScope.launch {
            val currentMonth = _uiState.value.currentMonth
            val startDate = currentMonth.atDay(1)
            val endDate = currentMonth.atEndOfMonth()

            scheduleRepository.getDatesWithSchedules(startDate, endDate)
                .catch { /* 에러 무시 */ }
                .collect { dates ->
                    _uiState.update {
                        it.copy(datesWithSchedules = dates.toSet())
                    }
                }
        }
    }

    /**
     * 할 일 완료 토글
     */
    private fun toggleTaskComplete(taskId: String) {
        viewModelScope.launch {
            todoRepository.toggleCompletion(taskId)
            // Flow가 자동으로 UI 업데이트
        }
    }

    /**
     * 할 일 삭제
     */
    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            todoRepository.deleteTodo(taskId)
            // Flow가 자동으로 UI 업데이트
        }
    }
}
