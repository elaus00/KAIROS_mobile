package com.example.kairos_mobile.presentation.history

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType

/**
 * 전체 기록 화면 UI 상태
 */
data class HistoryUiState(
    /** 캡처 목록 (역시간순) */
    val captures: List<Capture> = emptyList(),
    /** 로딩 중 여부 (초기 로드) */
    val isLoading: Boolean = true,
    /** 추가 페이지 로딩 중 */
    val isLoadingMore: Boolean = false,
    /** 더 불러올 데이터 존재 여부 */
    val hasMore: Boolean = true,
    /** 에러 메시지 */
    val errorMessage: String? = null,
    /** 선택된 분류 유형 필터 (null = 전체) */
    val selectedType: ClassifiedType? = null,
    /** 날짜 범위 필터 시작 (epoch ms) */
    val startDate: Long? = null,
    /** 날짜 범위 필터 끝 (epoch ms) */
    val endDate: Long? = null
)

/**
 * 전체 기록 화면 이벤트
 */
sealed class HistoryEvent {
    /** 삭제 완료 (Snackbar 실행 취소용) */
    data class DeleteSuccess(val captureId: String) : HistoryEvent()
    /** 실행 취소 완료 */
    data object UndoSuccess : HistoryEvent()
}
