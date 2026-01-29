package com.example.kairos_mobile.presentation.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.*

/**
 * 투두 그룹 헤더
 */
@Composable
fun TodoGroupHeader(
    title: String,
    count: Int,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimaryColor,
            letterSpacing = 0.3.sp
        )

        Text(
            text = "$count",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = textSecondaryColor
        )
    }
}
