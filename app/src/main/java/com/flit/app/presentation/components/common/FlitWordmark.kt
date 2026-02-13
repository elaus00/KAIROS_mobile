package com.flit.app.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * @param fontSize Sora 텍스트 크기
 * @param dotSize 채움 도트 지름
 * @param dotGap 텍스트-도트 간격
 * @param dotOffset 도트 수직 오프셋 (위에서 아래로)
 */
enum class FlitWordmarkSize(
    val fontSize: TextUnit,
    val dotSize: Dp,
    val dotGap: Dp,
    val dotOffset: Dp
) {
    /** 히어로 섹션 (64sp) */
    HERO(64.sp, 11.dp, 3.5.dp, 19.dp),
    /** 기본 워드마크 (44sp) */
    DEFAULT(44.sp, 7.5.dp, 2.5.dp, 13.dp),
    /** 스플래시 화면 (32sp) */
    SPLASH(32.sp, 5.5.dp, 2.dp, 10.dp),
    /** 헤더/설정 (28sp) */
    HEADER(28.sp, 5.dp, 1.5.dp, 8.dp),
    /** 네비게이션/앱바 (20sp) */
    NAVIGATION(20.sp, 3.5.dp, 1.dp, 6.dp),
    /** 최소 크기 (14sp) */
    MINIMUM(14.sp, 2.5.dp, 0.5.dp, 4.dp)
}

/**
 * Flit. 브랜드 워드마크 컴포넌트
 *
 * Sora weight 480 텍스트 "Flit" + 독립 채움 도트(●)로 구성.
 * 마침표(.)를 폰트 글리프가 아닌 독립적 원형으로 표현하여 "완결" 의미 강조.
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
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
    ) {
        Text(
            text = "Flit",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight(480),
            fontSize = size.fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        Spacer(modifier = Modifier.width(size.dotGap))
        Box(
            modifier = Modifier
                .offset(y = size.dotOffset)
                .size(size.dotSize)
                .background(color, CircleShape)
        )
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
            androidx.compose.foundation.layout.Column {
                FlitWordmarkSize.entries.forEach { size ->
                    FlitWordmark(size = size)
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}
