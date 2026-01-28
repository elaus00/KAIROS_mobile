package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 웹 클립 메타데이터 DTO (API v2.1)
 */
data class ClipMetadataDto(
    @SerializedName("author")
    val author: String? = null,

    @SerializedName("published_date")
    val publishedDate: String? = null,

    @SerializedName("site_name")
    val siteName: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("word_count")
    val wordCount: Int? = null
)
