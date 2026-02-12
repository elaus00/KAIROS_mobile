package com.flit.app.presentation.settings.ai

import com.flit.app.domain.model.ClassificationPreset
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.SubscriptionTier

/**
 * AI 분류 설정 세부 화면 UI 상태
 */
data class AiClassificationSettingsUiState(
    val presets: List<ClassificationPreset> = emptyList(),
    val selectedPresetId: String = "default",
    val customInstruction: String = "",
    val captureFontSize: String = FontSizePreference.MEDIUM.name,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
)
