package com.example.kairos_mobile.presentation.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kairos_mobile.presentation.classification.AIStatusSheet
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 캡처 화면 (Home Tab)
 * PRD v10.0: 상단바(Kairos + 벨 + 설정) + 날짜 + 빈 상태 + 하단 입력바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureContent(
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaptureEvent.SubmitSuccess -> {
                    snackbarHostState.showSnackbar("캡처가 저장되었습니다")
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

    // 화면 이탈/백그라운드 시 임시 저장
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveDraft()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.saveDraft()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // 상단 바: Kairos + 벨 아이콘(뱃지) + 설정 아이콘
            CaptureTopBar(
                unconfirmedCount = uiState.unconfirmedCount,
                onBellClick = { viewModel.toggleStatusSheet() },
                onHistoryClick = onNavigateToHistory,
                onSettingsClick = onNavigateToSettings
            )

            // 날짜 표시
            DateDisplay()

            // 빈 상태 영역 (가운데 확장)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.inputText.isEmpty()) {
                    Text(
                        text = "떠오르는 생각을\n바로 던져보세요",
                        color = colors.placeholder,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 28.sp
                    )
                }
            }

            // 하단 입력 바
            CaptureInputBar(
                inputText = uiState.inputText,
                characterCount = uiState.characterCount,
                isSubmitting = uiState.isSubmitting,
                onInputChange = { viewModel.updateInput(it) },
                onSubmit = { viewModel.submit() }
            )
        }

        // AI Status Sheet (바텀시트)
        if (uiState.showStatusSheet) {
            AIStatusSheet(
                onDismiss = { viewModel.dismissStatusSheet() },
                onNavigateToHistory = {
                    viewModel.dismissStatusSheet()
                    onNavigateToHistory()
                }
            )
        }
    }
}

/**
 * 상단 바: Kairos 제목 + 벨 아이콘(뱃지) + 설정 아이콘
 */
@Composable
private fun CaptureTopBar(
    unconfirmedCount: Int,
    onBellClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 앱 제목
        Text(
            text = "Kairos",
            color = colors.text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        // 오른쪽: 벨 + 히스토리 + 설정
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 벨 아이콘 (뱃지 포함)
            BadgedBox(
                badge = {
                    if (unconfirmedCount > 0) {
                        Badge(
                            containerColor = colors.danger,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (unconfirmedCount > 99) "99+" else "$unconfirmedCount",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "AI 분류 현황",
                    tint = if (unconfirmedCount > 0) colors.text else colors.textMuted,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBellClick() }
                )
            }

            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "전체 기록",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onHistoryClick() }
            )

            // 설정 아이콘
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSettingsClick() }
            )
        }
    }
}

/**
 * 날짜 표시
 */
@Composable
private fun DateDisplay() {
    val colors = KairosTheme.colors
    val today = remember {
        val format = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN)
        format.format(Date())
    }

    Text(
        text = today,
        color = colors.textSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

/**
 * 하단 입력 바: 텍스트 필드 + 전송 버튼
 */
@Composable
private fun CaptureInputBar(
    inputText: String,
    characterCount: Int,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 이미지 아이콘 (비활성 — Phase 2 멀티모달 대비)
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "이미지 첨부",
            tint = colors.iconMuted,
            modifier = Modifier.size(24.dp)
        )

        // 텍스트 입력 필드
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = colors.text,
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(colors.accent),
            singleLine = false,
            maxLines = 4,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.accentBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "떠오르는 생각을 캡처하세요...",
                            color = colors.placeholder,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        // 전송 버튼
        Box(
            modifier = Modifier
                .size(44.dp)
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
                    modifier = Modifier.size(20.dp),
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
