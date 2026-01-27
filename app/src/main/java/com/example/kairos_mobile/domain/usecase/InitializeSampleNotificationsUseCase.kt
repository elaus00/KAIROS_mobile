package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.model.NotificationType
import com.example.kairos_mobile.domain.repository.NotificationRepository
import java.util.*
import javax.inject.Inject

/**
 * 테스트용 샘플 알림 초기화 UseCase
 */
class InitializeSampleNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    /**
     * 샘플 알림 추가
     */
    suspend operator fun invoke() {
        val now = System.currentTimeMillis()
        val sampleNotifications = listOf(
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.CAPTURE_SAVED,
                title = "캡처 완료",
                message = "아이디어가 성공적으로 저장되었습니다",
                timestamp = now - (2 * 60 * 1000), // 2분 전
                isRead = false,
                relatedCaptureId = null
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.SYNC_COMPLETED,
                title = "동기화 완료",
                message = "Obsidian에 3개 노트가 동기화되었습니다",
                timestamp = now - (60 * 60 * 1000), // 1시간 전
                isRead = false,
                relatedCaptureId = null
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.AI_PROCESSING,
                title = "AI 분석 완료",
                message = "5개의 캡처가 자동 분류되었습니다",
                timestamp = now - (3 * 60 * 60 * 1000), // 3시간 전
                isRead = true,
                relatedCaptureId = null
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.REMINDER,
                title = "미팅 알림",
                message = "30분 후 팀 회의가 예정되어 있습니다",
                timestamp = now - (5 * 60 * 60 * 1000), // 5시간 전
                isRead = true,
                relatedCaptureId = null
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.SYNC_FAILED,
                title = "동기화 실패",
                message = "네트워크 오류로 동기화에 실패했습니다",
                timestamp = now - (24 * 60 * 60 * 1000), // 1일 전
                isRead = true,
                relatedCaptureId = null
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.SYSTEM,
                title = "시스템 업데이트",
                message = "KAIROS 앱이 최신 버전으로 업데이트되었습니다",
                timestamp = now - (2 * 24 * 60 * 60 * 1000), // 2일 전
                isRead = true,
                relatedCaptureId = null
            )
        )

        // 샘플 알림 추가
        sampleNotifications.forEach { notification ->
            notificationRepository.addNotification(notification)
        }
    }
}
