package com.example.kairos_mobile.presentation.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
            try {
                CaptureGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                // 위젯 갱신 실패는 무시 (테스트 환경 등)
            }
        }
    }

    /**
     * 할 일 위젯 갱신 (할 일 리스트 데이터 반영)
     */
    fun updateTodoWidget(context: Context) {
        scope.launch {
            try {
                TodoGlanceWidget().updateAll(context)
            } catch (e: Exception) {
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
