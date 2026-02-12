package com.example.kairos_mobile.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

/**
 * 캡처 홈 화면 위젯 (Glance)
 * - 입력 영역 탭 → QuickCaptureActivity
 * - 오늘 캡처 수 동적 표시
 * - GlanceTheme 자동 다크/라이트 대응
 */
class CaptureGlanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CaptureWidgetEntryPoint {
        fun captureDao(): CaptureDao
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todayCount = getTodayCaptureCount(context)

        provideContent {
            GlanceTheme {
                CaptureContent(todayCount)
            }
        }
    }

    private suspend fun getTodayCaptureCount(context: Context): Int {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                CaptureWidgetEntryPoint::class.java
            )
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            entryPoint.captureDao().getTodayCaptureCount(todayStart)
        } catch (e: Exception) {
            0
        }
    }
}

@Composable
private fun CaptureContent(todayCount: Int) {
    val countText = if (todayCount > 0) "오늘 ${todayCount}개 캡처" else "오늘 첫 캡처를 시작하세요"

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        // 입력 영역 (탭하면 QuickCaptureActivity)
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .clickable(onClick = actionStartActivity<QuickCaptureActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "무엇이든 캡처하세요...",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        // 오늘 캡처 수
        Text(
            text = countText,
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier.padding(top = 4.dp)
        )
    }
}

/**
 * 캡처 위젯 리시버 (Manifest에 등록)
 */
class CaptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CaptureGlanceWidget()
}
