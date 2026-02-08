package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 캡처 Room Entity
 * 모든 기록의 원본. 시스템의 중심 엔티티.
 */
@Entity(
    tableName = "captures",
    indices = [
        Index(value = ["classified_type"]),
        Index(value = ["note_sub_type"]),
        Index(value = ["created_at"]),
        Index(value = ["is_deleted"]),
        Index(value = ["is_confirmed"]),
        Index(value = ["is_trashed"]),
        Index(value = ["parent_capture_id"])
    ]
)
data class CaptureEntity(
    @PrimaryKey
    val id: String,

    // 사용자가 입력한 원본 텍스트
    @ColumnInfo(name = "original_text")
    val originalText: String,

    // AI 생성 요약 제목 (최대 30자)
    @ColumnInfo(name = "ai_title")
    val aiTitle: String? = null,

    // 분류 유형: SCHEDULE, TODO, NOTES, TEMP
    @ColumnInfo(name = "classified_type")
    val classifiedType: String,

    // 노트 서브타입: INBOX, IDEA, BOOKMARK, USER_FOLDER (classified_type=NOTES일 때만)
    @ColumnInfo(name = "note_sub_type")
    val noteSubType: String? = null,

    // AI 분류 신뢰도: HIGH, MEDIUM, LOW
    val confidence: String? = null,

    // 캡처 소스: APP, SHARE_INTENT, WIDGET, SPLIT
    val source: String,

    // AI 분류 확인 여부
    @ColumnInfo(name = "is_confirmed", defaultValue = "0")
    val isConfirmed: Boolean = false,

    // AI 분류 확인 시각
    @ColumnInfo(name = "confirmed_at")
    val confirmedAt: Long? = null,

    // 소프트 삭제 (Snackbar 실행 취소용)
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,

    // 소프트 삭제 시점
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 최종 수정 시각
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    // AI 분류 완료 시각
    @ColumnInfo(name = "classification_completed_at")
    val classificationCompletedAt: Long? = null,

    // 휴지통 여부 (30일 보존)
    @ColumnInfo(name = "is_trashed", defaultValue = "0")
    val isTrashed: Boolean = false,

    // 휴지통 이동 시점
    @ColumnInfo(name = "trashed_at")
    val trashedAt: Long? = null,

    // 이미지 URI
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    // 부모 캡처 ID (멀티 인텐트 분할 시 원본 참조)
    @ColumnInfo(name = "parent_capture_id")
    val parentCaptureId: String? = null
)
