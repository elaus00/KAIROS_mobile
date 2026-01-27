package com.example.kairos_mobile.data.remote.dto.ai

import com.google.gson.annotations.SerializedName

/**
 * 태그 제안 요청 DTO
 * 서버의 POST /tags/suggest 엔드포인트 요청
 */
data class TagSuggestRequest(
    @SerializedName("content")
    val content: String,

    @SerializedName("classification")
    val classification: String?,  // 분류 타입 (SCHEDULE, TODO, IDEA 등)

    @SerializedName("limit")
    val limit: Int = 5  // 최대 제안 태그 수
)
