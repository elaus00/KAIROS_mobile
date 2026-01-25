package com.example.kairos_mobile.data.remote.dto

/**
 * Obsidian 노트 생성 응답 DTO
 */
data class ObsidianCreateResponse(
    val success: Boolean,                   // 성공 여부
    val filePath: String?,                  // 생성된 파일 경로
    val message: String?                    // 메시지 또는 에러 메시지
)
