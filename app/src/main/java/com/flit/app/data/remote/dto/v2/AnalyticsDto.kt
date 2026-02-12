package com.flit.app.data.remote.dto.v2

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

/**
 * 분석 이벤트 DTO
 */
data class AnalyticsEventDto(
    @SerializedName("event_type") val eventType: String,
    @SerializedName("event_data") val eventData: JsonObject = JsonObject(),
    @SerializedName("timestamp") val timestamp: String
)

data class AnalyticsEventsRequest(
    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("events")
    val events: List<AnalyticsEventDto>
)

data class AnalyticsEventsResponse(
    @SerializedName("received")
    val received: Int
)
