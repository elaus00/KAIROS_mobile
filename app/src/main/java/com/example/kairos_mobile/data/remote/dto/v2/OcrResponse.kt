package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * OCR 응답 DTO (API v2.1)
 */
data class OcrResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("text")
    val text: String? = null,                      // 추출된 텍스트

    @SerializedName("confidence")
    val confidence: Float? = null,                 // 신뢰도 (0.0 ~ 1.0)

    @SerializedName("language")
    val language: String? = null,                  // 감지된 언어

    @SerializedName("word_count")
    val wordCount: Int? = null,                    // 단어 수

    @SerializedName("error")
    val error: String? = null
)
