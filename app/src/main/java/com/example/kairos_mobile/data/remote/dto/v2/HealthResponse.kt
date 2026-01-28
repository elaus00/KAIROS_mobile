package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 헬스체크 응답 DTO (API v2.1)
 */
data class HealthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("status")
    val status: String,                            // "healthy", "degraded", "unhealthy"

    @SerializedName("version")
    val version: String? = null,                   // API 버전

    @SerializedName("environment")
    val environment: String? = null,               // "development", "production"

    @SerializedName("timestamp")
    val timestamp: String? = null                  // ISO 타임스탬프
)
