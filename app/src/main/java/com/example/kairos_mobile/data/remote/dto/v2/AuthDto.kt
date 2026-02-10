package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/** Google OAuth 로그인 요청 */
data class AuthGoogleRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("device_id") val deviceId: String
)

/** 인증 응답 (로그인/리프레시 공통) */
data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("user") val user: UserResponse? = null
)

/** 토큰 갱신 요청 */
data class AuthRefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

/** 사용자 정보 응답 */
data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("subscription_tier") val subscriptionTier: String,
    @SerializedName("google_calendar_connected") val googleCalendarConnected: Boolean = false
)
