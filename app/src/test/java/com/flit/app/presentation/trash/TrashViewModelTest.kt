package com.flit.app.presentation.trash

import com.flit.app.domain.model.Capture
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.usecase.capture.EmptyTrashUseCase
import com.flit.app.domain.usecase.capture.HardDeleteCaptureUseCase
import com.flit.app.util.MainDispatcherRule
import com.flit.app.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * TrashViewModel 유닛 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrashViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var captureRepository: CaptureRepository
    private lateinit var hardDeleteCaptureUseCase: HardDeleteCaptureUseCase
    private lateinit var emptyTrashUseCase: EmptyTrashUseCase

    private val trashedCaptures = listOf(
        TestFixtures.capture(id = "cap-1", isTrashed = true, originalText = "휴지통 항목 1"),
        TestFixtures.capture(id = "cap-2", isTrashed = true, originalText = "휴지통 항목 2")
    )

    @Before
    fun setup() {
        captureRepository = mockk(relaxed = true)
        hardDeleteCaptureUseCase = mockk(relaxed = true)
        emptyTrashUseCase = mockk(relaxed = true)
    }

    private fun createViewModel(): TrashViewModel {
        return TrashViewModel(captureRepository, hardDeleteCaptureUseCase, emptyTrashUseCase)
    }

    @Test
    fun `init_loads_trashed_items`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("cap-1", state.items[0].id)
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `init_empty_trash_shows_empty_list`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `init_error_sets_error_message`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flow {
            throw RuntimeException("DB 에러")
        }

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("DB 에러", vm.uiState.value.errorMessage)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `restoreItem_calls_repository`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.restoreItem("cap-1")
        advanceUntilIdle()

        coVerify { captureRepository.restoreFromTrash("cap-1") }
    }

    @Test
    fun `restoreItem_error_sets_message`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)
        coEvery { captureRepository.restoreFromTrash("cap-1") } throws RuntimeException("복원 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.restoreItem("cap-1")
        advanceUntilIdle()

        assertEquals("복원 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `deleteItem_calls_hardDelete`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.deleteItem("cap-1")
        advanceUntilIdle()

        coVerify { hardDeleteCaptureUseCase("cap-1") }
    }

    @Test
    fun `deleteItem_error_sets_message`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)
        coEvery { hardDeleteCaptureUseCase("cap-1") } throws RuntimeException("삭제 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.deleteItem("cap-1")
        advanceUntilIdle()

        assertEquals("삭제 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `emptyTrash_calls_usecase`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.emptyTrash()
        advanceUntilIdle()

        coVerify { emptyTrashUseCase() }
    }

    @Test
    fun `emptyTrash_error_sets_message`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flowOf(trashedCaptures)
        coEvery { emptyTrashUseCase() } throws RuntimeException("비우기 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.emptyTrash()
        advanceUntilIdle()

        assertEquals("비우기 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `dismissError_clears_message`() = runTest {
        coEvery { captureRepository.getTrashedItems() } returns flow {
            throw RuntimeException("에러")
        }

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("에러", vm.uiState.value.errorMessage)

        vm.dismissError()

        assertNull(vm.uiState.value.errorMessage)
    }
}
