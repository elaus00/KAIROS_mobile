package com.flit.app.presentation.calendar

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.usecase.calendar.ApproveCalendarSuggestionUseCase
import com.flit.app.domain.usecase.todo.ReorderTodoUseCase
import com.flit.app.domain.usecase.todo.ToggleTodoCompletionUseCase
import com.flit.app.presentation.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * CalendarScreen ViewModel
 * Repository + UseCase 기반으로 일정/할일 조회
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val application: Application,
    private val scheduleRepository: ScheduleRepository,
    private val todoRepository: TodoRepository,
    private val captureRepository: CaptureRepository,
    private val calendarRepository: CalendarRepository,
    private val toggleTodoCompletion: ToggleTodoCompletionUseCase,
    private val reorderTodo: ReorderTodoUseCase,
    private val approveSuggestion: ApproveCalendarSuggestionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<CalendarUiEvent>()
    val events: SharedFlow<CalendarUiEvent> = _events.asSharedFlow()

    private var schedulesJob: Job? = null
    private var todosJob: Job? = null
    private var datesJob: Job? = null
    private val pendingTrashJobs = mutableMapOf<String, Job>()

    companion object {
        private const val MOVE_TO_TRASH_DELAY_MS = 3_000L
    }

    init {
        loadSchedulesForSelectedDate()
        loadTodos()
        loadDatesWithSchedules()
        loadTargetCalendarName()
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
            is CalendarEvent.DeleteTask -> deleteByCaptureId(event.captureId)
            is CalendarEvent.DeleteSchedule -> deleteByCaptureId(event.captureId)
            is CalendarEvent.ApproveSuggestion -> approveCalendarSuggestion(event.scheduleId)
            is CalendarEvent.RejectSuggestion -> rejectCalendarSuggestion(event.scheduleId)
            is CalendarEvent.ReorderTasks -> reorderTasks(event.todoIds)
            is CalendarEvent.UpdateTaskDeadline -> updateTaskDeadline(event.todoId, event.deadlineMs)
            is CalendarEvent.StartEditSchedule -> startEditSchedule(event.schedule)
            is CalendarEvent.EditSchedule -> editSchedule(event)
            is CalendarEvent.DismissEditSchedule -> dismissEditSchedule()
            is CalendarEvent.ReorderSchedules -> reorderSchedules(event.scheduleIds)
            is CalendarEvent.NavigateWeek -> navigateWeek(event.referenceDate)
        }
    }

    /**
     * 주간 뷰 주차 이동 (선택 없이)
     * 오늘이 포함된 주로 돌아오면 오늘 자동 선택
     */
    private fun navigateWeek(referenceDate: LocalDate) {
        val today = LocalDate.now()
        val startOfWeek = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() % 7)
        val endOfWeek = startOfWeek.plusDays(6)
        if (today in startOfWeek..endOfWeek) {
            _uiState.update { it.copy(selectedDate = today, weekReference = null) }
        } else {
            _uiState.update { it.copy(selectedDate = null, weekReference = referenceDate) }
        }
        loadSchedulesForSelectedDate()
    }

    /**
     * 날짜 선택
     */
    private fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, weekReference = null) }
        loadSchedulesForSelectedDate()

        // 선택된 날짜가 다른 월이면 월도 변경
        val newMonth = YearMonth.from(date)
        if (newMonth != _uiState.value.currentMonth) {
            _uiState.update { it.copy(currentMonth = newMonth) }
            loadDatesWithSchedules()
        }
    }

    /**
     * 월 변경
     * 오늘이 포함된 달이면 오늘을 자동 선택, 아니면 선택 해제
     */
    private fun changeMonth(yearMonth: YearMonth) {
        val today = LocalDate.now()
        val newSelectedDate = if (YearMonth.from(today) == yearMonth) today else null
        _uiState.update { it.copy(selectedDate = newSelectedDate, currentMonth = yearMonth, weekReference = null) }
        loadSchedulesForSelectedDate()
        loadDatesWithSchedules()
    }

    /**
     * 월간 뷰 펼침/접기 토글
     */
    private fun toggleMonthExpand() {
        _uiState.update { it.copy(isMonthExpanded = !it.isMonthExpanded) }
    }

    /**
     * 선택된 날짜의 일정 로드
     */
    private fun loadSchedulesForSelectedDate() {
        schedulesJob?.cancel()
        val selectedDate = _uiState.value.selectedDate
        if (selectedDate == null) {
            _uiState.update { it.copy(schedules = emptyList(), isLoading = false) }
            return
        }
        schedulesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val zone = ZoneId.systemDefault()
            val startMs = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val endMs = selectedDate.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

            scheduleRepository.getSchedulesByDate(startMs, endMs)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
                .collect { schedules ->
                    // Schedule → ScheduleDisplayItem (Capture에서 제목 조회)
                    val displayItems = schedules.map { schedule ->
                        val capture = captureRepository.getCaptureById(schedule.captureId)
                        ScheduleDisplayItem(
                            scheduleId = schedule.id,
                            captureId = schedule.captureId,
                            title = capture?.aiTitle ?: capture?.originalText?.take(30) ?: "",
                            startTime = schedule.startTime,
                            endTime = schedule.endTime,
                            location = schedule.location,
                            isAllDay = schedule.isAllDay,
                            calendarSyncStatus = schedule.calendarSyncStatus
                        )
                    }

                    _uiState.update {
                        it.copy(
                            schedules = displayItems.sortedBy { item -> item.startTime },
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * 할 일 로드 — 완료/미완료 통합
     */
    private fun loadTodos() {
        todosJob?.cancel()
        todosJob = viewModelScope.launch {
            todoRepository.getAllTodos()
                .catch { /* 에러 무시 */ }
                .collect { todos ->
                    val displayItems = todos.map { todo ->
                        val capture = captureRepository.getCaptureById(todo.captureId)
                        TodoDisplayItem(
                            todoId = todo.id,
                            captureId = todo.captureId,
                            title = capture?.aiTitle ?: capture?.originalText?.take(30) ?: "",
                            deadline = todo.deadline,
                            isCompleted = todo.isCompleted,
                            deadlineSource = todo.deadlineSource?.name,
                            originalText = capture?.originalText
                        )
                    }
                    _uiState.update { it.copy(tasks = displayItems) }
                }
        }
    }

    /**
     * 할 일 드래그 순서 변경
     */
    private fun reorderTasks(todoIds: List<String>) {
        // 즉시 UI 반영
        _uiState.update { state ->
            val reordered = todoIds.mapNotNull { id ->
                state.tasks.find { it.todoId == id }
            }
            state.copy(tasks = reordered)
        }
        // DB 업데이트
        viewModelScope.launch {
            reorderTodo(todoIds)
        }
    }

    /**
     * 할 일 마감일 업데이트
     */
    private fun updateTaskDeadline(todoId: String, deadlineMs: Long) {
        viewModelScope.launch {
            try {
                todoRepository.updateDeadline(todoId, deadlineMs)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "마감일 변경에 실패했어요")
                }
            }
        }
    }

    /**
     * 상세 화면 네비게이션 이벤트 발행
     */
    fun navigateToDetail(captureId: String) {
        viewModelScope.launch {
            _events.emit(CalendarUiEvent.NavigateToDetail(captureId))
        }
    }

    /**
     * 일정 편집 시작
     */
    private fun startEditSchedule(schedule: ScheduleDisplayItem) {
        _uiState.update { it.copy(editingSchedule = schedule) }
    }

    /**
     * 일정 편집 닫기
     */
    private fun dismissEditSchedule() {
        _uiState.update { it.copy(editingSchedule = null) }
    }

    /**
     * 일정 편집 저장
     */
    private fun editSchedule(event: CalendarEvent.EditSchedule) {
        viewModelScope.launch {
            try {
                val existing = scheduleRepository.getScheduleById(event.scheduleId) ?: return@launch
                val updated = existing.copy(
                    startTime = event.startTime,
                    endTime = event.endTime,
                    location = event.location,
                    isAllDay = event.isAllDay,
                    updatedAt = System.currentTimeMillis()
                )
                scheduleRepository.updateSchedule(updated)

                // 제목이 변경되었으면 Capture.aiTitle도 업데이트
                captureRepository.updateAiTitle(existing.captureId, event.title)

                _uiState.update { it.copy(editingSchedule = null) }
                loadSchedulesForSelectedDate()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "일정 수정에 실패했어요")
                }
            }
        }
    }

    /**
     * 일정 드래그 순서 변경 (메모리 전용)
     */
    private fun reorderSchedules(scheduleIds: List<String>) {
        _uiState.update { state ->
            val reordered = scheduleIds.mapNotNull { id ->
                state.schedules.find { it.scheduleId == id }
            }
            state.copy(schedules = reordered)
        }
    }

    /**
     * 현재 월의 일정이 있는 날짜 로드 (dot 표시용)
     */
    private fun loadDatesWithSchedules() {
        datesJob?.cancel()
        datesJob = viewModelScope.launch {
            val currentMonth = _uiState.value.currentMonth
            val zone = ZoneId.systemDefault()
            val startMs = currentMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endMs = currentMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

            scheduleRepository.getDatesWithSchedules(startMs, endMs)
                .catch { /* 에러 무시 */ }
                .collect { startTimes ->
                    // start_time ms → 로컬 타임존 기준 LocalDate 변환
                    val dates = startTimes.mapNotNull { ms ->
                        try {
                            Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
                        } catch (_: Exception) {
                            null
                        }
                    }.toSet()

                    _uiState.update { it.copy(datesWithSchedules = dates) }
                }
        }
    }

    /**
     * 할 일 완료 토글
     * 즉시 UI에 반영 후 DB 업데이트
     */
    private fun toggleTaskComplete(taskId: String) {
        val beforeToggle = _uiState.value
        if (beforeToggle.tasks.none { it.todoId == taskId }) return

        // 즉시 UI 반영: 같은 리스트에서 완료 상태만 반전
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.todoId == taskId) task.copy(isCompleted = !task.isCompleted) else task
                }
            )
        }

        viewModelScope.launch {
            runCatching {
                toggleTodoCompletion(taskId, trackEvent = false)
            }.onFailure { error ->
                // DB 반영 실패 시 직전 상태로 롤백
                _uiState.update {
                    it.copy(
                        tasks = beforeToggle.tasks,
                        errorMessage = error.message ?: "할 일 상태 변경에 실패했어요"
                    )
                }
            }
            // 위젯 갱신
            WidgetUpdateHelper.updateTodoWidget(application)
        }
    }

    fun undoDelete(captureId: String) {
        viewModelScope.launch {
            try {
                pendingTrashJobs.remove(captureId)?.cancel()
                captureRepository.undoSoftDelete(captureId)
                _events.emit(CalendarUiEvent.UndoSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "실행 취소에 실패했어요")
                }
            }
        }
    }

    private fun deleteByCaptureId(captureId: String) {
        viewModelScope.launch {
            try {
                captureRepository.softDelete(captureId)
                scheduleMoveToTrash(captureId)
                _events.emit(CalendarUiEvent.DeleteSuccess(captureId))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "삭제에 실패했어요")
                }
            }
        }
    }

    /**
     * 연동 캘린더 이름 로드
     */
    private fun loadTargetCalendarName() {
        viewModelScope.launch {
            try {
                val targetId = calendarRepository.getTargetCalendarId()
                if (targetId != null) {
                    val calendars = calendarRepository.getAvailableCalendars()
                    val name = calendars.firstOrNull { it.id == targetId }?.displayName
                    _uiState.update { it.copy(targetCalendarName = name) }
                }
            } catch (_: Exception) {
                // 캘린더 이름 로드 실패 시 무시
            }
        }
    }

    /**
     * 캘린더 제안 승인
     */
    private fun approveCalendarSuggestion(scheduleId: String) {
        viewModelScope.launch {
            try {
                approveSuggestion(scheduleId)
                _events.emit(CalendarUiEvent.SyncApproved(_uiState.value.targetCalendarName))
                loadSchedulesForSelectedDate()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "캘린더 동기화에 실패했어요")
                }
            }
        }
    }

    /**
     * 캘린더 제안 거부
     */
    private fun rejectCalendarSuggestion(scheduleId: String) {
        viewModelScope.launch {
            try {
                calendarRepository.updateSyncStatus(scheduleId, CalendarSyncStatus.REJECTED)
                _events.emit(CalendarUiEvent.SyncRejected)
                loadSchedulesForSelectedDate()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "캘린더 동기화 거부에 실패했어요")
                }
            }
        }
    }

    private fun scheduleMoveToTrash(captureId: String) {
        pendingTrashJobs.remove(captureId)?.cancel()
        pendingTrashJobs[captureId] = viewModelScope.launch {
            try {
                delay(MOVE_TO_TRASH_DELAY_MS)
                captureRepository.moveToTrash(captureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "휴지통 이동에 실패했습니다.")
                }
            } finally {
                pendingTrashJobs.remove(captureId)
            }
        }
    }

    override fun onCleared() {
        schedulesJob?.cancel()
        todosJob?.cancel()
        datesJob?.cancel()
        val pendingIds = pendingTrashJobs.keys.toList()
        pendingTrashJobs.values.forEach { it.cancel() }
        pendingTrashJobs.clear()
        runBlocking(Dispatchers.IO) {
            pendingIds.forEach { captureId ->
                runCatching { captureRepository.moveToTrash(captureId) }
            }
        }
        super.onCleared()
    }
}
