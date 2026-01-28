package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * STT(Speech-to-Text) 요청 DTO (API v2.1)
 */
data class SttRequest(
    @SerializedName("audio_data")
    val audioData: String,                         // Base64 인코딩된 오디오 데이터

    @SerializedName("audio_type")
    val audioType: String = "wav",                 // "wav", "mp3", "m4a", "webm"

    @SerializedName("language")
    val language: String? = null                   // 언어 코드 (예: "ko", "en")
)
