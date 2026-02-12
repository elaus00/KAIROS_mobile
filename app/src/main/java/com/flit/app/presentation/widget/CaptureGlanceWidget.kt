package com.flit.app.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.flit.app.R
import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.domain.repository.UserPreferenceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

/**
 * 캡처 홈 화면 위젯 (Glance)
 * - 노트패드 스타일 대형 카드 (화면 절반 크기)
 * - 탭하면 QuickCaptureActivity 진입
 * - 오늘 캡처 수 동적 표시
 * - 앱 테마 설정(SYSTEM/LIGHT/DARK) 반영
 */
class CaptureGlanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CaptureWidgetEntryPoint {
        fun captureDao(): CaptureDao
        fun userPreferenceRepository(): UserPreferenceRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CaptureWidgetEntryPoint::class.java
        )
        val todayCount = getTodayCaptureCount(entryPoint)
        val isDark = resolveWidgetDarkTheme(context, entryPoint.userPreferenceRepository())

        provideContent {
            CaptureContent(
                todayCount = todayCount,
                isDark = isDark
            )
        }
    }

    private suspend fun getTodayCaptureCount(entryPoint: CaptureWidgetEntryPoint): Int {
        return try {
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
private fun CaptureContent(
    todayCount: Int,
    isDark: Boolean
) {
    val colors = widgetColors(isDark)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(16.dp)
            .padding(16.dp)
    ) {
        // 노트패드 스타일 입력 영역 (탭하면 QuickCaptureActivity)
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .background(colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(16.dp)
                .clickable(onClick = actionStartActivity<QuickCaptureActivity>())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_edit),
                    contentDescription = "캡처 입력",
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(colors.primary)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "무엇이든 캡처하세요",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colors.onSurfaceVariant
                    )
                )
            }
        }

        // 하단: 오늘 캡처 수
        if (todayCount > 0) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "오늘 ${todayCount}개",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                )
            }
        }
    }
}

private data class CaptureWidgetColors(
    val background: ColorProvider,
    val surfaceVariant: ColorProvider,
    val primary: ColorProvider,
    val onSurfaceVariant: ColorProvider
)

private fun widgetColors(isDark: Boolean): CaptureWidgetColors {
    return if (isDark) {
        CaptureWidgetColors(
            background = fixedColor(Color(0xFF111111)),
            surfaceVariant = fixedColor(Color(0xFF1D1D1D)),
            primary = fixedColor(Color(0xFFF0F0F0)),
            onSurfaceVariant = fixedColor(Color(0xFFBDBDBD))
        )
    } else {
        CaptureWidgetColors(
            background = fixedColor(Color(0xFFFFFFFF)),
            surfaceVariant = fixedColor(Color(0xFFF5F5F5)),
            primary = fixedColor(Color(0xFF1A1A1A)),
            onSurfaceVariant = fixedColor(Color(0xFF616161))
        )
    }
}

private fun fixedColor(color: Color): ColorProvider =
    ColorProvider(color)

/**
 * 캡처 위젯 리시버 (Manifest에 등록)
 */
class CaptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CaptureGlanceWidget()
}
