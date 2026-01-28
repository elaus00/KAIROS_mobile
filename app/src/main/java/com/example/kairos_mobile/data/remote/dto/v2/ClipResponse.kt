package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 웹 클립 응답 DTO (API v2.1)
 */
data class ClipResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("url")
    val url: String? = null,                       // 원본 URL

    @SerializedName("title")
    val title: String? = null,                     // 페이지 제목

    @SerializedName("content")
    val content: String? = null,                   // 추출된 본문

    @SerializedName("summary")
    val summary: String? = null,                   // AI 생성 요약

    @SerializedName("metadata")
    val metadata: ClipMetadataDto? = null,         // 메타데이터

    @SerializedName("tags")
    val tags: List<String> = emptyList(),          // 자동 추출된 태그

    @SerializedName("error")
    val error: String? = null
)
