package com.example.kairos_mobile.data.remote.dto

/**
 * AI 분류 응답 DTO
 */
data class ClassificationResponse(
    val type: String,                       // CaptureType enum name
    val destinationPath: String,            // 목적지 경로
    val title: String,                      // 자동 생성된 제목
    val tags: List<String>,                 // 추출된 태그
    val confidence: Float,                  // 신뢰도 (0.0 ~ 1.0)
    val metadata: Map<String, String>? = null  // 추가 메타데이터
)
