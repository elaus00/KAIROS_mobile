package com.example.kairos_mobile.presentation.search

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightSource
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.domain.model.DateRange

/**
 * Search 화면 UI 상태
 */
data class SearchUiState(
    /**
     * 검색 텍스트
     */
    val searchText: String = "",

    /**
     * 선택된 타입 필터
     */
    val selectedTypes: Set<InsightType> = emptySet(),

    /**
     * 선택된 소스 필터
     */
    val selectedSources: Set<InsightSource> = emptySet(),

    /**
     * 날짜 범위 필터
     */
    val dateRange: DateRange? = null,

    /**
     * 검색 결과 리스트
     */
    val searchResults: List<Insight> = emptyList(),

    /**
     * 로딩 중 여부
     */
    val isLoading: Boolean = false,

    /**
     * 에러 메시지
     */
    val errorMessage: String? = null,

    /**
     * 검색 수행 여부 (검색 버튼 클릭 여부)
     */
    val hasSearched: Boolean = false,

    /**
     * 더 불러올 항목이 있는지 여부
     */
    val hasMore: Boolean = true,

    /**
     * 현재 페이지 오프셋
     */
    val currentOffset: Int = 0,

    /**
     * 페이지 크기
     */
    val pageSize: Int = 20
)
