package com.flit.app.domain.model

import java.util.UUID

/**
 * 분류 수정 로그 도메인 모델
 */
data class ClassificationLog(
    val id: String = UUID.randomUUID().toString(),
    /** 캡처 ID */
    val captureId: String,
    /** 변경 전 분류 유형 */
    val originalType: ClassifiedType,
    /** 변경 전 서브 유형 */
    val originalSubType: NoteSubType? = null,
    /** 변경 후 분류 유형 */
    val newType: ClassifiedType,
    /** 변경 후 서브 유형 */
    val newSubType: NoteSubType? = null,
    /** AI 분류 후 수정까지 걸린 시간 (ms) */
    val timeSinceClassificationMs: Long? = null,
    /** 수정 시각 */
    val modifiedAt: Long = System.currentTimeMillis()
)
