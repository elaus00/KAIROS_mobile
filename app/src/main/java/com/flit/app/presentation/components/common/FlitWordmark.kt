package com.flit.app.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.ui.theme.FlitTheme
import com.flit.app.ui.theme.SoraFontFamily

/**
 * 워드마크 크기 프리셋 (brand-identity.md 스케일링 규칙 기반)
 *
 * dotOffset은 CSS `position:relative; top:Xpx` 과 동일한 의미:
 * 텍스트 베이스라인에서 아래로 이동하는 거리.
 */
enum class FlitWordmarkSize(
    val fontSize: TextUnit,
    val dotSize: Dp,
    val dotGap: Dp,
    val dotOffset: Dp
) {
    /** 히어로 섹션 (64sp) */
    HERO(64.sp, 11.dp, 3.5.dp, 1.5.dp),
    /** 기본 워드마크 (44sp) */
    DEFAULT(44.sp, 7.5.dp, 2.5.dp, 1.dp),
    /** 스플래시 화면 (32sp) */
    SPLASH(32.sp, 5.5.dp, 2.dp, 1.dp),
    /** 헤더/설정 (28sp) */
    HEADER(28.sp, 5.dp, 1.5.dp, 1.dp),
    /** 홈 앱바 (24sp) */
    TITLE(24.sp, 4.dp, 1.25.dp, 0.5.dp),
    /** 네비게이션/앱바 (20sp) */
    NAVIGATION(20.sp, 3.5.dp, 1.dp, 0.5.dp),
    /** 최소 크기 (14sp) */
    MINIMUM(14.sp, 2.5.dp, 0.5.dp, 0.5.dp)
}

/**
 * Flit. 브랜드 워드마크 컴포넌트
 *
 * Sora weight 480 텍스트 "Flit" + 독립 채움 도트(●)로 구성.
 * 도트는 텍스트 베이스라인 기준으로 배치 (CSS position:relative; top 과 동일).
 *
 * @param size 워드마크 크기 프리셋
 * @param color 워드마크 색상 (기본: FlitTheme.colors.text)
 * @param modifier Modifier
 */
@Composable
fun FlitWordmark(
    size: FlitWordmarkSize = FlitWordmarkSize.DEFAULT,
    color: Color = FlitTheme.colors.text,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val gapPx = with(density) { size.dotGap.roundToPx() }
    val offsetPx = with(density) { size.dotOffset.roundToPx() }

    Layout(
        content = {
            // [0] 텍스트
            Text(
                text = "Flit",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight(480),
                fontSize = size.fontSize,
                letterSpacing = 1.sp,
                color = color
            )
            // [1] 채움 도트
            Box(
                modifier = Modifier
                    .size(size.dotSize)
                    .background(color, CircleShape)
            )
        },
        modifier = modifier
    ) { measurables, constraints ->
        val textPlaceable = measurables[0].measure(constraints)
        val dotPlaceable = measurables[1].measure(constraints)

        // 텍스트 베이스라인 위치 (상단 기준)
        val baseline = textPlaceable[FirstBaseline]

        // 도트 Y: 베이스라인 - 도트높이 + 오프셋
        // CSS 동작: inline-block 하단이 baseline에 정렬 → relative top으로 아래 이동
        val dotY = baseline - dotPlaceable.height + offsetPx
        val dotX = textPlaceable.width + gapPx

        // 전체 크기: 도트가 텍스트 아래로 넘어갈 수 있으므로 maxOf
        val width = dotX + dotPlaceable.width
        val height = maxOf(textPlaceable.height, dotY + dotPlaceable.height)

        layout(width, height) {
            textPlaceable.placeRelative(0, 0)
            dotPlaceable.placeRelative(dotX, dotY)
        }
    }
}

@Preview(name = "워드마크 - 라이트")
@Preview(name = "워드마크 - 다크", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FlitWordmarkPreview() {
    FlitTheme {
        Box(Modifier.background(FlitTheme.colors.background)) {
            FlitWordmark(size = FlitWordmarkSize.DEFAULT)
        }
    }
}

@Preview(name = "워드마크 전체 크기")
@Composable
private fun FlitWordmarkSizesPreview() {
    FlitTheme {
        Box(Modifier.background(FlitTheme.colors.background)) {
            Column {
                FlitWordmarkSize.entries.forEach { wmSize ->
                    FlitWordmark(size = wmSize)
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}
