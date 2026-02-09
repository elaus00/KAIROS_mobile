package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/** 구독 정보 응답 */
data class SubscriptionResponse(
    @SerializedName("tier") val tier: String,
    @SerializedName("features") val features: SubscriptionFeaturesDto
)

data class SubscriptionFeaturesDto(
    @SerializedName("ai_grouping") val aiGrouping: Boolean = false,
    @SerializedName("inbox_classify") val inboxClassify: Boolean = false,
    @SerializedName("semantic_search") val semanticSearch: Boolean = false,
    @SerializedName("note_reorganize") val noteReorganize: Boolean = false,
    @SerializedName("analytics_dashboard") val analyticsDashboard: Boolean = false,
    @SerializedName("ocr") val ocr: Boolean = false,
    @SerializedName("classification_preset") val classificationPreset: Boolean = false,
    @SerializedName("custom_instruction") val customInstruction: Boolean = false
)

/** 구독 구매 검증 요청 */
data class SubscriptionVerifyRequest(
    @SerializedName("purchase_token") val purchaseToken: String,
    @SerializedName("product_id") val productId: String
)
