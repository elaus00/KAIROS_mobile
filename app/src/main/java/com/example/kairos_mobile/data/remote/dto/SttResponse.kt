package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * STT(Speech-to-Text) 응답 DTO
 * 서버의 POST /stt 엔드포인트 응답
 */
data class SttResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("text")
    val text: String?,  // 음성에서 추출된 텍스트

    @SerializedName("confidence")
    val confidence: Float?,  // 인식 신뢰도 (0.0 ~ 1.0)

    @SerializedName("language")
    val language: String?,  // 감지된 언어 코드 (예: "ko", "en")

    @SerializedName("error")
    val error: String?  // 에러 메시지
)
