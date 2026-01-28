package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * Todo 메타데이터 DTO (API v2.1)
 * 분류 결과가 todo인 경우 포함됨
 */
data class TodoMetadataDto(
    @SerializedName("due_date")
    val dueDate: String? = null,          // ISO 날짜 (예: "2026-01-30")

    @SerializedName("due_time")
    val dueTime: String? = null,          // ISO 시간 (예: "14:00")

    @SerializedName("priority")
    val priority: String = "none",        // "none", "low", "medium", "high"

    @SerializedName("labels")
    val labels: List<String> = emptyList()
)
