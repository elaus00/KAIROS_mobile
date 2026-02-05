package com.example.kairos_mobile.presentation.capture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 전체 화면 QuickCapture 오버레이 (앱 시작 시)
 *
 * @param initialText 초기 텍스트 (공유 인텐트에서 전달)
 */
@Composable
fun QuickCaptureOverlay(
    onDismiss: () -> Unit,
    onOpenCamera: () -> Unit = {},
    initialText: String? = null,
    viewModel: QuickCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // 초기 텍스트 설정 (공유 인텐트)
    LaunchedEffect(initialText) {
        if (!initialText.isNullOrBlank()) {
            viewModel.updateInput(initialText)
        }
    }

    // 축소 애니메이션 상태
    var isMinimizing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isMinimizing) 0.3f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isMinimizing) 0f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "alpha"
    )

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QuickCaptureEvent.SubmitSuccess -> {
                    snackbarHostState.showSnackbar("캡처가 저장되었습니다")
                    onDismiss()
                }
                is QuickCaptureEvent.MinimizeSuccess -> {
                    isMinimizing = true
                }
                is QuickCaptureEvent.CloseSuccess -> {
                    onDismiss()
                }
            }
        }
    }

    // 축소 애니메이션 완료 후 dismiss
    LaunchedEffect(isMinimizing, scale) {
        if (isMinimizing && scale <= 0.31f) {
            onDismiss()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    // 자동 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 바: 닫기(X) + 축소(-) 버튼 / 분류 태그
            OverlayTopBar(
                suggestedType = uiState.suggestedType,
                isClassifying = uiState.isClassifying,
                onClose = { viewModel.close() },
                onMinimize = { viewModel.saveDraft() },
                onTypeSelect = { viewModel.setType(it) }
            )

            // 텍스트 입력 영역 (전체 화면)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                BasicTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = colors.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 32.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.inputText.isEmpty()) {
                                Text(
                                    text = "떠오르는 생각을 캡처하세요...",
                                    color = colors.placeholder,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // 하단 바: 이미지 버튼 + 글자수 + 전송 버튼
            OverlayBottomBar(
                characterCount = uiState.characterCount,
                isSubmitting = uiState.isSubmitting,
                hasContent = uiState.inputText.isNotBlank(),
                onImageClick = onOpenCamera,
                onSubmit = { viewModel.submit() }
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}

/**
 * 오버레이 상단 바
 */
@Composable
private fun OverlayTopBar(
    suggestedType: CaptureType?,
    isClassifying: Boolean,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onTypeSelect: (CaptureType) -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 닫기 + 축소 버튼 (미니멀 macOS 스타일)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 닫기 버튼 (빨간색 원)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colors.danger)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
            )

            // 축소 버튼 (노란색 원)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colors.warning)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMinimize() }
            )
        }

        // 오른쪽: 분류 태그
        TypeTagChip(
            suggestedType = suggestedType,
            isClassifying = isClassifying,
            onTypeSelect = onTypeSelect
        )
    }
}

/**
 * 분류 태그 칩 (드롭다운 메뉴 포함)
 */
@Composable
private fun TypeTagChip(
    suggestedType: CaptureType?,
    isClassifying: Boolean,
    onTypeSelect: (CaptureType) -> Unit
) {
    val colors = KairosTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box {
        if (isClassifying) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = colors.textMuted
            )
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.chipBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = suggestedType?.getDisplayName() ?: "노트",
                    color = colors.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 타입 선택 드롭다운
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.card)
        ) {
            CaptureType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.getDisplayName(),
                            color = colors.text,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onTypeSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 오버레이 하단 바
 */
@Composable
private fun OverlayBottomBar(
    characterCount: Int,
    isSubmitting: Boolean,
    hasContent: Boolean,
    onImageClick: () -> Unit,
    onSubmit: () -> Unit
) {
    val colors = KairosTheme.colors

    HorizontalDivider(
        thickness = 1.dp,
        color = colors.borderLight
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 이미지 버튼
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "이미지 첨부",
            tint = colors.textMuted,
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onImageClick() }
        )

        // 중앙: 글자수
        Text(
            text = "$characterCount",
            color = colors.textMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )

        // 오른쪽: 전송 버튼
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (hasContent) colors.accent
                    else colors.accentBg
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isSubmitting && hasContent
                ) { onSubmit() },
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = if (colors.isDark) colors.background else Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "전송",
                    tint = if (hasContent) {
                        if (colors.isDark) colors.background else Color.White
                    } else colors.textMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
