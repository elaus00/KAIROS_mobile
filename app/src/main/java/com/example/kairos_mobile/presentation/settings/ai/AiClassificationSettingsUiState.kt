package com.example.kairos_mobile.presentation.settings.ai

import com.example.kairos_mobile.domain.model.ClassificationPreset
import com.example.kairos_mobile.domain.model.SubscriptionTier

/**
 * AI 분류 설정 세부 화면 UI 상태
 */
data class AiClassificationSettingsUiState(
    val presets: List<ClassificationPreset> = emptyList(),
    val selectedPresetId: String = "default",
    val customInstruction: String = "",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
)
