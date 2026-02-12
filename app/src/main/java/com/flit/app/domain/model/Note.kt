package com.flit.app.domain.model

import java.util.UUID

/**
 * 노트 도메인 모델
 * Capture가 NOTES로 분류될 때 생성되는 파생 엔티티
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    /** FK → Capture.id (1:1) */
    val captureId: String,
    /** FK → Folder.id */
    val folderId: String? = null,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 최종 수정 시각 */
    val updatedAt: Long = createdAt,
    /** 노트 본문 (사용자 편집 가능) */
    val body: String? = null
)
