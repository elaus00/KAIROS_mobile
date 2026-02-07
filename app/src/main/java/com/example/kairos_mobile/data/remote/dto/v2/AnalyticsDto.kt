package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 분석 이벤트 DTO
 */
data class AnalyticsEventDto(
    @SerializedName("event_type") val eventType: String,
    @SerializedName("event_data") val eventData: String? = null,
    @SerializedName("timestamp") val timestamp: Long
)
