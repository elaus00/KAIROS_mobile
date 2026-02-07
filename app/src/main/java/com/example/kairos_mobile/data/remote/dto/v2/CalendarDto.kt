package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 캘린더 이벤트 생성 요청 DTO
 */
data class CalendarEventRequest(
    @SerializedName("capture_id") val captureId: String,
    @SerializedName("title") val title: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("is_all_day") val isAllDay: Boolean = false
)

/**
 * 캘린더 이벤트 응답 DTO
 */
data class CalendarEventResponse(
    @SerializedName("google_event_id") val googleEventId: String,
    @SerializedName("html_link") val htmlLink: String? = null
)

data class CalendarEventDeleteResponse(
    @SerializedName("deleted")
    val deleted: Boolean
)

data class CalendarTokenExchangeRequest(
    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("redirect_uri")
    val redirectUri: String
)

data class CalendarTokenRequest(
    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("expires_in")
    val expiresIn: Long?
)

data class CalendarTokenResponse(
    @SerializedName("connected")
    val connected: Boolean
)

data class CalendarEventsResponse(
    @SerializedName("events")
    val events: List<CalendarEventItemDto> = emptyList()
)

data class CalendarEventItemDto(
    @SerializedName("google_event_id")
    val googleEventId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("is_all_day")
    val isAllDay: Boolean,

    @SerializedName("source")
    val source: String
)
