package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 캡처 도메인 모델
 * 모든 기록의 원본. 시스템의 중심 엔티티.
 */
data class Capture(
    val id: String = UUID.randomUUID().toString(),
    /** 사용자가 입력한 원본 텍스트 */
    val originalText: String,
    /** AI 생성 요약 제목 (최대 30자) */
    val aiTitle: String? = null,
    /** 분류 유형 */
    val classifiedType: ClassifiedType = ClassifiedType.TEMP,
    /** 노트 서브 분류 (classifiedType=NOTES일 때만 사용) */
    val noteSubType: NoteSubType? = null,
    /** AI 분류 신뢰도 */
    val confidence: ConfidenceLevel? = null,
    /** 캡처 입력 소스 */
    val source: CaptureSource = CaptureSource.APP,
    /** AI 분류 확인 여부 */
    val isConfirmed: Boolean = false,
    /** AI 분류 확인 시각 */
    val confirmedAt: Long? = null,
    /** 소프트 삭제 여부 */
    val isDeleted: Boolean = false,
    /** 소프트 삭제 시점 */
    val deletedAt: Long? = null,
    /** 임시 저장 텍스트 */
    val draftText: String? = null,
    /** 생성 시각 (epoch ms) */
    val createdAt: Long = System.currentTimeMillis(),
    /** 최종 수정 시각 */
    val updatedAt: Long = createdAt,
    /** AI 분류 완료 시각 */
    val classificationCompletedAt: Long? = null,
    /** 휴지통 여부 */
    val isTrashed: Boolean = false,
    /** 휴지통 이동 시점 */
    val trashedAt: Long? = null,
    /** 이미지 URI */
    val imageUri: String? = null,
    /** 부모 캡처 ID (멀티 인텐트 분할 시 원본 참조) */
    val parentCaptureId: String? = null
)
