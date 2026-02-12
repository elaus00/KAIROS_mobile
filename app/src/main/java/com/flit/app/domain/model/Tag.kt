package com.flit.app.domain.model

import java.util.UUID

/**
 * 태그 도메인 모델
 */
data class Tag(
    val id: String = UUID.randomUUID().toString(),
    /** 태그명 (고유) */
    val name: String,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis()
)
