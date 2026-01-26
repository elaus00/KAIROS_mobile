package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OAuth 인증 URL 응답 DTO
 * 서버에서 생성한 OAuth 2.0 인증 URL을 반환
 */
data class AuthUrlResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("authUrl")
    val authUrl: String?,

    @SerializedName("error")
    val error: String?
)
