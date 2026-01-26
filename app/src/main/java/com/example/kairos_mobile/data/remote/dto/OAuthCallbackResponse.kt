package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * OAuth 콜백 응답 DTO
 * 서버가 토큰 교환 후 반환하는 결과
 */
data class OAuthCallbackResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("error")
    val error: String?
)
