package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * AI 분류 요청 DTO (API v2.1)
 */
data class ClassifyRequest(
    @SerializedName("content")
    val content: String,                              // 분류할 콘텐츠

    @SerializedName("content_type")
    val contentType: String,                          // "text", "image", "audio", "url"

    @SerializedName("context")
    val context: Map<String, String>? = null          // 추가 컨텍스트 (선택)
)
