package com.example.kairos_mobile.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.usecase.notifications.GetNotificationsUseCase
import com.example.kairos_mobile.domain.usecase.notifications.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    // 중복 구독 방지를 위한 Job 관리
    private var loadJob: Job? = null

    // 원본 알림 리스트 (필터링 전)
    private var allNotifications: List<Notification> = emptyList()

    init {
        loadNotifications()
    }

    /**
     * 알림 로드
     * 이전 로드 작업이 있으면 취소하고 새로 시작
     */
    private fun loadNotifications() {
        // 이전 Job 취소
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getNotificationsUseCase()
                    .collect { notifications ->
                        allNotifications = notifications
                        val unreadCount = notifications.count { !it.isRead }
                        _uiState.update { state ->
                            state.copy(
                                notifications = filterNotifications(notifications, state.selectedFilter),
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
     * 로컬 캐시된 알림을 필터링하므로 새로운 Flow 구독 불필요
     */
    fun onFilterChanged(filter: NotificationFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                notifications = filterNotifications(allNotifications, filter)
            )
        }
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
