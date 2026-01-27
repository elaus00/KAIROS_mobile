package com.example.kairos_mobile.data.remote.dto.insight

import com.google.gson.annotations.SerializedName

/**
 * OCR 응답 DTO
 * 서버의 POST /ocr 엔드포인트 응답
 */
data class OcrResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("text")
    val text: String?,  // 추출된 텍스트

    @SerializedName("error")
    val error: String?  // 에러 메시지
)
