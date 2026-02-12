package com.flit.app.presentation.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.presentation.components.common.AppFontScaleProvider
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
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                        Text(
                            text = "휴지통이 비어 있습니다",
                            color = colors.textMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            TrashItem(
                                capture = item,
                                onRestore = { viewModel.restoreItem(item.id) },
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
                        viewModel.emptyTrash()
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
                        deleteTargetId?.let { viewModel.deleteItem(it) }
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
 * 휴지통 항목
 */
@Composable
private fun TrashItem(
    capture: Capture,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FlitTheme.colors
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()) }
    val trashedDate = remember(capture.trashedAt) {
        capture.trashedAt?.let { dateFormat.format(Date(it)) } ?: ""
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 제목
            Text(
                text = capture.aiTitle ?: capture.originalText.take(50),
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            // 삭제 시각
            if (trashedDate.isNotEmpty()) {
                Text(
                    text = "삭제: $trashedDate",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeLabel,
                    color = colors.chipText,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.chipBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = "남은 ${remainingDays}일",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }

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
