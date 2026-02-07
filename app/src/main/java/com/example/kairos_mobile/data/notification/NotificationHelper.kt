package com.example.kairos_mobile.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kairos_mobile.MainActivity
import com.example.kairos_mobile.R
import com.example.kairos_mobile.domain.usecase.calendar.CalendarNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 발행 헬퍼
 * 채널 생성 및 로컬 푸시 알림 발행
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarNotifier {
    companion object {
        /** 일정 관련 알림 채널 */
        const val CHANNEL_CALENDAR = "kairos_calendar"
        /** 일반 알림 채널 */
        const val CHANNEL_GENERAL = "kairos_general"

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

            // 일반 알림 채널
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "일반 알림",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "앱 일반 알림"
            }

            manager.createNotificationChannels(listOf(calendarChannel, generalChannel))
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
}
