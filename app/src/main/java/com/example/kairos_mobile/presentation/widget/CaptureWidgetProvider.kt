package com.example.kairos_mobile.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import com.example.kairos_mobile.R
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * 캡처 홈 화면 위젯
 * - 입력 영역 탭 → QuickCaptureActivity (홈 화면 위 플로팅 입력)
 * - 오늘 캡처 수 동적 표시
 * - 라이트/다크 테마 대응
 */
class CaptureWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CaptureWidgetEntryPoint {
        fun captureDao(): CaptureDao
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_capture)
        val isDark = isDarkMode(context)

        // 퀵 캡처 Activity 실행 Intent
        val quickCaptureIntent = Intent(context, QuickCaptureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val quickCapturePending = PendingIntent.getActivity(
            context, appWidgetId, quickCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_capture_input, quickCapturePending)

        // 오늘 캡처 수 조회
        val todayCount = getTodayCaptureCount(context)
        val countText = if (todayCount > 0) "오늘 ${todayCount}개 캡처" else "오늘 첫 캡처를 시작하세요"
        views.setTextViewText(R.id.widget_capture_count, countText)

        // 테마별 텍스트 색상 적용
        val textColor = if (isDark) 0xCCFFFFFF.toInt() else 0xFF111111.toInt()
        val hintColor = if (isDark) 0xFF888888.toInt() else 0xFF888888.toInt()
        val countColor = if (isDark) 0xFF888888.toInt() else 0xFF737373.toInt()

        views.setTextColor(R.id.widget_capture_hint, hintColor)
        views.setTextColor(R.id.widget_capture_count, countColor)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * 오늘 생성된 캡처 수 조회
     */
    private fun getTodayCaptureCount(context: Context): Int {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                CaptureWidgetEntryPoint::class.java
            )
            val captureDao = entryPoint.captureDao()

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            runBlocking {
                captureDao.getTodayCaptureCount(todayStart)
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun isDarkMode(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
