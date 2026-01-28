package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 웹 클립 요청 DTO (API v2.1)
 */
data class ClipRequest(
    @SerializedName("url")
    val url: String,                               // 클립할 URL

    @SerializedName("include_images")
    val includeImages: Boolean = false,            // 이미지 포함 여부

    @SerializedName("summarize")
    val summarize: Boolean = true                  // AI 요약 포함 여부
)
