package com.example.kairos_mobile.domain.usecase.misc

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.note.GetNotesByFolderUseCase
import com.example.kairos_mobile.domain.usecase.note.MoveNoteToFolderUseCase
import com.example.kairos_mobile.domain.usecase.schedule.GetDatesWithSchedulesUseCase
import com.example.kairos_mobile.domain.usecase.schedule.GetSchedulesByDateUseCase
import com.example.kairos_mobile.domain.usecase.search.SearchCapturesUseCase
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetThemePreferenceUseCase
import com.example.kairos_mobile.domain.usecase.todo.GetActiveTodosUseCase
import com.example.kairos_mobile.domain.usecase.todo.ToggleTodoCompletionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DelegatingUseCasesTest {

    @Test
    fun note_usecases_delegate_to_repository() = runTest {
        val noteRepository = mockk<NoteRepository>()
        val getNotesByFolder = GetNotesByFolderUseCase(noteRepository)
        val moveNoteToFolder = MoveNoteToFolderUseCase(noteRepository)
        val notes = listOf(Note(captureId = "c1", folderId = "f1"))

        every { noteRepository.getNotesByFolderId("f1") } returns flowOf(notes)
        coEvery { noteRepository.moveToFolder("n1", "f2") } just runs

        getNotesByFolder("f1").test {
            assertEquals(notes, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        moveNoteToFolder("n1", "f2")
        coVerify(exactly = 1) { noteRepository.moveToFolder("n1", "f2") }
    }

    @Test
    fun schedule_usecases_delegate_to_repository() = runTest {
        val scheduleRepository = mockk<ScheduleRepository>()
        val getDatesWithSchedules = GetDatesWithSchedulesUseCase(scheduleRepository)
        val getSchedulesByDate = GetSchedulesByDateUseCase(scheduleRepository)
        val days = listOf(1L, 2L)
        val schedules = listOf(Schedule(captureId = "c1"))

        every { scheduleRepository.getDatesWithSchedules(100L, 200L) } returns flowOf(days)
        every { scheduleRepository.getSchedulesByDate(100L, 200L) } returns flowOf(schedules)

        getDatesWithSchedules(100L, 200L).test {
            assertEquals(days, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        getSchedulesByDate(100L, 200L).test {
            assertEquals(schedules, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun search_usecase_returns_empty_flow_for_blank_query() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val useCase = SearchCapturesUseCase(captureRepository)

        useCase("   ").test {
            awaitComplete()
        }

        every { captureRepository.searchCaptures(any()) } returns flowOf(emptyList())
        val expected = listOf(Capture(id = "c1", originalText = "hello", classifiedType = ClassifiedType.TEMP))
        every { captureRepository.searchCaptures("hello") } returns flowOf(expected)

        useCase("hello").test {
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun settings_usecases_delegate_to_user_preferences_repository() = runTest {
        val preferences = mockk<UserPreferenceRepository>()
        val getThemePreference = GetThemePreferenceUseCase(preferences)
        val setThemePreference = SetThemePreferenceUseCase(preferences)

        every { preferences.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { preferences.setThemePreference(ThemePreference.DARK) } just runs

        getThemePreference().test {
            assertEquals(ThemePreference.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        setThemePreference(ThemePreference.DARK)
        coVerify(exactly = 1) { preferences.setThemePreference(ThemePreference.DARK) }
    }

    @Test
    fun todo_usecases_delegate_to_repository() = runTest {
        val todoRepository = mockk<TodoRepository>()
        val getActiveTodos = GetActiveTodosUseCase(todoRepository)
        val toggleTodoCompletion = ToggleTodoCompletionUseCase(todoRepository)
        val todos = listOf(Todo(captureId = "c1"))

        every { todoRepository.getActiveTodos() } returns flowOf(todos)
        coEvery { todoRepository.toggleCompletion("t1") } just runs

        getActiveTodos().test {
            assertEquals(todos, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        toggleTodoCompletion("t1")
        coVerify(exactly = 1) { todoRepository.toggleCompletion("t1") }
    }
}
