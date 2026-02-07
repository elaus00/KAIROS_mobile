package com.example.kairos_mobile.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.kairos_mobile.MainActivity
import com.example.kairos_mobile.R

/**
 * 캡처 홈 화면 위젯
 * 탭하면 앱 열고 입력 화면으로 이동
 */
class CaptureWidgetProvider : AppWidgetProvider() {

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
        // 앱 열기 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_widget", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // RemoteViews 구성
        val views = RemoteViews(context.packageName, R.layout.widget_capture)
        views.setOnClickPendingIntent(R.id.widget_text, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
