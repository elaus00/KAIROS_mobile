package com.example.kairos_mobile.presentation.calendar.conflict

import com.example.kairos_mobile.domain.model.CalendarConflict
import com.example.kairos_mobile.domain.model.ConflictResolution
import com.example.kairos_mobile.domain.usecase.calendar.DetectCalendarConflictsUseCase
import com.example.kairos_mobile.domain.usecase.calendar.ResolveCalendarConflictUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ConflictResolutionViewModel 단위 테스트
 * - 충돌 목록 로드
 * - 충돌 해결
 * - 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConflictResolutionViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var detectConflictsUseCase: DetectCalendarConflictsUseCase
    private lateinit var resolveConflictUseCase: ResolveCalendarConflictUseCase
    private lateinit var viewModel: ConflictResolutionViewModel

    @Before
    fun setup() {
        detectConflictsUseCase = mockk()
        resolveConflictUseCase = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `충돌 감지 - 충돌 목록 로드`() = runTest {
        // given: 충돌 목록 mock
        val conflicts = listOf(
            CalendarConflict(
                scheduleId = "s1",
                googleEventId = "g1",
                localTitle = "로컬 제목",
                googleTitle = "Google 제목",
                localStartTime = 1000L,
                googleStartTime = 2000L,
                localEndTime = 3000L,
                googleEndTime = 4000L,
                localLocation = "로컬",
                googleLocation = "Google"
            )
        )
        coEvery { detectConflictsUseCase() } returns conflicts

        // when: ViewModel 초기화 (init에서 loadConflicts 호출)
        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        // then: 충돌 목록 로드됨
        assertEquals(1, viewModel.uiState.value.conflicts.size)
        assertEquals("s1", viewModel.uiState.value.conflicts[0].scheduleId)
        assertEquals("로컬 제목", viewModel.uiState.value.conflicts[0].localTitle)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `충돌 해결 - 해결 후 목록에서 제거`() = runTest {
        // given: 충돌 목록 초기화
        val conflict = CalendarConflict(
            scheduleId = "s1",
            googleEventId = "g1",
            localTitle = "로컬",
            googleTitle = "Google",
            localStartTime = 1000L,
            googleStartTime = 2000L,
            localEndTime = null,
            googleEndTime = null,
            localLocation = null,
            googleLocation = null
        )
        coEvery { detectConflictsUseCase() } returns listOf(conflict)
        coEvery { resolveConflictUseCase(any(), any()) } just Runs

        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.conflicts.size)

        // when: 충돌 해결
        viewModel.resolveConflict(conflict, ConflictResolution.OVERRIDE_LOCAL)
        advanceUntilIdle()

        // then: 충돌이 목록에서 제거됨
        assertTrue(viewModel.uiState.value.conflicts.isEmpty())
        assertNull(viewModel.uiState.value.resolvingId)
        coVerify { resolveConflictUseCase(conflict, ConflictResolution.OVERRIDE_LOCAL) }
    }

    @Test
    fun `충돌 해결 - OVERRIDE_GOOGLE 선택 시 Google 덮어쓰기`() = runTest {
        // given: 충돌 목록 초기화
        val conflict = CalendarConflict(
            scheduleId = "s1",
            googleEventId = "g1",
            localTitle = "로컬",
            googleTitle = "Google",
            localStartTime = 1000L,
            googleStartTime = 2000L,
            localEndTime = null,
            googleEndTime = null,
            localLocation = null,
            googleLocation = null
        )
        coEvery { detectConflictsUseCase() } returns listOf(conflict)
        coEvery { resolveConflictUseCase(any(), any()) } just Runs

        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        // when: 로컬 데이터로 Google 덮어쓰기 선택
        viewModel.resolveConflict(conflict, ConflictResolution.OVERRIDE_GOOGLE)
        advanceUntilIdle()

        // then: UseCase 호출됨
        coVerify { resolveConflictUseCase(conflict, ConflictResolution.OVERRIDE_GOOGLE) }
        assertTrue(viewModel.uiState.value.conflicts.isEmpty())
    }

    @Test
    fun `충돌 해결 - MERGE 선택 시 병합 처리`() = runTest {
        // given: 충돌 목록 초기화
        val conflict = CalendarConflict(
            scheduleId = "s1",
            googleEventId = "g1",
            localTitle = "로컬",
            googleTitle = "Google",
            localStartTime = 1000L,
            googleStartTime = 2000L,
            localEndTime = null,
            googleEndTime = null,
            localLocation = null,
            googleLocation = null
        )
        coEvery { detectConflictsUseCase() } returns listOf(conflict)
        coEvery { resolveConflictUseCase(any(), any()) } just Runs

        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        // when: 병합 선택
        viewModel.resolveConflict(conflict, ConflictResolution.MERGE)
        advanceUntilIdle()

        // then: UseCase 호출됨
        coVerify { resolveConflictUseCase(conflict, ConflictResolution.MERGE) }
        assertTrue(viewModel.uiState.value.conflicts.isEmpty())
    }

    @Test
    fun `충돌 감지 실패 시 에러 메시지 표시`() = runTest {
        // given: UseCase가 예외 던짐
        coEvery { detectConflictsUseCase() } throws RuntimeException("Network error")

        // when: ViewModel 초기화
        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        // then: 에러 메시지 설정
        assertEquals("Network error", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `충돌 해결 실패 시 에러 메시지 표시`() = runTest {
        // given: 충돌 목록 초기화
        val conflict = CalendarConflict(
            scheduleId = "s1",
            googleEventId = "g1",
            localTitle = "로컬",
            googleTitle = "Google",
            localStartTime = 1000L,
            googleStartTime = 2000L,
            localEndTime = null,
            googleEndTime = null,
            localLocation = null,
            googleLocation = null
        )
        coEvery { detectConflictsUseCase() } returns listOf(conflict)
        coEvery { resolveConflictUseCase(any(), any()) } throws RuntimeException("Resolve failed")

        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        // when: 충돌 해결 시도
        viewModel.resolveConflict(conflict, ConflictResolution.OVERRIDE_LOCAL)
        advanceUntilIdle()

        // then: 에러 메시지 표시 및 충돌 목록 유지
        assertEquals("Resolve failed", viewModel.uiState.value.errorMessage)
        assertEquals(1, viewModel.uiState.value.conflicts.size)
        assertNull(viewModel.uiState.value.resolvingId)
    }

    @Test
    fun `onErrorDismissed_clears_error_message`() = runTest {
        // given: 에러 발생
        coEvery { detectConflictsUseCase() } throws RuntimeException("Error")
        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)

        // when: 에러 메시지 닫기
        viewModel.onErrorDismissed()

        // then: 에러 메시지 초기화
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loadConflicts_재로드 시 상태 갱신`() = runTest {
        // given: 초기 충돌 목록
        coEvery { detectConflictsUseCase() } returns emptyList()
        viewModel = ConflictResolutionViewModel(detectConflictsUseCase, resolveConflictUseCase)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.conflicts.isEmpty())

        // 새로운 충돌 발생
        val newConflicts = listOf(
            CalendarConflict(
                scheduleId = "s2",
                googleEventId = "g2",
                localTitle = "새 충돌",
                googleTitle = "New Conflict",
                localStartTime = 5000L,
                googleStartTime = 6000L,
                localEndTime = null,
                googleEndTime = null,
                localLocation = null,
                googleLocation = null
            )
        )
        coEvery { detectConflictsUseCase() } returns newConflicts

        // when: 충돌 재로드
        viewModel.loadConflicts()
        advanceUntilIdle()

        // then: 새 충돌 목록 반영
        assertEquals(1, viewModel.uiState.value.conflicts.size)
        assertEquals("s2", viewModel.uiState.value.conflicts[0].scheduleId)
    }
}
