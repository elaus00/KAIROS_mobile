package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * STT(Speech-to-Text) 응답 DTO (API v2.1)
 */
data class SttResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("text")
    val text: String? = null,                      // 인식된 텍스트

    @SerializedName("confidence")
    val confidence: Float? = null,                 // 신뢰도 (0.0 ~ 1.0)

    @SerializedName("duration_seconds")
    val durationSeconds: Float? = null,            // 오디오 길이 (초)

    @SerializedName("error")
    val error: String? = null
)
