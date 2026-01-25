package com.example.kairos_mobile.data.remote.dto

/**
 * Obsidian 노트 생성 요청 DTO
 */
data class ObsidianCreateRequest(
    val path: String,                       // 저장 경로
    val title: String,                      // 노트 제목
    val content: String,                    // 노트 내용
    val tags: List<String>,                 // 태그 목록
    val metadata: Map<String, String>? = null  // 추가 메타데이터
)
