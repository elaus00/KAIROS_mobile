package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 할 일 도메인 모델
 * Capture가 TODO로 분류될 때 생성되는 파생 엔티티
 */
data class Todo(
    val id: String = UUID.randomUUID().toString(),
    /** FK → Capture.id (1:1) */
    val captureId: String,
    /** 마감 일시 (epoch ms) */
    val deadline: Long? = null,
    /** 완료 여부 */
    val isCompleted: Boolean = false,
    /** 완료 처리 시각 */
    val completedAt: Long? = null,
    /** 리스트 내 정렬 순서 */
    val sortOrder: Int = 0,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 최종 수정 시각 */
    val updatedAt: Long = createdAt
)
