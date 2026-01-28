package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * OCR 요청 DTO (API v2.1)
 */
data class OcrRequest(
    @SerializedName("image_data")
    val imageData: String,                         // Base64 인코딩된 이미지 데이터

    @SerializedName("image_type")
    val imageType: String = "jpeg",                // "jpeg", "png", "webp"

    @SerializedName("language_hint")
    val languageHint: String? = null               // 언어 힌트 (예: "ko", "en")
)
