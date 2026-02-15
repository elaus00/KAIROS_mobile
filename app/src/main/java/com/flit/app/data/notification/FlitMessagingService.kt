package com.flit.app.data.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM 메시지 수신 서비스
 * 서버에서 발송한 푸시 알림을 처리한다.
 */
@AndroidEntryPoint
class FlitMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    /**
     * FCM 토큰 갱신 시 호출
     * 갱신된 토큰을 로컬에 저장한다.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM 토큰 갱신: $token")
        fcmTokenManager.saveToken(token)
    }

    /**
     * FCM 메시지 수신 시 호출
     * data payload를 파싱하여 알림을 표시한다.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM 메시지 수신: ${message.messageId}")

        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: return
        val body = data["body"] ?: message.notification?.body ?: ""
        val channel = data["channel"] ?: NotificationHelper.CHANNEL_GENERAL

        notificationHelper.showFcmNotification(title, body, channel)
    }

    companion object {
        private const val TAG = "FlitMessagingService"
    }
}
