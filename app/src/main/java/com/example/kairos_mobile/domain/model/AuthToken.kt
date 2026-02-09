package com.example.kairos_mobile.domain.model

/** 인증 토큰 */
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)
