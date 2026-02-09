package com.example.kairos_mobile.domain.model

/** 시맨틱 검색 결과 */
data class SemanticSearchResult(
    val captureId: String,
    val score: Double,
    val snippet: String
)
