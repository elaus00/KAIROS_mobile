package com.flit.app.presentation.calendar

import android.app.Application
import app.cash.turbine.test
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.usecase.calendar.ApproveCalendarSuggestionUseCase
import com.flit.app.domain.usecase.todo.ReorderTodoUseCase
import com.flit.app.domain.usecase.todo.ToggleTodoCompletionUseCase
import com.flit.app.util.MainDispatcherRule
import com.flit.app.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * CalendarViewModel 단위 테스트
 * 일정/할일 조회 및 삭제/복원 로직 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var todoRepository: TodoRepository
    private lateinit var captureRepository: CaptureRepository
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var toggleTodoCompletion: ToggleTodoCompletionUseCase
    private lateinit var reorderTodo: ReorderTodoUseCase
    private lateinit var approveSuggestion: ApproveCalendarSuggestionUseCase

    @Before
    fun setUp() {
        application = mockk(relaxed = true)
        scheduleRepository = mockk()
        todoRepository = mockk()
        captureRepository = mockk()
        calendarRepository = mockk(relaxed = true)
        toggleTodoCompletion = mockk()
        reorderTodo = mockk(relaxed = true)
        approveSuggestion = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * init{}에서 3개 load 메서드가 호출되므로
     * 기본 빈 Flow 설정 후 ViewModel 생성 헬퍼
     */
    private fun createViewModel(): CalendarViewModel {
        // init에서 호출되는 Repository 메서드에 대한 기본 응답 (타임존 의존 ms 파라미터 → any())
        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(emptyList())
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())

        return CalendarViewModel(
            application,
            scheduleRepository,
            todoRepository,
            captureRepository,
            calendarRepository,
            toggleTodoCompletion,
            reorderTodo,
            approveSuggestion
        )
    }

    // ========== init 로딩 테스트 ==========

    @Test
    fun `init_loads_schedules_for_today`() = runTest {
        // Given: 일정 1개 반환
        val schedule = TestFixtures.schedule(
            id = "sch-1", captureId = "cap-1",
            startTime = 1000L, endTime = 2000L, location = "서울"
        )
        val capture = TestFixtures.capture(id = "cap-1", aiTitle = "회의")

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(listOf(schedule))
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture

        // When
        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // Then: 일정이 uiState에 반영
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.schedules.size)
        assertEquals("회의", state.schedules[0].title)
        assertEquals("서울", state.schedules[0].location)
    }

    @Test
    fun `init_loads_all_todos`() = runTest {
        // Given: 할일 1개 반환
        val todo = TestFixtures.todo(id = "todo-1", captureId = "cap-2", isCompleted = false)
        val capture = TestFixtures.capture(id = "cap-2", aiTitle = "보고서 작성")

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(emptyList())
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(listOf(todo))
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())
        coEvery { captureRepository.getCaptureById("cap-2") } returns capture

        // When
        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // Then: 할일이 uiState에 반영
        val state = viewModel.uiState.value
        assertEquals(1, state.tasks.size)
        assertEquals("보고서 작성", state.tasks[0].title)
        assertFalse(state.tasks[0].isCompleted)
    }

    @Test
    fun `init_loads_dates_with_schedules`() = runTest {
        // Given: epochDay 리스트 반환
        val epochDay = LocalDate.of(2026, 2, 7).toEpochDay()

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(emptyList())
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(listOf(epochDay))
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())

        // When
        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // Then: LocalDate 세트로 변환됨
        val dates = viewModel.uiState.value.datesWithSchedules
        assertTrue(dates.contains(LocalDate.of(2026, 2, 7)))
    }

    // ========== 날짜/월 변경 ==========

    @Test
    fun `selectDate_reloads_schedules`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        val otherDate = LocalDate.of(2026, 2, 15)

        // When: 날짜 선택
        viewModel.onEvent(CalendarEvent.SelectDate(otherDate))
        advanceUntilIdle()

        // Then: selectedDate 변경됨
        assertEquals(otherDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `changeMonth_updates_and_reloads_dates`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        val nextMonth = YearMonth.of(2026, 3)

        // When: 월 변경
        viewModel.onEvent(CalendarEvent.ChangeMonth(nextMonth))
        advanceUntilIdle()

        // Then: currentMonth 변경됨
        assertEquals(nextMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `toggleMonthExpand_toggles`() = runTest {
        // Given: 초기값 false
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isMonthExpanded)

        // When: 토글
        viewModel.onEvent(CalendarEvent.ToggleMonthExpand)

        // Then: true로 변경
        assertTrue(viewModel.uiState.value.isMonthExpanded)

        // When: 다시 토글
        viewModel.onEvent(CalendarEvent.ToggleMonthExpand)

        // Then: false로 복귀
        assertFalse(viewModel.uiState.value.isMonthExpanded)
    }

    // ========== 정렬 및 캡처 미발견 처리 ==========

    @Test
    fun `schedules_sorted_by_startTime`() = runTest {
        // Given: startTime 순서가 뒤섞인 2개 일정
        val sch1 = TestFixtures.schedule(id = "s1", captureId = "c1", startTime = 5000L)
        val sch2 = TestFixtures.schedule(id = "s2", captureId = "c2", startTime = 1000L)
        val cap1 = TestFixtures.capture(id = "c1", aiTitle = "늦은 일정")
        val cap2 = TestFixtures.capture(id = "c2", aiTitle = "이른 일정")

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(listOf(sch1, sch2))
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())
        coEvery { captureRepository.getCaptureById("c1") } returns cap1
        coEvery { captureRepository.getCaptureById("c2") } returns cap2

        // When
        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // Then: startTime 오름차순 정렬
        val schedules = viewModel.uiState.value.schedules
        assertEquals(2, schedules.size)
        assertEquals("이른 일정", schedules[0].title)
        assertEquals("늦은 일정", schedules[1].title)
    }

    @Test
    fun `capture_not_found_uses_empty_title`() = runTest {
        // Given: 캡처가 null
        val schedule = TestFixtures.schedule(id = "s1", captureId = "missing-cap")

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(listOf(schedule))
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())
        coEvery { captureRepository.getCaptureById("missing-cap") } returns null

        // When
        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // Then: title이 빈 문자열
        assertEquals("", viewModel.uiState.value.schedules[0].title)
    }

    // ========== 할일 토글 / 삭제 / 복원 ==========

    @Test
    fun `toggleTaskComplete_delegates`() = runTest {
        // Given
        coEvery { toggleTodoCompletion(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(CalendarEvent.ToggleTaskComplete("todo-1"))
        advanceUntilIdle()

        // Then: UseCase 호출됨
        coVerify(exactly = 1) { toggleTodoCompletion("todo-1") }
    }

    @Test
    fun `delete_emits_event`() = runTest {
        // Given
        coEvery { captureRepository.softDelete(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When + Then: DeleteSuccess 이벤트 발행
        viewModel.events.test {
            viewModel.onEvent(CalendarEvent.DeleteSchedule("cap-1"))
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CalendarUiEvent.DeleteSuccess)
            assertEquals("cap-1", (event as CalendarUiEvent.DeleteSuccess).captureId)
            coVerify(exactly = 1) { captureRepository.softDelete("cap-1") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undoDelete_restores_and_emits`() = runTest {
        // Given
        coEvery { captureRepository.undoSoftDelete(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When + Then: UndoSuccess 이벤트 발행
        viewModel.events.test {
            viewModel.undoDelete("cap-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CalendarUiEvent.UndoSuccess)
            coVerify(exactly = 1) { captureRepository.undoSoftDelete("cap-1") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== 신규 이벤트 테스트 ==========

    @Test
    fun `updateTaskDeadline_delegates_to_repository`() = runTest {
        // Given
        coEvery { todoRepository.updateDeadline(any(), any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(CalendarEvent.UpdateTaskDeadline("todo-1", 1700000000000L))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { todoRepository.updateDeadline("todo-1", 1700000000000L) }
    }

    @Test
    fun `startEditSchedule_sets_editingSchedule`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        val schedule = ScheduleDisplayItem(
            scheduleId = "sch-1", captureId = "cap-1", title = "회의",
            startTime = 1000L, endTime = 2000L, location = "서울",
            isAllDay = false
        )

        // When
        viewModel.onEvent(CalendarEvent.StartEditSchedule(schedule))

        // Then
        assertEquals(schedule, viewModel.uiState.value.editingSchedule)
    }

    @Test
    fun `dismissEditSchedule_clears_editingSchedule`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        val schedule = ScheduleDisplayItem(
            scheduleId = "sch-1", captureId = "cap-1", title = "회의",
            startTime = 1000L, endTime = 2000L, location = null,
            isAllDay = false
        )
        viewModel.onEvent(CalendarEvent.StartEditSchedule(schedule))
        assertEquals(schedule, viewModel.uiState.value.editingSchedule)

        // When
        viewModel.onEvent(CalendarEvent.DismissEditSchedule)

        // Then
        assertEquals(null, viewModel.uiState.value.editingSchedule)
    }

    @Test
    fun `editSchedule_updates_repository_and_clears_state`() = runTest {
        // Given
        val existingSchedule = TestFixtures.schedule(
            id = "sch-1", captureId = "cap-1",
            startTime = 1000L, endTime = 2000L, location = "서울"
        )
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns existingSchedule
        coEvery { scheduleRepository.updateSchedule(any()) } just runs
        coEvery { captureRepository.updateAiTitle(any(), any()) } just runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        // 편집 상태 설정
        val displayItem = ScheduleDisplayItem(
            scheduleId = "sch-1", captureId = "cap-1", title = "회의",
            startTime = 1000L, endTime = 2000L, location = "서울",
            isAllDay = false
        )
        viewModel.onEvent(CalendarEvent.StartEditSchedule(displayItem))

        // When
        viewModel.onEvent(CalendarEvent.EditSchedule(
            scheduleId = "sch-1", title = "수정된 회의",
            startTime = 3000L, endTime = 4000L,
            location = "부산", isAllDay = false
        ))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { scheduleRepository.updateSchedule(any()) }
        coVerify(exactly = 1) { captureRepository.updateAiTitle("cap-1", "수정된 회의") }
        assertEquals(null, viewModel.uiState.value.editingSchedule)
    }

    @Test
    fun `reorderSchedules_updates_ui_state`() = runTest {
        // Given: 일정 2개
        val sch1 = TestFixtures.schedule(id = "s1", captureId = "c1", startTime = 1000L)
        val sch2 = TestFixtures.schedule(id = "s2", captureId = "c2", startTime = 2000L)
        val cap1 = TestFixtures.capture(id = "c1", aiTitle = "일정1")
        val cap2 = TestFixtures.capture(id = "c2", aiTitle = "일정2")

        every { scheduleRepository.getSchedulesByDate(any(), any()) } returns flowOf(listOf(sch1, sch2))
        every { scheduleRepository.getDatesWithSchedules(any(), any()) } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { todoRepository.getCompletedTodos() } returns flowOf(emptyList())
        coEvery { captureRepository.getCaptureById("c1") } returns cap1
        coEvery { captureRepository.getCaptureById("c2") } returns cap2

        val viewModel = CalendarViewModel(
            application, scheduleRepository, todoRepository, captureRepository,
            calendarRepository, toggleTodoCompletion,
            reorderTodo, approveSuggestion
        )
        advanceUntilIdle()

        // When: 순서 뒤집기
        viewModel.onEvent(CalendarEvent.ReorderSchedules(listOf("s2", "s1")))

        // Then: 순서가 변경됨
        val schedules = viewModel.uiState.value.schedules
        assertEquals("일정2", schedules[0].title)
        assertEquals("일정1", schedules[1].title)
    }

    @Test
    fun `navigateToDetail_emits_event`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When + Then
        viewModel.events.test {
            viewModel.navigateToDetail("cap-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CalendarUiEvent.NavigateToDetail)
            assertEquals("cap-1", (event as CalendarUiEvent.NavigateToDetail).captureId)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
