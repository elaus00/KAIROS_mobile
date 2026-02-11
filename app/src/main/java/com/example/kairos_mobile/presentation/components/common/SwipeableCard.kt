package com.example.kairos_mobile.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * SwipeableCard 컴포넌트 (PRD v4.0)
 * 스와이프로 삭제 가능한 카드
 *
 * @param onDismiss 삭제 콜백
 * @param content 카드 내용
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = KairosTheme.colors

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    if (enableSwipe) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                SwipeBackground(
                    dismissValue = dismissState.currentValue,
                    targetValue = dismissState.targetValue
                )
            },
            modifier = modifier,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            content = { content() }
        )
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * 스와이프 배경 (삭제 아이콘)
 */
@Composable
private fun SwipeBackground(
    dismissValue: SwipeToDismissBoxValue,
    targetValue: SwipeToDismissBoxValue
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = when (targetValue) {
            SwipeToDismissBoxValue.EndToStart -> colors.danger
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0.5f,
        animationSpec = tween(durationMillis = 200),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // HIG 2.2: 색상만으로 의미 전달하지 않는다 — 아이콘 + 텍스트 병행
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.scale(iconScale)
        ) {
            Text(
                text = "삭제",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
