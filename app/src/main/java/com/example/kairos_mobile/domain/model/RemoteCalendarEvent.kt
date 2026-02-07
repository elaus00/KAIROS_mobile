package com.example.kairos_mobile.domain.model

data class RemoteCalendarEvent(
    val googleEventId: String,
    val title: String,
    val startTime: Long,
    val endTime: Long?,
    val location: String?,
    val isAllDay: Boolean,
    val source: String
)
