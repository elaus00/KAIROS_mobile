package com.example.kairos_mobile.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Kairos Chip 컴포넌트 (PRD v4.0)
 * AI 분류 결과 표시용
 */
@Composable
fun KairosChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val colors = KairosTheme.colors

    val backgroundColor = if (selected) colors.accent else colors.chipBg
    val textColor = if (selected) {
        if (colors.isDark) colors.background else Color.White
    } else {
        colors.chipText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
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

/**
 * 작은 크기의 Kairos Chip
 */
@Composable
fun KairosChipSmall(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.chipBg)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.chipText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 아웃라인 스타일 Kairos Chip
 */
@Composable
fun KairosChipOutlined(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val colors = KairosTheme.colors

    val backgroundColor = if (selected) colors.accentBg else Color.Transparent
    val textColor = if (selected) colors.accent else colors.textSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
