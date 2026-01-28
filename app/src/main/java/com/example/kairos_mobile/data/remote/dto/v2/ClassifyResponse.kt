package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * AI 분류 응답 DTO (API v2.1)
 */
data class ClassifyResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("type")
    val type: String,                              // "todo", "idea", "note", "quick_note", "clip"

    @SerializedName("destination")
    val destination: String,                       // "todo", "obsidian"

    @SerializedName("confidence")
    val confidence: Float,                         // 0.0 ~ 1.0

    @SerializedName("reasoning")
    val reasoning: String? = null,                 // AI 분류 근거

    @SerializedName("title")
    val title: String,                             // 자동 생성된 제목

    @SerializedName("tags")
    val tags: List<String> = emptyList(),          // 추출된 태그

    @SerializedName("suggested_filename")
    val suggestedFilename: String? = null,         // 제안 파일명 (Obsidian용)

    @SerializedName("suggested_path")
    val suggestedPath: String? = null,             // 제안 경로 (Obsidian용)

    @SerializedName("todo_metadata")
    val todoMetadata: TodoMetadataDto? = null      // Todo 메타데이터 (destination이 todo인 경우)
)
