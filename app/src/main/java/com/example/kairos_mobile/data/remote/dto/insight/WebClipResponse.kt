package com.example.kairos_mobile.data.remote.dto.insight

import com.google.gson.annotations.SerializedName

/**
 * 웹 클립 응답 DTO
 * 서버의 POST /webclip 엔드포인트 응답
 */
data class WebClipResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("title")
    val title: String?,  // 페이지 제목

    @SerializedName("description")
    val description: String?,  // 페이지 설명/요약

    @SerializedName("imageUrl")
    val imageUrl: String?,  // 대표 이미지 URL

    @SerializedName("content")
    val content: String?,  // AI 요약 콘텐츠

    @SerializedName("error")
    val error: String?  // 에러 메시지
)
