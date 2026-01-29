package com.example.kairos_mobile.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 투두 도메인 모델
 */
data class Todo(
    val id: String,
    val content: String,
    val title: String? = null,
    val sourceCaptureId: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TodoPriority = TodoPriority.NONE,
    val labels: List<String> = emptyList(),
    val manualOrder: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * 오늘 마감인지 확인
     */
    fun isDueToday(): Boolean {
        return dueDate == LocalDate.now()
    }

    /**
     * 내일 마감인지 확인
     */
    fun isDueTomorrow(): Boolean {
        return dueDate == LocalDate.now().plusDays(1)
    }

    /**
     * 이번 주 마감인지 확인
     */
    fun isDueThisWeek(): Boolean {
        val today = LocalDate.now()
        val endOfWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
        return dueDate != null && dueDate in today..endOfWeek
    }

    /**
     * 다음 주 마감인지 확인
     */
    fun isDueNextWeek(): Boolean {
        val today = LocalDate.now()
        val startOfNextWeek = today.plusDays((8 - today.dayOfWeek.value).toLong())
        val endOfNextWeek = startOfNextWeek.plusDays(6)
        return dueDate != null && dueDate in startOfNextWeek..endOfNextWeek
    }

    /**
     * 마감일이 지났는지 확인
     */
    fun isOverdue(): Boolean {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted
    }

    /**
     * 마감일 그룹 키 반환
     */
    fun getDueDateGroupKey(): String {
        return when {
            dueDate == null -> "나중에"
            isDueToday() -> "오늘"
            isDueTomorrow() -> "내일"
            isOverdue() -> "지연됨"
            isDueThisWeek() -> "이번 주"
            isDueNextWeek() -> "다음 주"
            else -> "나중에"
        }
    }

    companion object {
        /**
         * 마감일 그룹 정렬 순서
         */
        val GROUP_ORDER = listOf(
            "지연됨",
            "오늘",
            "내일",
            "이번 주",
            "다음 주",
            "나중에"
        )
    }
}
