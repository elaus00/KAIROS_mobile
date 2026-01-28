package com.example.kairos_mobile.presentation.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 섹션 헤더 컴포넌트 (PRD v4.0)
 * 12px 대문자, 0.5px letter-spacing
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

/**
 * 섹션 헤더 (한글용, 대문자 변환 없음)
 */
@Composable
fun SectionHeaderKorean(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}
