package com.flit.app.presentation.trash

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.FlitSnackbar
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.ui.theme.FlitTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * 휴지통 화면
 * 삭제된 항목 조회, 복원, 완전 삭제
 */
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrashContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRestoreItem = viewModel::restoreItem,
        onDeleteItem = viewModel::deleteItem,
        onEmptyTrash = viewModel::emptyTrash,
        onDismissError = viewModel::dismissError,
        modifier = modifier
    )
}

/**
 * 휴지통 화면 Content
 */
@Composable
fun TrashContent(
    uiState: TrashUiState,
    onNavigateBack: () -> Unit,
    onRestoreItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onEmptyTrash: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                FlitSnackbar(snackbarData = data)
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            // 상단 바: 뒤로 가기 + 제목 + 비우기 버튼
            TrashTopBar(
                onNavigateBack = onNavigateBack,
                onEmptyTrash = { showEmptyTrashDialog = true },
                hasItems = uiState.items.isNotEmpty()
            )

            // 콘텐츠
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "로드 중...",
                                color = colors.textMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "휴지통이 비어 있어요",
                                color = colors.textMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            TrashItem(
                                capture = item,
                                isExpanded = expandedId == item.id,
                                onToggle = {
                                    expandedId = if (expandedId == item.id) null else item.id
                                },
                                onRestore = { onRestoreItem(item.id) },
                                onDelete = { deleteTargetId = item.id }
                            )
                        }
                    }
                }
            }
        }

        // 휴지통 비우기 확인 다이얼로그
        if (showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashDialog = false },
                title = { Text("휴지통 비우기", color = colors.text) },
                text = {
                    Text(
                        "모든 항목이 영구적으로 삭제됩니다.\n이 작업은 되돌릴 수 없습니다.",
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showEmptyTrashDialog = false
                        onEmptyTrash()
                    }) {
                        Text("삭제", color = colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyTrashDialog = false }) {
                        Text("취소", color = colors.textSecondary)
                    }
                },
                containerColor = colors.card,
                titleContentColor = colors.text,
                textContentColor = colors.textSecondary
            )
        }

        // 개별 항목 완전 삭제 확인 다이얼로그
        if (deleteTargetId != null) {
            AlertDialog(
                onDismissRequest = { deleteTargetId = null },
                title = { Text("완전 삭제", color = colors.text) },
                text = {
                    Text(
                        "이 항목을 영구적으로 삭제합니다.\n이 작업은 되돌릴 수 없습니다.",
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        deleteTargetId?.let { onDeleteItem(it) }
                        deleteTargetId = null
                    }) {
                        Text("삭제", color = colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTargetId = null }) {
                        Text("취소", color = colors.textSecondary)
                    }
                },
                containerColor = colors.card,
                titleContentColor = colors.text,
                textContentColor = colors.textSecondary
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrashContentPreview() {
    FlitTheme {
        TrashContent(
            uiState = TrashUiState(),
            onNavigateBack = {},
            onRestoreItem = {},
            onDeleteItem = {},
            onEmptyTrash = {},
            onDismissError = {}
        )
    }
}

/**
 * 휴지통 상단 바
 */
@Composable
private fun TrashTopBar(
    onNavigateBack: () -> Unit,
    onEmptyTrash: () -> Unit,
    hasItems: Boolean
) {
    val colors = FlitTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = colors.text
            )
        }

        Text(
            text = "휴지통",
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (hasItems) {
            TextButton(onClick = onEmptyTrash) {
                Text(
                    text = "비우기",
                    color = colors.danger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 휴지통 항목 (확장 가능한 카드)
 */
@Composable
private fun TrashItem(
    capture: Capture,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FlitTheme.colors
    val shortDateFormat = remember { SimpleDateFormat("M/d", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()) }

    val shortDateText = remember(capture.trashedAt) {
        capture.trashedAt?.let { shortDateFormat.format(Date(it)) } ?: ""
    }
    val trashedDate = remember(capture.trashedAt) {
        capture.trashedAt?.let { fullDateFormat.format(Date(it)) } ?: ""
    }
    val remainingDays = remember(capture.trashedAt) {
        val trashedAt = capture.trashedAt ?: return@remember 0
        val elapsedDays = ((System.currentTimeMillis() - trashedAt) / (24L * 60L * 60L * 1000L)).toInt()
        max(0, 30 - elapsedDays)
    }
    val typeLabel = remember(capture.classifiedType, capture.noteSubType) {
        when (capture.classifiedType) {
            ClassifiedType.SCHEDULE -> "일정"
            ClassifiedType.TODO -> "할 일"
            ClassifiedType.NOTES -> {
                if (capture.noteSubType == com.flit.app.domain.model.NoteSubType.IDEA) "아이디어" else "노트"
            }
            ClassifiedType.TEMP -> "미분류"
        }
    }

    val title = capture.aiTitle ?: capture.originalText.take(40)
    val preview = capture.originalText.take(100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 첫 줄: 제목 + 날짜
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = if (isExpanded) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = shortDateText,
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }

        // 미리보기 텍스트 (접힌 상태에서만)
        if (!isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preview,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }

        // 타입 칩
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = typeLabel,
                color = colors.chipText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.chipBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // 확장 영역: 상세 정보 + 액션 버튼
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                // 삭제 시각
                if (trashedDate.isNotEmpty()) {
                    Text(
                        text = "삭제일: $trashedDate",
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }

                // 남은 기간
                Text(
                    text = "남은 기간: ${remainingDays}일",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestore,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.accent
                        )
                    ) {
                        Text(
                            text = "복원",
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.danger
                        )
                    ) {
                        Text(
                            text = "완전 삭제",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
