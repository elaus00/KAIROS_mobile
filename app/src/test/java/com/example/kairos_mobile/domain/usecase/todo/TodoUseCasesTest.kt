package com.example.kairos_mobile.domain.usecase.todo

import com.example.kairos_mobile.domain.model.SortSource
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * ToggleTodoCompletionUseCase, ReorderTodoUseCase 유닛 테스트
 */
class TodoUseCasesTest {

    private lateinit var todoRepository: TodoRepository
    private lateinit var trackEventUseCase: TrackEventUseCase
    private lateinit var toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase
    private lateinit var reorderTodoUseCase: ReorderTodoUseCase

    @Before
    fun setup() {
        todoRepository = mockk(relaxed = true)
        trackEventUseCase = mockk(relaxed = true)
        toggleTodoCompletionUseCase = ToggleTodoCompletionUseCase(todoRepository, trackEventUseCase)
        reorderTodoUseCase = ReorderTodoUseCase(todoRepository)
    }

    // === ToggleTodoCompletionUseCase ===

    @Test
    fun `toggle_incomplete_to_complete_tracks_event`() = runTest {
        val todo = TestFixtures.todo(
            id = "todo-1",
            isCompleted = false,
            captureId = "cap-1"
        )
        coEvery { todoRepository.getTodoById("todo-1") } returns todo

        toggleTodoCompletionUseCase("todo-1")

        coVerify { todoRepository.toggleCompletion("todo-1") }
        coVerify {
            trackEventUseCase(
                eventType = "todo_completed",
                eventData = match { it.contains("time_since_creation_ms") }
            )
        }
    }

    @Test
    fun `toggle_complete_to_incomplete_does_not_track_event`() = runTest {
        val todo = TestFixtures.todo(
            id = "todo-1",
            isCompleted = true,
            captureId = "cap-1"
        )
        coEvery { todoRepository.getTodoById("todo-1") } returns todo

        toggleTodoCompletionUseCase("todo-1")

        coVerify { todoRepository.toggleCompletion("todo-1") }
        coVerify(exactly = 0) {
            trackEventUseCase(eventType = "todo_completed", eventData = any())
        }
    }

    @Test
    fun `toggle_null_todo_does_not_track_event`() = runTest {
        coEvery { todoRepository.getTodoById("todo-999") } returns null

        toggleTodoCompletionUseCase("todo-999")

        coVerify { todoRepository.toggleCompletion("todo-999") }
        coVerify(exactly = 0) {
            trackEventUseCase(eventType = any(), eventData = any())
        }
    }

    // === ReorderTodoUseCase ===

    @Test
    fun `reorder_updates_sort_order_for_each_todo`() = runTest {
        val todoIds = listOf("todo-a", "todo-b", "todo-c")

        reorderTodoUseCase(todoIds)

        coVerify { todoRepository.updateSortOrder("todo-a", 0, SortSource.USER.name) }
        coVerify { todoRepository.updateSortOrder("todo-b", 1, SortSource.USER.name) }
        coVerify { todoRepository.updateSortOrder("todo-c", 2, SortSource.USER.name) }
    }

    @Test
    fun `reorder_empty_list_does_nothing`() = runTest {
        reorderTodoUseCase(emptyList())

        coVerify(exactly = 0) { todoRepository.updateSortOrder(any(), any(), any()) }
    }

    @Test
    fun `reorder_single_item_sets_index_zero`() = runTest {
        reorderTodoUseCase(listOf("todo-only"))

        coVerify { todoRepository.updateSortOrder("todo-only", 0, SortSource.USER.name) }
    }
}
