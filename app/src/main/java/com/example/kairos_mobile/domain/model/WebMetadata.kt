package com.example.kairos_mobile.domain.model

/**
 * 웹 클립 메타데이터
 * M08: 웹 페이지에서 추출한 메타데이터
 */
data class WebMetadata(
    /**
     * 원본 URL
     */
    val url: String,

    /**
     * 웹 페이지 제목 (og:title 또는 <title>)
     */
    val title: String?,

    /**
     * 웹 페이지 설명 (og:description)
     */
    val description: String?,

    /**
     * 대표 이미지 URL (og:image)
     */
    val imageUrl: String?
)
