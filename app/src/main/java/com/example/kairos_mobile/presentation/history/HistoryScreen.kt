package com.example.kairos_mobile.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.components.common.SwipeableCard
import com.example.kairos_mobile.presentation.history.components.HistoryItem
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 전체 기록 화면
 * 역시간순, 20개씩 무한 스크롤
 * 스와이프 삭제 + Snackbar 실행 취소
 */
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onCaptureClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // 이벤트 처리 (삭제 → Snackbar + 실행 취소)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.DeleteSuccess -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "삭제되었습니다",
                        actionLabel = "실행 취소",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.captureId)
                    }
                }
                is HistoryEvent.UndoSuccess -> {
                    // 실행 취소 완료 — 목록 자동 새로고침
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

    // 무한 스크롤 감지
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 5 && !uiState.isLoadingMore && uiState.hasMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
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
            // 상단 바: 뒤로 가기 + 제목
            HistoryTopBar(onNavigateBack = onNavigateBack)

            // 콘텐츠
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = colors.textMuted
                        )
                    }
                }

                uiState.captures.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "기록이 없습니다",
                            color = colors.textMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.captures,
                            key = { it.id }
                        ) { capture ->
                            SwipeableCard(
                                onDismiss = { viewModel.deleteCaptureById(capture.id) }
                            ) {
                                HistoryItem(
                                    capture = capture,
                                    onChangeType = { type, subType ->
                                        viewModel.changeClassification(capture.id, type, subType)
                                    },
                                    modifier = Modifier.clickable {
                                        onCaptureClick(capture.id)
                                    }
                                )
                            }
                        }

                        // 추가 로딩 인디케이터
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 전체 기록 상단 바
 */
@Composable
private fun HistoryTopBar(
    onNavigateBack: () -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로",
            tint = colors.text,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNavigateBack() }
        )

        Text(
            text = "전체 기록",
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
