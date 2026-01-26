package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OAuth 콜백 요청 DTO
 * OAuth 인증 완료 후 받은 code를 서버로 전달
 */
data class OAuthCallbackRequest(
    @SerializedName("code")
    val code: String,

    @SerializedName("state")
    val state: String?
)
