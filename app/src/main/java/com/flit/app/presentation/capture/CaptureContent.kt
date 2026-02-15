package com.flit.app.presentation.capture

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.FlitWordmark
import com.flit.app.presentation.components.common.FlitWordmarkSize
import com.flit.app.presentation.classification.AIStatusSheet
import com.flit.app.ui.theme.FlitTheme
import com.flit.app.ui.theme.FlitWritingFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 캡처 화면 (Home Tab) - ViewModel 보유
 * PRD v10.0: 상단바(Flit. + 벨 + 설정) + 날짜 + 빈 상태 + 하단 입력바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    autoFocusCapture: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaptureEvent.SubmitSuccess -> {
                    snackbarHostState.showSnackbar("캡처가 저장되었어요")
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

    CaptureContent(
        uiState = uiState,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHistory = onNavigateToHistory,
        autoFocusCapture = autoFocusCapture,
        onUpdateInput = viewModel::updateInput,
        onSubmit = viewModel::submit,
        onImageSelected = viewModel::handleImageSelected,
        onRemoveImage = viewModel::removeImage,
        onToggleStatusSheet = viewModel::toggleStatusSheet,
        onDismissStatusSheet = viewModel::dismissStatusSheet,
        onSaveDraft = viewModel::saveDraft,
        modifier = modifier
    )
}

/**
 * 캡처 화면 컨텐츠 - UI만 담당
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureContent(
    uiState: CaptureUiState,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    autoFocusCapture: Boolean,
    onUpdateInput: (String) -> Unit,
    onSubmit: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onRemoveImage: () -> Unit,
    onToggleStatusSheet: () -> Unit,
    onDismissStatusSheet: () -> Unit,
    onSaveDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppFontScaleProvider {
    val colors = FlitTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current
    val contentHorizontalPadding = 20.dp

    // 화면 복귀 시 글씨 크기 재로드 + 이탈 시 임시 저장
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> onSaveDraft()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSaveDraft()
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 위젯에서 진입 시 자동 포커스 + 키보드 표시
    LaunchedEffect(autoFocusCapture) {
        if (autoFocusCapture) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 갤러리 이미지 선택 런처
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // 상단 바: Flit. + 벨 아이콘(뱃지) + 설정 아이콘
            CaptureTopBar(
                unconfirmedCount = uiState.unconfirmedCount,
                onBellClick = onToggleStatusSheet,
                onHistoryClick = onNavigateToHistory,
                onSettingsClick = onNavigateToSettings
            )

            // 날짜 표시
            DateDisplay()

            // 전체 화면 텍스트 입력 영역 — 브런치 스타일 타이포그래피
            // 커서 핸들(물방울) 숨김
            val noHandleSelectionColors = TextSelectionColors(
                handleColor = Color.Transparent,
                backgroundColor = colors.accent.copy(alpha = 0.3f)
            )
            CompositionLocalProvider(
                LocalTextSelectionColors provides noHandleSelectionColors
            ) {
                val captureFontSize = 20.sp
                val captureLineHeight = 34.sp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = contentHorizontalPadding, vertical = 24.dp)
                ) {
                    TextField(
                        value = uiState.inputText,
                        onValueChange = onUpdateInput,
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .testTag("capture_input"),
                        textStyle = TextStyle(
                            fontFamily = FlitWritingFontFamily,
                            color = colors.text,
                            fontSize = captureFontSize,
                            lineHeight = captureLineHeight,
                            letterSpacing = 0.3.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Top,
                                trim = LineHeightStyle.Trim.None
                            )
                        ),
                        singleLine = false,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = colors.accent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            // 이미지 미리보기 — 부드러운 등장/퇴장 애니메이션
            // ColumnScope 오버로드를 사용하여 수직 슬라이드 적용
            this@Column.AnimatedVisibility(
                visible = uiState.imageUri != null,
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                // AnimatedVisibility 내부에서는 remember로 마지막 유효 URI 유지
                val imageUri = uiState.imageUri ?: return@AnimatedVisibility
                ImagePreview(
                    imageUri = imageUri,
                    onRemove = onRemoveImage
                )
            }

            // 하단 툴바: 이미지 첨부 + 전송 버튼
            CaptureToolBar(
                isSubmitting = uiState.isSubmitting,
                canSubmit = uiState.inputText.isNotBlank() || uiState.imageUri != null,
                hasImage = uiState.imageUri != null,
                horizontalPadding = contentHorizontalPadding,
                onSubmit = onSubmit,
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
                onDismiss = onDismissStatusSheet,
                onNavigateToHistory = {
                    onDismissStatusSheet()
                    onNavigateToHistory()
                }
            )
        }
    }
    }
}

/**
 * 캡처 화면 Preview
 */
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CaptureContentPreview() {
    FlitTheme {
        CaptureContent(
            uiState = CaptureUiState(
                inputText = "떠오르는 생각을 자유롭게...",
                unconfirmedCount = 3,
                isSubmitting = false,
                showStatusSheet = false,
                imageUri = null,
                errorMessage = null
            ),
            onNavigateToSettings = {},
            onNavigateToHistory = {},
            autoFocusCapture = false,
            onUpdateInput = {},
            onSubmit = {},
            onImageSelected = {},
            onRemoveImage = {},
            onToggleStatusSheet = {},
            onDismissStatusSheet = {},
            onSaveDraft = {}
        )
    }
}

