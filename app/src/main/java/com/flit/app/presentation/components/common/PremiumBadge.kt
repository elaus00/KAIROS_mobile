package com.flit.app.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.ui.theme.FlitTheme

/**
 * Premium 배지 컴포넌트
 * 자물쇠 아이콘 + "Premium" 텍스트
 */
@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.accent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Premium",
            color = colors.accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
