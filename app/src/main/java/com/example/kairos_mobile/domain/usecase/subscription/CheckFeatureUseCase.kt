package com.example.kairos_mobile.domain.usecase.subscription

import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 특정 기능 사용 가능 여부 확인 (캐시 기반, 빠름) */
class CheckFeatureUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(featureName: String): Boolean {
        val features = subscriptionRepository.getCachedFeatures()
        return when (featureName) {
            "ai_grouping" -> features.aiGrouping
            "inbox_classify" -> features.inboxClassify
            "semantic_search" -> features.semanticSearch
            "note_reorganize" -> features.noteReorganize
            "analytics_dashboard" -> features.analyticsDashboard
            "ocr" -> features.ocr
            "classification_preset" -> features.classificationPreset
            "custom_instruction" -> features.customInstruction
            else -> false
        }
    }
}
