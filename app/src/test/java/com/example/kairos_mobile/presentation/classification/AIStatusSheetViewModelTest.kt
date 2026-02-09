package com.example.kairos_mobile.presentation.classification

import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import com.example.kairos_mobile.domain.usecase.classification.ConfirmClassificationUseCase
import com.example.kairos_mobile.domain.usecase.classification.GetUnconfirmedClassificationsUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import com.example.kairos_mobile.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * AIStatusSheetViewModel 유닛 테스트
 */
class AIStatusSheetViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getUnconfirmedClassificationsUseCase: GetUnconfirmedClassificationsUseCase
    private lateinit var confirmClassificationUseCase: ConfirmClassificationUseCase
    private lateinit var captureRepository: CaptureRepository
    private lateinit var changeClassificationUseCase: ChangeClassificationUseCase

    private val unconfirmedCaptures = listOf(
        TestFixtures.capture(id = "cap-1", classifiedType = ClassifiedType.TODO, isConfirmed = false),
        TestFixtures.capture(id = "cap-2", classifiedType = ClassifiedType.SCHEDULE, isConfirmed = false),
        TestFixtures.capture(id = "cap-3", classifiedType = ClassifiedType.NOTES, isConfirmed = false)
    )

    @Before
    fun setup() {
        getUnconfirmedClassificationsUseCase = mockk()
        confirmClassificationUseCase = mockk(relaxed = true)
        captureRepository = mockk(relaxed = true)
        changeClassificationUseCase = mockk(relaxed = true)
    }

    private fun createViewModel(): AIStatusSheetViewModel {
        return AIStatusSheetViewModel(
            getUnconfirmedClassificationsUseCase,
            confirmClassificationUseCase,
            captureRepository,
            changeClassificationUseCase
        )
    }

    @Test
    fun `init_loads_unconfirmed_captures`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.unconfirmedCaptures.size)
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `init_empty_list_shows_no_items`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.unconfirmedCaptures.isEmpty())
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `confirmClassification_delegates_to_usecase`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirmClassification("cap-1")
        advanceUntilIdle()

        coVerify { confirmClassificationUseCase("cap-1") }
    }

    @Test
    fun `confirmClassification_error_sets_message`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)
        coEvery { confirmClassificationUseCase("cap-1") } throws RuntimeException("확인 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirmClassification("cap-1")
        advanceUntilIdle()

        assertEquals("확인 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `confirmAll_delegates_to_repository`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirmAll()
        advanceUntilIdle()

        coVerify { captureRepository.confirmAllClassifications() }
    }

    @Test
    fun `confirmAll_error_sets_message`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)
        coEvery { captureRepository.confirmAllClassifications() } throws RuntimeException("일괄 확인 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirmAll()
        advanceUntilIdle()

        assertEquals("일괄 확인 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `changeClassification_delegates_to_usecase`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.changeClassification("cap-1", ClassifiedType.NOTES, NoteSubType.IDEA)
        advanceUntilIdle()

        coVerify { changeClassificationUseCase("cap-1", ClassifiedType.NOTES, NoteSubType.IDEA) }
    }

    @Test
    fun `changeClassification_error_sets_message`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)
        coEvery {
            changeClassificationUseCase("cap-1", ClassifiedType.TODO, null)
        } throws RuntimeException("변경 실패")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.changeClassification("cap-1", ClassifiedType.TODO, null)
        advanceUntilIdle()

        assertEquals("변경 실패", vm.uiState.value.errorMessage)
    }

    @Test
    fun `dismissError_clears_message`() = runTest {
        every { getUnconfirmedClassificationsUseCase() } returns flowOf(unconfirmedCaptures)
        coEvery { confirmClassificationUseCase("cap-1") } throws RuntimeException("에러")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.confirmClassification("cap-1")
        advanceUntilIdle()
        assertEquals("에러", vm.uiState.value.errorMessage)

        vm.dismissError()
        assertNull(vm.uiState.value.errorMessage)
    }
}
