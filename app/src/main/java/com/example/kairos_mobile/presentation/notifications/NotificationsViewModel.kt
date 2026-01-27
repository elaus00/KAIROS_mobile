package com.example.kairos_mobile.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.usecase.GetNotificationsUseCase
import com.example.kairos_mobile.domain.usecase.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알림 화면 ViewModel
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationsViewModel"
    }

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * 알림 로드
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getNotificationsUseCase()
                    .collect { notifications ->
                        val unreadCount = notifications.count { !it.isRead }
                        _uiState.update {
                            it.copy(
                                notifications = filterNotifications(notifications, it.selectedFilter),
                                unreadCount = unreadCount,
                                isLoading = false
                            )
                        }
                        Log.d(TAG, "Loaded ${notifications.size} notifications, $unreadCount unread")
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "알림을 불러오는 데 실패했습니다"
                    )
                }
                Log.e(TAG, "Failed to load notifications", e)
            }
        }
    }

    /**
     * 알림 클릭 (읽음 처리)
     */
    fun onNotificationClick(notificationId: String) {
        viewModelScope.launch {
            try {
                markNotificationAsReadUseCase(notificationId)
                Log.d(TAG, "Marked notification as read: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark notification as read", e)
            }
        }
    }

    /**
     * 필터 변경
     */
    fun onFilterChanged(filter: NotificationFilter) {
        _uiState.update { state ->
            // 현재 로드된 전체 알림 리스트를 가져와서 필터링
            val allNotifications = state.notifications
            state.copy(
                selectedFilter = filter
            )
        }
        // 필터가 변경되면 다시 로드
        loadNotifications()
    }

    /**
     * 알림 필터링
     */
    private fun filterNotifications(
        notifications: List<Notification>,
        filter: NotificationFilter
    ): List<Notification> {
        return when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.READ -> notifications.filter { it.isRead }
        }
    }

    /**
     * 새로고침
     */
    fun onRefresh() {
        loadNotifications()
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
