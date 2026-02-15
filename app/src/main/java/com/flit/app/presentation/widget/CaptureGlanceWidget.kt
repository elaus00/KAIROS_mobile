package com.flit.app.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.flit.app.R
import com.flit.app.domain.repository.UserPreferenceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 캡처 홈 화면 위젯 (Glance)
 * - 4x1 기본 크기의 컴팩트 캡처 카드
 * - 탭하면 QuickCaptureActivity 진입
 * - 리사이즈 지원
 * - 앱 테마 설정(SYSTEM/LIGHT/DARK) 반영
 */
class CaptureGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CaptureWidgetEntryPoint {
        fun userPreferenceRepository(): UserPreferenceRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CaptureWidgetEntryPoint::class.java
        )
        val isDark = resolveWidgetDarkTheme(context, entryPoint.userPreferenceRepository())

        provideContent {
            CaptureContent(
                isDark = isDark
            )
        }
    }
}

@Composable
private fun CaptureContent(
    isDark: Boolean
) {
    val colors = widgetColors(isDark)
    val widgetWidth: Dp = LocalSize.current.width
    val iconOnly = widgetWidth < 110.dp
    val textSize = if (widgetWidth < 170.dp) 16.sp else 18.sp

    if (iconOnly) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.background)
                .cornerRadius(16.dp)
                .clickable(onClick = actionStartActivity<QuickCaptureActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_edit),
                contentDescription = "캡처 입력",
                modifier = GlanceModifier.size(22.dp),
                colorFilter = ColorFilter.tint(colors.primary)
            )
        }
    } else {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.background)
                .cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(onClick = actionStartActivity<QuickCaptureActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_edit),
                contentDescription = "캡처 입력",
                modifier = GlanceModifier.size(22.dp),
                colorFilter = ColorFilter.tint(colors.primary)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "캡쳐하기",
                style = TextStyle(
                    fontSize = textSize,
                    color = colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

private data class CaptureWidgetColors(
    val background: ColorProvider,
    val primary: ColorProvider,
    val onSurfaceVariant: ColorProvider
)

private fun widgetColors(isDark: Boolean): CaptureWidgetColors {
    return if (isDark) {
        CaptureWidgetColors(
            background = fixedColor(Color(0xFF111111)),
            primary = fixedColor(Color(0xFFF0F0F0)),
            onSurfaceVariant = fixedColor(Color(0xFFBDBDBD))
        )
    } else {
        CaptureWidgetColors(
            background = fixedColor(Color(0xFFFFFFFF)),
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
