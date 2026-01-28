package com.example.kairos_mobile.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * AI 분류 결과의 Todo 메타데이터
 * destination이 TODO인 경우 포함됨
 */
data class TodoMetadata(
    val dueDate: LocalDate? = null,              // 마감일
    val dueTime: LocalTime? = null,              // 마감 시간
    val priority: TodoPriority = TodoPriority.NONE,  // 우선순위
    val labels: List<String> = emptyList()       // 라벨/태그
)
