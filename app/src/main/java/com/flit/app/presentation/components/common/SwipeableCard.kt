package com.flit.app.presentation.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.ui.theme.FlitTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * SwipeableCard 컴포넌트
 * 스와이프 시 80dp 지점에서 멈추고 삭제 버튼 노출
 *
 * @param onDismiss 삭제 콜백
 * @param enableSwipe 스와이프 활성화 여부
 * @param content 카드 내용
 */
@Composable
fun SwipeableCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = FlitTheme.colors
    val cardShape = RoundedCornerShape(12.dp)

    if (enableSwipe) {
        val density = LocalDensity.current
        val revealWidthPx = with(density) { 80.dp.toPx() }
        val scope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }

        Box(modifier = modifier.clip(cardShape)) {
            // 배경: 삭제 버튼
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(colors.danger),
                contentAlignment = Alignment.CenterEnd
            ) {
                // HIG 2.2: 색상만으로 의미 전달하지 않는다 — 아이콘 + 텍스트 병행
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .clickable {
                            onDismiss()
                            scope.launch { offsetX.animateTo(0f, tween(200)) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

            // 전경: 콘텐츠 (offset 연동)
            Box(
                modifier = Modifier
                    .clip(cardShape)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    // 절반 이상 열렸으면 삭제 버튼 노출, 아니면 복귀
                                    val target =
                                        if (offsetX.value < -revealWidthPx / 2) -revealWidthPx else 0f
                                    offsetX.animateTo(target, tween(200))
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.animateTo(0f, tween(200)) }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    // 왼←오 방향만 허용 (음수 offset)
                                    val newValue =
                                        (offsetX.value + dragAmount).coerceIn(-revealWidthPx, 0f)
                                    offsetX.snapTo(newValue)
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}
