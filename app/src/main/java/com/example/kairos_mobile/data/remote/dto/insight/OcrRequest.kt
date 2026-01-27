package com.example.kairos_mobile.data.remote.dto.insight

import okhttp3.MultipartBody

/**
 * OCR 요청 DTO
 * 서버의 POST /ocr 엔드포인트용
 */
data class OcrRequest(
    val image: MultipartBody.Part  // Multipart 이미지 파일
)
