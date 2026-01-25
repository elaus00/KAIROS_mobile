package com.example.kairos_mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.ui.theme.BlobNavy
import com.example.kairos_mobile.ui.theme.BlobNavyDim
import com.example.kairos_mobile.ui.theme.BlobSlate
import com.example.kairos_mobile.ui.theme.NavyDark

/**
 * Glassmorphism 애니메이션 배경
 *
 * 3개의 애니메이션되는 blur된 원형 요소 (blobs)로 구성
 */
@Composable
fun AnimatedGlassBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
    ) {
        // Blob 1: 좌상단 (네이비, 크게)
        AnimatedBlob(
            size = 600.dp,
            color = BlobNavy,
            blurRadius = 120.dp,
            startOffsetX = (-0.1f),
            startOffsetY = (-0.2f),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 14000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            animationType = BlobAnimationType.TYPE_1
        )

        // Blob 2: 우측 중앙 (슬레이트, 중간)
        AnimatedBlob(
            size = 500.dp,
            color = BlobSlate,
            blurRadius = 100.dp,
            startOffsetX = 1.1f,
            startOffsetY = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 16000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            animationType = BlobAnimationType.TYPE_2
        )

        // Blob 3: 하단 좌측 (네이비 dim, 크게)
        AnimatedBlob(
            size = 700.dp,
            color = BlobNavyDim,
            blurRadius = 140.dp,
            startOffsetX = 0.2f,
            startOffsetY = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 18000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            animationType = BlobAnimationType.TYPE_1
        )
    }
}

/**
 * 개별 애니메이션 Blob
 */
@Composable
private fun AnimatedBlob(
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    blurRadius: androidx.compose.ui.unit.Dp,
    startOffsetX: Float,
    startOffsetY: Float,
    animationSpec: InfiniteRepeatableSpec<Float>,
    animationType: BlobAnimationType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob_animation")

    val offsetXProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "offset_x"
    )

    val offsetYProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "offset_y"
    )

    val scaleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "scale"
    )

    // 애니메이션 타입에 따라 다른 경로
    val (currentOffsetX, currentOffsetY, currentScale) = when (animationType) {
        BlobAnimationType.TYPE_1 -> {
            // 0% → 33% → 66% → 100% 경로
            val progress = offsetXProgress
            when {
                progress < 0.33f -> {
                    val t = progress / 0.33f
                    Triple(
                        startOffsetX + (0.03f * t),
                        startOffsetY - (0.05f * t),
                        1f + (0.1f * t)
                    )
                }
                progress < 0.66f -> {
                    val t = (progress - 0.33f) / 0.33f
                    Triple(
                        startOffsetX + 0.03f - (0.05f * t),
                        startOffsetY - 0.05f + (0.07f * t),
                        1.1f - (0.2f * t)
                    )
                }
                else -> {
                    val t = (progress - 0.66f) / 0.34f
                    Triple(
                        startOffsetX - 0.02f + (0.02f * t),
                        startOffsetY + 0.02f - (0.02f * t),
                        0.9f + (0.1f * t)
                    )
                }
            }
        }
        BlobAnimationType.TYPE_2 -> {
            // 다른 경로 (반대 방향)
            val progress = offsetYProgress
            when {
                progress < 0.33f -> {
                    val t = progress / 0.33f
                    Triple(
                        startOffsetX - (0.03f * t),
                        startOffsetY + (0.05f * t),
                        1f + (0.1f * t)
                    )
                }
                progress < 0.66f -> {
                    val t = (progress - 0.33f) / 0.33f
                    Triple(
                        startOffsetX - 0.03f + (0.05f * t),
                        startOffsetY + 0.05f - (0.07f * t),
                        1.1f - (0.2f * t)
                    )
                }
                else -> {
                    val t = (progress - 0.66f) / 0.34f
                    Triple(
                        startOffsetX + 0.02f - (0.02f * t),
                        startOffsetY - 0.02f + (0.02f * t),
                        0.9f + (0.1f * t)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .offset(
                x = (currentOffsetX * 1000).dp,
                y = (currentOffsetY * 1000).dp
            )
            .size(size * currentScale)
            .blur(blurRadius)
            .background(color, shape = CircleShape)
    )
}

/**
 * Blob 애니메이션 타입
 */
private enum class BlobAnimationType {
    TYPE_1,  // float 애니메이션
    TYPE_2   // float-delayed 애니메이션
}
