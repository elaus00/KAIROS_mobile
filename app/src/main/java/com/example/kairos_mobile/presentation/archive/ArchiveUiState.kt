package com.example.kairos_mobile.presentation.archive

import com.example.kairos_mobile.domain.model.Capture

/**
 * Archive 화면 UI 상태
 */
data class ArchiveUiState(
    /**
     * 날짜별로 그룹화된 캡처들
     * 키: "Today", "Yesterday", "This Week", "2026-01-25" 등
     * 값: 해당 날짜의 캡처 리스트
     */
    val groupedCaptures: Map<String, List<Capture>> = emptyMap(),

    /**
     * 확장된 캡처 ID 집합
     * 확장/축소 상태 관리
     */
    val expandedCaptureIds: Set<String> = emptySet(),

    /**
     * 로딩 중 여부
     */
    val isLoading: Boolean = false,

    /**
     * 에러 메시지
     */
    val errorMessage: String? = null,

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
    val pageSize: Int = 50
)
