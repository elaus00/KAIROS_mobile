package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 캘린더 이벤트 생성 요청 DTO
 */
data class CalendarEventRequest(
    @SerializedName("title") val title: String,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("end_time") val endTime: Long?,
    @SerializedName("location") val location: String?,
    @SerializedName("is_all_day") val isAllDay: Boolean = false
)

/**
 * 캘린더 이벤트 응답 DTO
 */
data class CalendarEventResponse(
    @SerializedName("google_event_id") val googleEventId: String,
    @SerializedName("status") val status: String
)