/**
 * 상단 바: Flit. 제목 + 벨 아이콘(뱃지) + 설정 아이콘
 */
@Composable
private fun CaptureTopBar(
    unconfirmedCount: Int,
    onBellClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = FlitTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 브랜드 워드마크
        FlitWordmark(
            size = FlitWordmarkSize.TITLE,
            color = colors.text
        )

        // 오른쪽: 벨 + 히스토리 + 설정
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 벨 아이콘 (뱃지 포함)
            IconButton(onClick = onBellClick) {
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
                        tint = if (unconfirmedCount > 0) colors.text else colors.textMuted
                    )
                }
            }

            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "전체 기록",
                    tint = colors.text
                )
            }

            // 설정 아이콘
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = colors.text
                )
            }
        }
    }
}

/**
 * 날짜 표시
 */
@Composable
private fun DateDisplay() {
    val colors = FlitTheme.colors
    val currentTimeMs by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(60_000L)
            value = System.currentTimeMillis()
        }
    }
    val today = remember(currentTimeMs) {
        val format = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        format.format(Date(currentTimeMs))
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
    val colors = FlitTheme.colors

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
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "이미지 제거",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.danger.copy(alpha = 0.8f))
                    .padding(2.dp)
            )
        }
    }
}

/**
 * 하단 툴바: 이미지 첨부 아이콘 + 전송 버튼 (애니메이션 적용)
 */
@Composable
private fun CaptureToolBar(
    isSubmitting: Boolean,
    canSubmit: Boolean,
    hasImage: Boolean,
    horizontalPadding: Dp,
    onSubmit: () -> Unit,
    onImageClick: () -> Unit
) {
    val colors = FlitTheme.colors

    // 이미지 아이콘 색상 전환 애니메이션
    val imageIconColor by animateColorAsState(
        targetValue = if (hasImage) colors.accent else colors.iconMuted,
        animationSpec = tween(200),
        label = "imageIconColor"
    )

    // 전송 버튼 배경 색상 전환 애니메이션
    val submitBgColor by animateColorAsState(
        targetValue = if (canSubmit) colors.accent else colors.accentBg,
        animationSpec = tween(200),
        label = "submitBgColor"
    )

    // 전송 아이콘 색상 전환 애니메이션
    val submitIconColor by animateColorAsState(
        targetValue = if (canSubmit) {
            if (colors.isDark) colors.background else Color.White
        } else colors.textMuted,
        animationSpec = tween(200),
        label = "submitIconColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 이미지 아이콘
        IconButton(onClick = onImageClick) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "이미지 첨부",
                tint = imageIconColor
            )
        }

        // 전송 버튼 — AnimatedContent로 아이콘/로딩 전환
        FilledIconButton(
            onClick = onSubmit,
            enabled = !isSubmitting && canSubmit,
            modifier = Modifier
                .testTag("capture_submit")
                .size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = submitBgColor,
                contentColor = submitIconColor,
                disabledContainerColor = submitBgColor,
                disabledContentColor = submitIconColor
            )
        ) {
            AnimatedContent(
                targetState = isSubmitting,
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.8f))
                        .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                },
                label = "submitContent"
            ) { submitting ->
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (colors.isDark) colors.background else Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "전송",
                        tint = submitIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
