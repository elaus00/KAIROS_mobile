package com.example.kairos_mobile.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.kairos_mobile.R

/**
 * 위젯 갱신 헬퍼
 * 앱 내부에서 데이터 변경 시 위젯을 즉시 갱신하기 위한 유틸리티
 */
object WidgetUpdateHelper {

    /**
     * 캡처 위젯 갱신 (오늘 캡처 수 등 동적 데이터 반영)
     */
    fun updateCaptureWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CaptureWidgetProvider::class.java)
            )
            if (widgetIds.isNotEmpty()) {
                val provider = CaptureWidgetProvider()
                provider.onUpdate(context, appWidgetManager, widgetIds)
            }
        } catch (e: Exception) {
            // 위젯 갱신 실패는 무시 (테스트 환경 등)
        }
    }

    /**
     * 할 일 위젯 갱신 (할 일 리스트 데이터 반영)
     */
    fun updateTodoWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodoWidgetProvider::class.java)
            )
            if (widgetIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_todo_list)
                val provider = TodoWidgetProvider()
                provider.onUpdate(context, appWidgetManager, widgetIds)
            }
        } catch (e: Exception) {
            // 위젯 갱신 실패는 무시 (테스트 환경 등)
        }
    }

    /**
     * 모든 위젯 갱신
     */
    fun updateAllWidgets(context: Context) {
        updateCaptureWidget(context)
        updateTodoWidget(context)
    }
}
