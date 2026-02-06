package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * AI 분류 요청 DTO
 */
data class ClassifyRequest(
    @SerializedName("text")
    val text: String
)
