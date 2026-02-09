package com.example.kairos_mobile.domain.model

/** 구독 기능 플래그 */
data class SubscriptionFeatures(
    val aiGrouping: Boolean = false,
    val inboxClassify: Boolean = false,
    val semanticSearch: Boolean = false,
    val noteReorganize: Boolean = false,
    val analyticsDashboard: Boolean = false,
    val ocr: Boolean = false,
    val classificationPreset: Boolean = false,
    val customInstruction: Boolean = false
)
