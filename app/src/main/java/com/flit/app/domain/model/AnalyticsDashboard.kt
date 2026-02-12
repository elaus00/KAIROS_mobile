package com.flit.app.domain.model

/** 분석 대시보드 데이터 */
data class AnalyticsDashboard(
    val totalCaptures: Int,
    val capturesByType: Map<String, Int>,
    val capturesByDay: Map<String, Int>,
    val avgClassificationTimeMs: Long,
    val topTags: List<Pair<String, Int>>
)
