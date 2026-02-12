package com.example.kairos_mobile.presentation.settings.ai

import com.example.kairos_mobile.domain.model.ClassificationPreset
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.classification.GetPresetsUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetCustomInstructionUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetPresetUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * AiClassificationSettingsViewModel 단위 테스트
 * - 프리셋 로드, 프리셋 변경, 커스텀 인스트럭션 저장 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiClassificationSettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getPresetsUseCase: GetPresetsUseCase
    private lateinit var setPresetUseCase: SetPresetUseCase
    private lateinit var setCustomInstructionUseCase: SetCustomInstructionUseCase
    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var subscriptionRepository: SubscriptionRepository

    private val testPresets = listOf(
        ClassificationPreset("default", "기본", "일반적인 분류 기준"),
        ClassificationPreset("work", "업무", "업무 중심 분류")
    )

    @Before
    fun setUp() {
        getPresetsUseCase = mockk()
        setPresetUseCase = mockk(relaxed = true)
        setCustomInstructionUseCase = mockk(relaxed = true)
        userPreferenceRepository = mockk(relaxed = true)
        subscriptionRepository = mockk(relaxed = true)
        every { getPresetsUseCase() } returns testPresets
        every { subscriptionRepository.getCachedTier() } returns SubscriptionTier.PREMIUM
        coEvery { userPreferenceRepository.getString(any(), any()) } answers { secondArg() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createViewModel(): AiClassificationSettingsViewModel {
        return AiClassificationSettingsViewModel(
            getPresetsUseCase,
            setPresetUseCase,
            setCustomInstructionUseCase,
            userPreferenceRepository,
            subscriptionRepository
        )
    }

    /** init 시 프리셋과 구독 상태를 로드한다 */
    @Test
    fun init_loads_presets_and_subscription() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(testPresets, viewModel.uiState.value.presets)
        assertEquals("default", viewModel.uiState.value.selectedPresetId)
        assertEquals(SubscriptionTier.PREMIUM, viewModel.uiState.value.subscriptionTier)
    }

    /** setPreset 호출 시 SetPresetUseCase에 위임한다 */
    @Test
    fun setPreset_delegates_to_usecase() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPreset("work")
        advanceUntilIdle()

        coVerify { setPresetUseCase("work") }
        assertEquals("work", viewModel.uiState.value.selectedPresetId)
    }

    /** saveCustomInstruction 호출 시 SetCustomInstructionUseCase에 위임한다 */
    @Test
    fun saveCustomInstruction_delegates_to_usecase() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCustomInstruction("테스트 규칙")
        viewModel.saveCustomInstruction()
        advanceUntilIdle()

        coVerify { setCustomInstructionUseCase("테스트 규칙") }
    }
}
