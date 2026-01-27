package com.example.kairos_mobile.presentation.notifications

import com.example.kairos_mobile.domain.model.Notification

/**
 * 알림 화면 UI 상태
 */
data class NotificationsUiState(
    /**
     * 알림 리스트
     */
    val notifications: List<Notification> = emptyList(),

    /**
     * 읽지 않은 알림 개수
     */
    val unreadCount: Int = 0,

    /**
     * 로딩 중 여부
     */
    val isLoading: Boolean = false,

    /**
     * 에러 메시지
     */
    val errorMessage: String? = null,

    /**
     * 선택된 필터 (ALL, UNREAD, READ)
     */
    val selectedFilter: NotificationFilter = NotificationFilter.ALL
)

/**
 * 알림 필터 타입
 */
enum class NotificationFilter {
    ALL,      // 전체
    UNREAD,   // 읽지 않음
    READ      // 읽음
}
