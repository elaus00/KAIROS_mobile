package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Todo Room Entity
 * 앱 내 투두리스트 항목 저장
 */
@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["due_date"]),
        Index(value = ["is_completed"])
    ]
)
data class TodoEntity(
    @PrimaryKey
    val id: String,

    // 투두 내용
    val content: String,

    // 제목 (선택)
    val title: String? = null,

    // 원본 캡처 ID (캡처에서 생성된 경우)
    @ColumnInfo(name = "source_capture_id")
    val sourceCaptureId: String? = null,

    // 마감일 (epoch millis, 선택)
    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    // 마감 시간 (HH:mm 형식, 선택)
    @ColumnInfo(name = "due_time")
    val dueTime: String? = null,

    // 우선순위: 0=없음, 1=낮음, 2=중간, 3=높음
    val priority: Int = 0,

    // 라벨 (JSON 배열 문자열)
    val labels: String? = null,

    // 수동 정렬 순서
    @ColumnInfo(name = "manual_order")
    val manualOrder: Int = 0,

    // 완료 여부
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    // 완료 시간 (epoch millis)
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    // 생성 시간
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 수정 시간
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
