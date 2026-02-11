package com.example.kairos_mobile.presentation.detail

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.kairos_mobile.presentation.components.common.KairosChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 캡처 상세 화면
 * 분류 칩 변경 + 원문 확인 (최소 구현)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CaptureDetailScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: CaptureDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val context = LocalContext.current

    // 공유 텍스트가 설정되면 ShareSheet 실행
    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { text ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
            viewModel.onShareHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.aiTitle ?: "캡처 상세",
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.text
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onShare() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유",
                            tint = colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.onRetry() }) {
                        Text(
                            text = "다시 시도",
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 분류 칩 섹션
                Text(
                    text = "분류",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ClassificationChipRow(
                    currentType = uiState.classifiedType,
                    onTypeSelected = { type ->
                        viewModel.onChangeClassification(type)
                    }
                )

                // AI 자동 태그 표시
                if (uiState.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.tags.forEach { tag ->
                            KairosChip(text = "#$tag")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 캘린더 동기화 상태 (SCHEDULE 타입일 때)
                uiState.calendarSyncStatus?.let { syncStatus ->
                    CalendarSyncSection(
                        syncStatus = syncStatus,
                        onApprove = {
                            uiState.scheduleId?.let { viewModel.onApproveCalendarSync(it) }
                        },
                        onReject = {
                            uiState.scheduleId?.let { viewModel.onRejectCalendarSync(it) }
                        },
                        onOpenCalendar = {
                            openGoogleCalendar(context, uiState.scheduleStartTime)
                        },
                        onConnectCalendar = onNavigateToSettings
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 첨부 이미지 (있는 경우)
                uiState.imageUri?.let { imageUri ->
                    Text(
                        text = "첨부 이미지",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "첨부 이미지",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 생성 시각
                if (uiState.createdAt > 0) {
                    Text(
                        text = "생성 시각",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatDateTime(uiState.createdAt),
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 원문 텍스트
                Text(
                    text = "원문",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.originalText,
                        color = colors.text,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 분류 칩 Row (4옵션: 할 일 / 일정 / 노트 / 아이디어)
 */
@Composable
private fun ClassificationChipRow(
    currentType: ClassifiedType,
    onTypeSelected: (ClassifiedType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ClassificationChip(
            label = "할 일",
            isSelected = currentType == ClassifiedType.TODO,
            onClick = { onTypeSelected(ClassifiedType.TODO) }
        )
        ClassificationChip(
            label = "일정",
            isSelected = currentType == ClassifiedType.SCHEDULE,
            onClick = { onTypeSelected(ClassifiedType.SCHEDULE) }
        )
        ClassificationChip(
            label = "노트",
            isSelected = currentType == ClassifiedType.NOTES,
            onClick = { onTypeSelected(ClassifiedType.NOTES) }
        )
        ClassificationChip(
            label = "아이디어",
            isSelected = false,
            onClick = {
                onTypeSelected(ClassifiedType.NOTES)
                // NOTES + IDEA subtype은 ChangeClassificationUseCase에서 처리
            }
        )
    }
}

/**
 * 개별 분류 칩
 */
@Composable
private fun ClassificationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accent else colors.chipBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) {
                if (colors.isDark) colors.background else colors.card
            } else {
                colors.textSecondary
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 캘린더 동기화 상태 섹션
 */
@Composable
private fun CalendarSyncSection(
    syncStatus: CalendarSyncStatus,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onOpenCalendar: () -> Unit,
    onConnectCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "캘린더 동기화",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.card)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                // 상태 텍스트 + 아이콘 (색맹 접근성)
                val (statusText, statusColor, statusIcon) = when (syncStatus) {
                    CalendarSyncStatus.SYNCED -> Triple(
                        "Google Calendar에 동기화됨",
                        colors.success,
                        Icons.Outlined.Check
                    )
                    CalendarSyncStatus.SUGGESTION_PENDING -> Triple(
                        "캘린더 추가를 제안합니다",
                        colors.warning,
                        Icons.Outlined.Info
                    )
                    CalendarSyncStatus.SYNC_FAILED -> Triple(
                        "동기화 실패",
                        colors.danger,
                        Icons.Outlined.Warning
                    )
                    CalendarSyncStatus.REJECTED -> Triple(
                        "사용자가 거부함",
                        colors.textMuted,
                        Icons.Outlined.LinkOff
                    )
                    CalendarSyncStatus.NOT_LINKED -> Triple(
                        "연결되지 않음",
                        colors.textMuted,
                        Icons.Outlined.LinkOff
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // SUGGESTION_PENDING일 때 승인/거부 버튼
                if (syncStatus == CalendarSyncStatus.SUGGESTION_PENDING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.accent)
                                .clickable { onApprove() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "캘린더에 추가",
                                color = if (colors.isDark) colors.background else androidx.compose.ui.graphics.Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.chipBg)
                                .clickable { onReject() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "무시",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (syncStatus == CalendarSyncStatus.SYNCED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onOpenCalendar) {
                        Text(
                            text = "Google Calendar에서 보기",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (syncStatus == CalendarSyncStatus.NOT_LINKED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onConnectCalendar) {
                        Text(
                            text = "Google Calendar 연동하기",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 시간 포맷 (epoch ms → "yyyy.M.d HH:mm")
 */
private fun formatDateTime(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d HH:mm"))
}

private fun openGoogleCalendar(context: android.content.Context, startTime: Long?) {
    val targetTime = startTime ?: System.currentTimeMillis()
    val deepLink = Uri.Builder()
        .scheme("content")
        .authority("com.android.calendar")
        .appendPath("time")
        .appendPath(targetTime.toString())
        .build()

    val appIntent = Intent(Intent.ACTION_VIEW).apply {
        data = deepLink
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, targetTime)
        setPackage("com.google.android.calendar")
    }

    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://calendar.google.com/calendar/r/day")
    )

    runCatching { context.startActivity(appIntent) }
        .onFailure { context.startActivity(fallbackIntent) }
}
