package com.flit.app.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.ui.theme.FlitTheme

/**
 * Flit Chip 컴포넌트
 * AI 분류 결과 표시, 필터 태그 용도
 * 높이 36dp, 시각적 크기 = 터치 영역 (터치 겹침 방지)
 */
@Composable
fun FlitChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val colors = FlitTheme.colors

    val backgroundColor = if (selected) colors.accent else colors.chipBg
    val textColor = if (selected) {
        if (colors.isDark) colors.background else Color.White
    } else {
        colors.chipText
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)  // 36dp 높이
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
