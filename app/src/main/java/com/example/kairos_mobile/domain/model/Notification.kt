package com.example.kairos_mobile.domain.model

/**
 * 알림 도메인 모델
 *
 * 앱 내 알림을 나타내는 모델
 */
data class Notification(
    /**
     * 알림 고유 ID
     */
    val id: String,

    /**
     * 알림 제목
     */
    val title: String,

    /**
     * 알림 메시지 내용
     */
    val message: String,

    /**
     * 알림 생성 시간 (Unix timestamp milliseconds)
     */
    val timestamp: Long,

    /**
     * 읽음 여부
     */
    val isRead: Boolean,

    /**
     * 연관된 캡처 ID
     * 알림 클릭 시 해당 캡처로 이동하기 위해 사용
     */
    val relatedCaptureId: String? = null,

    /**
     * 알림 타입
     */
    val type: NotificationType = NotificationType.CAPTURE_COMPLETE
)

/**
 * 알림 타입
 */
enum class NotificationType {
    /**
     * 캡처 완료
     */
    CAPTURE_COMPLETE,

    /**
     * 동기화 완료
     */
    SYNC_COMPLETE,

    /**
     * 동기화 실패
     */
    SYNC_FAILED,

    /**
     * AI 처리 완료
     */
    AI_PROCESSING_COMPLETE,

    /**
     * 일반 정보
     */
    INFO
}
