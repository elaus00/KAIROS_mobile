package com.example.kairos_mobile.presentation.search

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType

/**
 * 검색 화면 UI 상태
 * FTS 기반 전체 캡처 검색 + 필터
 */
data class SearchUiState(
    /** 검색 텍스트 */
    val searchText: String = "",
    /** 검색 결과 */
    val searchResults: List<Capture> = emptyList(),
    /** 검색 수행 여부 */
    val hasSearched: Boolean = false,
    /** 에러 메시지 */
    val errorMessage: String? = null,
    /** 분류 유형 필터 (null = 전체) */
    val selectedType: ClassifiedType? = null,
    /** 날짜 범위 시작 (epoch ms, null = 제한 없음) */
    val startDate: Long? = null,
    /** 날짜 범위 끝 (epoch ms, null = 제한 없음) */
    val endDate: Long? = null
)
