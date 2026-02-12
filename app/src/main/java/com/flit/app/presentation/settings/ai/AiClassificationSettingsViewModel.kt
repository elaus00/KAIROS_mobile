package com.flit.app.presentation.settings.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.classification.GetPresetsUseCase
import com.flit.app.domain.usecase.classification.SetCustomInstructionUseCase
import com.flit.app.domain.usecase.classification.SetPresetUseCase
import com.flit.app.domain.usecase.settings.PreferenceKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI 분류 설정 세부 화면 ViewModel
 */
@HiltViewModel
class AiClassificationSettingsViewModel @Inject constructor(
    private val getPresetsUseCase: GetPresetsUseCase,
    private val setPresetUseCase: SetPresetUseCase,
    private val setCustomInstructionUseCase: SetCustomInstructionUseCase,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiClassificationSettingsUiState())
    val uiState: StateFlow<AiClassificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadPresets()
        loadSubscription()
        loadCaptureFontSize()
    }

    /** 분류 프리셋 및 커스텀 인스트럭션 로드 */
    private fun loadPresets() {
        val presets = getPresetsUseCase()
        viewModelScope.launch {
            val currentPresetId = userPreferenceRepository.getString(PreferenceKeys.KEY_CLASSIFICATION_PRESET_ID, "default")
            val currentInstruction = userPreferenceRepository.getString(PreferenceKeys.KEY_CLASSIFICATION_CUSTOM_INSTRUCTION, "")
            _uiState.update {
                it.copy(
                    presets = presets,
                    selectedPresetId = currentPresetId,
                    customInstruction = currentInstruction
                )
            }
        }
    }

    /** 구독 상태 로드 */
    private fun loadSubscription() {
        val tier = subscriptionRepository.getCachedTier()
        _uiState.update { it.copy(subscriptionTier = tier) }
    }

    /** 분류 프리셋 변경 */
    fun setPreset(presetId: String) {
        viewModelScope.launch {
            setPresetUseCase(presetId)
            _uiState.update { it.copy(selectedPresetId = presetId) }
        }
    }

    /** 분류 커스텀 인스트럭션 변경 (UI 즉시 반영) */
    fun setCustomInstruction(instruction: String) {
        _uiState.update { it.copy(customInstruction = instruction) }
    }

    /** 분류 커스텀 인스트럭션 저장 (서버/로컬 영속화) */
    fun saveCustomInstruction() {
        viewModelScope.launch {
            setCustomInstructionUseCase(_uiState.value.customInstruction)
        }
    }

    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }
}
