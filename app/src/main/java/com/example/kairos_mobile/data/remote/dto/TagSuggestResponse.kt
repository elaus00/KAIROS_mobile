package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 태그 제안 응답 DTO
 * 서버의 POST /tags/suggest 엔드포인트 응답
 */
data class TagSuggestResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("tags")
    val tags: List<SuggestedTag>,

    @SerializedName("error")
    val error: String?
)

/**
 * 제안된 태그 정보
 */
data class SuggestedTag(
    @SerializedName("name")
    val name: String,  // 태그 이름

    @SerializedName("confidence")
    val confidence: Float,  // 제안 신뢰도 (0.0 ~ 1.0)

    @SerializedName("reason")
    val reason: String?  // 제안 이유 (예: "과거 유사 캡처에서 자주 사용")
)
