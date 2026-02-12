package com.flit.app.presentation.search

import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.SemanticSearchResult

/**
 * 검색 화면 UI 상태
 * FTS 기반 전체 캡처 검색 + 필터 + AI 시맨틱 검색
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
    /** AI 시맨틱 검색 모드 */
    val isSemanticMode: Boolean = false,
    /** 시맨틱 검색 결과 */
    val semanticResults: List<SemanticSearchResult> = emptyList(),
    /** 시맨틱 검색 로딩 */
    val isSemanticLoading: Boolean = false,
    /** 프리미엄 구독 여부 (AI 시맨틱 검색 토글 표시 제어) */
    val isPremium: Boolean = false
)
