package com.example.kairos_mobile.domain.model

/** 캘린더 충돌 정보 */
data class CalendarConflict(
    val scheduleId: String,
    val googleEventId: String,
    val localTitle: String,
    val googleTitle: String,
    val localStartTime: Long,
    val googleStartTime: Long,
    val localEndTime: Long?,
    val googleEndTime: Long?,
    val localLocation: String?,
    val googleLocation: String?
)
