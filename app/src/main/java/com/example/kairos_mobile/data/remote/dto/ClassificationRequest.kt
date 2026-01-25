package com.example.kairos_mobile.data.remote.dto

/**
 * AI 분류 요청 DTO
 */
data class ClassificationRequest(
    val content: String,                    // 분류할 컨텐츠
    val userId: String = "default_user",    // Phase 1: 하드코딩
    val context: Map<String, String>? = null  // 추가 컨텍스트 (선택)
)
