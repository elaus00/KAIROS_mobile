package com.example.kairos_mobile.presentation.capture

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    val focusRequester = remember { FocusRequester() }

    // 갤러리 이미지 선택 런처
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleImageSelected(it) }
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

            // 전체 화면 텍스트 입력 영역 (위에서부터 작성)
            BasicTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInput(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .testTag("capture_input"),
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                singleLine = false,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (uiState.inputText.isEmpty()) {
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

            // 이미지 미리보기 (첨부된 경우)
            uiState.imageUri?.let { imageUri ->
                ImagePreview(
                    imageUri = imageUri,
                    onRemove = { viewModel.removeImage() }
                )
            }

            // 하단 툴바: 이미지 첨부 + 전송 버튼
            CaptureToolBar(
                isSubmitting = uiState.isSubmitting,
                canSubmit = uiState.inputText.isNotBlank() || uiState.imageUri != null,
                hasImage = uiState.imageUri != null,
                onSubmit = { viewModel.submit() },
                onImageClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
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
                tint = colors.text,
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
                tint = colors.text,
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
        val format = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        format.format(Date())
    }

    Text(
        text = today,
        color = colors.textSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

/**
 * 이미지 미리보기 (툴바 위)
 */
@Composable
private fun ImagePreview(
    imageUri: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(imageUri))
                .crossfade(true)
                .build(),
            contentDescription = "첨부 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        // 제거 버튼
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "이미지 제거",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.danger.copy(alpha = 0.8f))
                .clickable { onRemove() }
                .padding(2.dp)
        )
    }
}

/**
 * 하단 툴바: 이미지 첨부 아이콘 + 전송 버튼
 */
@Composable
private fun CaptureToolBar(
    isSubmitting: Boolean,
    canSubmit: Boolean,
    hasImage: Boolean,
    onSubmit: () -> Unit,
    onImageClick: () -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 이미지 아이콘
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "이미지 첨부",
            tint = if (hasImage) colors.accent else colors.iconMuted,
            modifier = Modifier
                .size(24.dp)
                .clickable { onImageClick() }
        )

        // 전송 버튼
        Box(
            modifier = Modifier
                .testTag("capture_submit")
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (canSubmit) colors.accent
                    else colors.accentBg
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isSubmitting && canSubmit
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
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "전송",
                    tint = if (canSubmit) {
                        if (colors.isDark) colors.background else Color.White
                    } else colors.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
