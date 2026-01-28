package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * Obsidian 노트 생성 요청 DTO (API v2.1)
 */
data class NoteCreateRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("type")
    val type: String,                              // "idea", "note", "quick_note", "clip"

    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("folder")
    val folder: String? = null,                    // 저장할 폴더 경로

    @SerializedName("source")
    val source: String? = null,                    // "text", "image", "audio", "url"

    @SerializedName("url")
    val url: String? = null                        // 웹 클립인 경우 원본 URL
)
