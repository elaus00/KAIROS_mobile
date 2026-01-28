package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * Obsidian 노트 생성 응답 DTO (API v2.1)
 */
data class NoteCreateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("note_id")
    val noteId: String? = null,

    @SerializedName("path")
    val path: String? = null,                      // 생성된 노트의 경로

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,                 // ISO 타임스탬프

    @SerializedName("error")
    val error: String? = null
)
