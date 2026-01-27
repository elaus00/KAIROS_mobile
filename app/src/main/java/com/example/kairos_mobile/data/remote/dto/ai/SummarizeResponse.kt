package com.example.kairos_mobile.data.remote.dto.ai

import com.google.gson.annotations.SerializedName

/**
 * AI 요약 응답 DTO
 * 서버의 POST /summarize 엔드포인트 응답
 */
data class SummarizeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("summary")
    val summary: String?,  // AI 생성 요약문

    @SerializedName("originalLength")
    val originalLength: Int?,  // 원본 텍스트 길이

    @SerializedName("summaryLength")
    val summaryLength: Int?,  // 요약문 길이

    @SerializedName("error")
    val error: String?
)
