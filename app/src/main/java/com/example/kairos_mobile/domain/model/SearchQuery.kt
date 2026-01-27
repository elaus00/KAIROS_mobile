package com.example.kairos_mobile.domain.model

/**
 * 인사이트 검색 쿼리
 *
 * 사용자가 입력한 검색 조건을 담는 데이터 클래스
 */
data class SearchQuery(
    /**
     * 검색할 텍스트 (내용 기반)
     */
    val text: String = "",

    /**
     * 필터링할 인사이트 타입들
     * 비어있으면 모든 타입 포함
     */
    val types: Set<InsightType> = emptySet(),

    /**
     * 필터링할 인사이트 소스들
     * 비어있으면 모든 소스 포함
     */
    val sources: Set<InsightSource> = emptySet(),

    /**
     * 날짜 범위 필터
     * null이면 모든 날짜 포함
     */
    val dateRange: DateRange? = null
)

/**
 * 날짜 범위
 *
 * @property start 시작 시간 (Unix timestamp milliseconds)
 * @property end 종료 시간 (Unix timestamp milliseconds)
 */
data class DateRange(
    val start: Long,
    val end: Long
)
