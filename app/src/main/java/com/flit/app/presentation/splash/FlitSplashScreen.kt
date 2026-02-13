package com.flit.app.presentation.splash

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.presentation.components.common.FlitWordmark
import com.flit.app.presentation.components.common.FlitWordmarkSize
import com.flit.app.ui.theme.FlitTheme
import com.flit.app.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay

/**
 * 브랜드 스플래시 화면
 * 시스템 스플래시(아이콘) 이후 표시 — 워드마크 + 태그라인으로 브랜드 각인
 *
 * @param onFinished 스플래시 종료 시 콜백 (메인 화면으로 전환)
 */
@Composable
fun FlitSplashScreen(
    onFinished: () -> Unit
) {
    val colors = FlitTheme.colors
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 페이드인
        alpha.animateTo(1f, animationSpec = tween(400))
        // 브랜드 노출 유지
        delay(1200)
        // 페이드아웃
        alpha.animateTo(0f, animationSpec = tween(300))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .alpha(alpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlitWordmark(
                size = FlitWordmarkSize.SPLASH,
                color = colors.text
            )
            Text(
                text = "적으면, 알아서 정리됩니다",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight(300),
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Preview(name = "스플래시 - 라이트")
@Preview(name = "스플래시 - 다크", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FlitSplashScreenPreview() {
    FlitTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FlitTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlitWordmark(
                    size = FlitWordmarkSize.SPLASH,
                    color = FlitTheme.colors.text
                )
                Text(
                    text = "적으면, 알아서 정리됩니다",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight(300),
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    color = FlitTheme.colors.textSecondary
                )
            }
        }
    }
}
