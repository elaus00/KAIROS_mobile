package com.flit.app.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.presentation.components.common.FlitChip
import com.flit.app.presentation.components.common.SwipeableCard
import com.flit.app.presentation.history.components.HistoryItem
import com.flit.app.ui.theme.FlitTheme
import java.util.Calendar

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
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors
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

            // 필터 칩 (유형 + 날짜 범위 통합 1열)
            HistoryFilterRow(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.setTypeFilter(it) },
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateRangeSelected = { start, end -> viewModel.setDateRange(start, end) }
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

                uiState.captures.isEmpty() -> {
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
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "기록이 없습니다",
                                color = colors.textMuted,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "캡처한 내용이 여기에 시간순으로 표시됩니다",
                                color = colors.textMuted.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
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
}

/**
 * 전체 기록 상단 바
 */
@Composable
private fun HistoryTopBar(
    onNavigateBack: () -> Unit
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
            text = "전체 기록",
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 유형 + 날짜 범위 필터 통합 1열
 * 유형 칩 | 세로 구분선 | 날짜 범위 칩
 * HIG 2.3: 터치 타겟 44pt 이상, 8pt 그리드 준수
 */
@Composable
private fun HistoryFilterRow(
    selectedType: ClassifiedType?,
    onTypeSelected: (ClassifiedType?) -> Unit,
    startDate: Long?,
    endDate: Long?,
    onDateRangeSelected: (Long?, Long?) -> Unit
) {
    val colors = FlitTheme.colors
    val scrollState = rememberScrollState()
    val bgColor = colors.background

    val now = remember { System.currentTimeMillis() }

    val todayStart = remember {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val weekStart = remember {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val monthStart = remember {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val selectedRange = when {
        startDate == null && endDate == null -> "전체"
        startDate == todayStart -> "오늘"
        startDate == weekStart -> "이번 주"
        startDate == monthStart -> "이번 달"
        else -> "기타"
    }

    // 스크롤 가능 표시를 위해 Box로 감싸고 오른쪽 페이드 그래디언트 추가
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // 오른쪽 끝 페이드 그래디언트 (더 스크롤 가능 표시)
                if (scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue) {
                    val fadeWidth = 32.dp.toPx()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, bgColor),
                            startX = size.width - fadeWidth,
                            endX = size.width
                        )
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 유형 필터
            FlitChip(
                text = "전체",
                selected = selectedType == null,
                onClick = { onTypeSelected(null) }
            )
            FlitChip(
                text = "일정",
                selected = selectedType == ClassifiedType.SCHEDULE,
                onClick = { onTypeSelected(ClassifiedType.SCHEDULE) }
            )
            FlitChip(
                text = "할 일",
                selected = selectedType == ClassifiedType.TODO,
                onClick = { onTypeSelected(ClassifiedType.TODO) }
            )
            FlitChip(
                text = "노트",
                selected = selectedType == ClassifiedType.NOTES,
                onClick = { onTypeSelected(ClassifiedType.NOTES) }
            )

            // 세로 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.border)
            )

            // 날짜 범위 필터
            FlitChip(
                text = "전체",
                selected = selectedRange == "전체",
                onClick = { onDateRangeSelected(null, null) }
            )
            FlitChip(
                text = "오늘",
                selected = selectedRange == "오늘",
                onClick = { onDateRangeSelected(todayStart, null) }
            )
            FlitChip(
                text = "이번 주",
                selected = selectedRange == "이번 주",
                onClick = { onDateRangeSelected(weekStart, null) }
            )
            FlitChip(
                text = "이번 달",
                selected = selectedRange == "이번 달",
                onClick = { onDateRangeSelected(monthStart, null) }
            )
        }
    }
}
