package com.flit.app.presentation.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flit.app.ui.theme.FlitTheme

/**
 * Premium 기능 접근 시 표시되는 게이트 바텀시트
 * 기능 이름 + 업그레이드 유도
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumGateSheet(
    featureName: String,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FlitTheme.colors
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Premium 기능",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$featureName 기능은\nPremium 구독이 필요합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.background
                )
            ) {
                Text(
                    text = "Premium으로 업그레이드",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onDismiss) {
                Text(
                    text = "나중에",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
