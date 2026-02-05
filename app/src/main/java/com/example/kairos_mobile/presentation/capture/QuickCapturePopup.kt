package com.example.kairos_mobile.presentation.capture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 작은 팝업 형태의 QuickCapture (FAB 클릭 시)
 * FAB 위에 뜨는 작은 카드 형태
 */
@Composable
fun QuickCapturePopup(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenCamera: () -> Unit = {},
    viewModel: QuickCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QuickCaptureEvent.SubmitSuccess -> {
                    snackbarHostState.showSnackbar("캡처가 저장되었습니다")
                    viewModel.clearInput()
                    onDismiss()
                }
                is QuickCaptureEvent.MinimizeSuccess -> {
                    onDismiss()
                }
                is QuickCaptureEvent.CloseSuccess -> {
                    onDismiss()
                }
            }
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
    LaunchedEffect(visible) {
        if (visible) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 배경 오버레이 (터치 시 닫기)
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // 팝업 카드
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(
                initialScale = 0.8f,
                transformOrigin = TransformOrigin(0.9f, 1f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.8f,
                transformOrigin = TransformOrigin(0.9f, 1f)
            ) + slideOutVertically(
                targetOffsetY = { it / 4 }
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .navigationBarsPadding()
        ) {
            PopupCard(
                inputText = uiState.inputText,
                characterCount = uiState.characterCount,
                suggestedType = uiState.suggestedType,
                isClassifying = uiState.isClassifying,
                isSubmitting = uiState.isSubmitting,
                focusRequester = focusRequester,
                onInputChange = { viewModel.updateInput(it) },
                onTypeSelect = { viewModel.setType(it) },
                onClose = { viewModel.close() },
                onImageClick = onOpenCamera,
                onSubmit = { viewModel.submit() }
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
        )
    }
}

/**
 * 팝업 카드 컴포넌트
 */
@Composable
private fun PopupCard(
    inputText: String,
    characterCount: Int,
    suggestedType: CaptureType?,
    isClassifying: Boolean,
    isSubmitting: Boolean,
    focusRequester: FocusRequester,
    onInputChange: (String) -> Unit,
    onTypeSelect: (CaptureType) -> Unit,
    onClose: () -> Unit,
    onImageClick: () -> Unit,
    onSubmit: () -> Unit
) {
    val colors = KairosTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(
                width = 0.5.dp,
                color = colors.borderLight,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // 상단: 닫기 버튼 + 분류 태그
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 닫기 버튼
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.danger)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // 분류 태그
            PopupTypeChip(
                suggestedType = suggestedType,
                isClassifying = isClassifying,
                onTypeSelect = onTypeSelect
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.borderLight
        )

        // 텍스트 입력 영역
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 180.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { innerTextField ->
                Box {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "빠른 메모...",
                            color = colors.placeholder,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            }
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.borderLight
        )

        // 하단: 이미지 버튼 + 글자수 + 전송 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지 버튼
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "이미지 첨부",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onImageClick() }
            )

            // 글자수
            Text(
                text = "$characterCount",
                color = colors.textMuted,
                fontSize = 13.sp
            )

            // 전송 버튼
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) colors.accent
                        else colors.accentBg
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isSubmitting && inputText.isNotBlank()
                    ) { onSubmit() },
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = if (colors.isDark) colors.background else Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "전송",
                        tint = if (inputText.isNotBlank()) {
                            if (colors.isDark) colors.background else Color.White
                        } else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 팝업용 분류 태그 칩
 */
@Composable
private fun PopupTypeChip(
    suggestedType: CaptureType?,
    isClassifying: Boolean,
    onTypeSelect: (CaptureType) -> Unit
) {
    val colors = KairosTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box {
        if (isClassifying) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.textMuted
            )
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.chipBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = suggestedType?.getDisplayName() ?: "노트",
                    color = colors.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

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
                            fontSize = 13.sp
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
