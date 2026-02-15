package com.flit.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flit.app.MainActivity
import com.flit.app.R
import com.flit.app.domain.usecase.calendar.CalendarNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 발행 헬퍼
 * 채널 생성 및 로컬 푸시 알림 발행
 */
@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CalendarNotifier {
    companion object {
        /** 일정 관련 알림 채널 */
        const val CHANNEL_CALENDAR = "flit_calendar"

        /** 일정 제안 푸시 알림 채널 */
        const val CHANNEL_SCHEDULE_SUGGESTION = "flit_schedule_suggestion"

        /** 일반 푸시 알림 채널 */
        const val CHANNEL_GENERAL = "flit_general"

        private var notificationId = 100
    }

    /**
     * 알림 채널 초기화 (앱 시작 시 호출)
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // 캘린더 알림 채널
            val calendarChannel = NotificationChannel(
                CHANNEL_CALENDAR,
                "일정 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "일정 추가 확인 및 제안 알림"
            }

            // 일정 제안 푸시 알림 채널
            val scheduleSuggestionChannel = NotificationChannel(
                CHANNEL_SCHEDULE_SUGGESTION,
                "일정 제안",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "서버에서 감지한 일정 추가/제안 푸시 알림"
            }

            // 일반 푸시 알림 채널
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "일반 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "일반 푸시 알림"
            }

            manager.createNotificationChannel(calendarChannel)
            manager.createNotificationChannel(scheduleSuggestionChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    /**
     * 캘린더 동기화 알림 발행
     */
    fun showCalendarSyncNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CALENDAR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId++, notification)
    }

    /**
     * 일정 제안 알림 발행
     */
    fun showCalendarSuggestionNotification(scheduleTitle: String) {
        showCalendarSyncNotification(
            title = "일정 추가 제안",
            message = "\"$scheduleTitle\" 일정을 캘린더에 추가하시겠습니까?"
        )
    }

    /**
     * 일정 자동 추가 완료 알림
     */
    fun showCalendarAutoSyncNotification(scheduleTitle: String) {
        showCalendarSyncNotification(
            title = "일정 추가 완료",
            message = "\"$scheduleTitle\" 일정이 캘린더에 자동 추가되었습니다."
        )
    }

    // CalendarNotifier 구현
    override fun notifySuggestion(scheduleTitle: String) {
        showCalendarSuggestionNotification(scheduleTitle)
    }

    override fun notifyAutoSync(scheduleTitle: String) {
        showCalendarAutoSyncNotification(scheduleTitle)
    }

    /**
     * FCM 푸시 알림 표시
     * 서버에서 수신한 메시지 데이터를 기반으로 알림을 빌드한다.
     */
    fun showFcmNotification(title: String, body: String, channelId: String) {
        val resolvedChannel = when (channelId) {
            CHANNEL_SCHEDULE_SUGGESTION, CHANNEL_GENERAL, CHANNEL_CALENDAR -> channelId
            else -> CHANNEL_GENERAL
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, resolvedChannel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId++, notification)
    }
}
