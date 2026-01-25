package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * AI 요약 요청 DTO
 * 서버의 POST /summarize 엔드포인트 요청
 */
data class SummarizeRequest(
    @SerializedName("captureId")
    val captureId: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("maxLength")
    val maxLength: Int = 200  // 요약 최대 길이 (기본값 200자)
)
