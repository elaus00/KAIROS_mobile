package com.example.kairos_mobile.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정 도메인 모델 (PRD v4.0)
 */
data class Schedule(
    val id: String,
    val title: String,
    val time: LocalTime,
    val date: LocalDate,
    val location: String? = null,
    val category: ScheduleCategory = ScheduleCategory.PERSONAL,
    val googleCalendarId: String? = null,
    val sourceCaptureId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt
) {
    /**
     * 오늘 일정인지 확인
     */
    fun isToday(): Boolean {
        return date == LocalDate.now()
    }

    /**
     * 내일 일정인지 확인
     */
    fun isTomorrow(): Boolean {
        return date == LocalDate.now().plusDays(1)
    }

    /**
     * 이번 주 일정인지 확인
     */
    fun isThisWeek(): Boolean {
        val today = LocalDate.now()
        val endOfWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
        return date in today..endOfWeek
    }

    /**
     * 일정이 지났는지 확인
     */
    fun isPast(): Boolean {
        val now = LocalDate.now()
        val currentTime = LocalTime.now()
        return date.isBefore(now) || (date == now && time.isBefore(currentTime))
    }

    /**
     * 시간 포맷 (예: "10:00")
     */
    fun getFormattedTime(): String {
        return String.format("%02d:%02d", time.hour, time.minute)
    }

    /**
     * Google Calendar 연동 여부
     */
    fun isSyncedWithGoogle(): Boolean {
        return googleCalendarId != null
    }
}

/**
 * 일정 카테고리
 */
enum class ScheduleCategory {
    WORK,       // 업무
    PERSONAL;   // 개인

    fun getDisplayName(): String {
        return when (this) {
            WORK -> "업무"
            PERSONAL -> "개인"
        }
    }
}
