package com.example.kairos_mobile.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Glassmorphism 스타일의 헤더
 */
@Composable
fun GlassHeader(
    onNotificationClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextQuaternary else AiryTextTertiary

    val dateFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hourOfDay) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜 및 인사말
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = currentDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondaryColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = greeting,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor,
                letterSpacing = (-0.5).sp
            )
        }

        // 알림 버튼
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .glassButtonThemed(isDarkTheme = isDarkTheme, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "알림",
                tint = textPrimaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
