package com.flit.app.presentation.widget

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.flit.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * 위젯 갱신 헬퍼
 * 앱 내부에서 데이터 변경 시 Glance 위젯을 즉시 갱신하기 위한 유틸리티
 */
object WidgetUpdateHelper {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 캡처 위젯 갱신 (오늘 캡처 수 등 동적 데이터 반영)
     */
    fun updateCaptureWidget(context: Context) {
        scope.launch {
            val traceId = newTraceId("capture")
            logDebug("[$traceId] updateCaptureWidget requested")
            try {
                val elapsed = measureTimeMillis {
                    CaptureGlanceWidget().updateAll(context)
                }
                logDebug("[$traceId] updateCaptureWidget success durationMs=$elapsed")
            } catch (e: Exception) {
                Log.e(WIDGET_HELPER_TAG, "[$traceId] updateCaptureWidget failed", e)
                // 위젯 갱신 실패는 무시 (테스트 환경 등)
            }
        }
    }

    /**
     * 할 일 위젯 갱신 (할 일 리스트 데이터 반영)
     */
    fun updateTodoWidget(context: Context) {
        scope.launch {
            val traceId = newTraceId("todo")
            logDebug("[$traceId] updateTodoWidget requested")
            try {
                val glanceIds = runCatching {
                    GlanceAppWidgetManager(context).getGlanceIds(TodoGlanceWidget::class.java)
                }.getOrElse { e ->
                    Log.e(WIDGET_HELPER_TAG, "[$traceId] failed to get glance ids", e)
                    emptyList()
                }
                logDebug("[$traceId] updateTodoWidget targets=${glanceIds.joinToString()}")
                val elapsed = measureTimeMillis {
                    TodoGlanceWidget().updateAll(context)
                }
                logDebug("[$traceId] updateTodoWidget success durationMs=$elapsed")
            } catch (e: Exception) {
                Log.e(WIDGET_HELPER_TAG, "[$traceId] updateTodoWidget failed", e)
                // 위젯 갱신 실패는 무시 (테스트 환경 등)
            }
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

private const val WIDGET_HELPER_TAG = "WidgetUpdateHelperTrace"

private fun newTraceId(prefix: String): String =
    "$prefix-${SystemClock.elapsedRealtimeNanos().toString(16)}"

private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(WIDGET_HELPER_TAG, message)
    }
}
