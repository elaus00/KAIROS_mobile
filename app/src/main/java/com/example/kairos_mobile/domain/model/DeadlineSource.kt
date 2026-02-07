package com.example.kairos_mobile.domain.model

/**
 * 마감일 소스
 * AI가 추출했는지, 사용자가 직접 설정했는지 구분
 */
enum class DeadlineSource {
    AI_EXTRACTED,
    AI_SUGGESTED,
    USER
}
