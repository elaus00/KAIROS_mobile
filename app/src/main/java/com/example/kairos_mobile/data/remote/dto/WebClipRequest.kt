package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 웹 클립 요청 DTO
 * 서버의 POST /webclip 엔드포인트용
 */
data class WebClipRequest(
    @SerializedName("url")
    val url: String  // 크롤링할 URL
)
