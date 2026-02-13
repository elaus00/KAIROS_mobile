package com.flit.app.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
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
 * FlitTheme 색상 토큰 기반 커스텀 Snackbar
 * 배경: card + border 테두리, 메시지: text, 액션: accent
 */
@Composable
fun FlitSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = snackbarData.visuals.message,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false)
            )

            snackbarData.visuals.actionLabel?.let { actionLabel ->
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = actionLabel,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { snackbarData.performAction() }
                )
            }
        }
    }
}
