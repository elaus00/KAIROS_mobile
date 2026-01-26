package com.example.kairos_mobile.presentation.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.presentation.components.ArchiveCaptureCard
import com.example.kairos_mobile.ui.components.AnimatedGlassBackground
import com.example.kairos_mobile.ui.theme.*

/**
 * Archive 화면
 * 날짜별로 그룹화된 캡처 히스토리 표시
 */
@Composable
fun ArchiveScreen(
    onBackClick: () -> Unit,
    onCaptureClick: (String) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // 에러 메시지 스낵바 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onErrorDismissed()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 애니메이션 배경
        AnimatedGlassBackground()

        // 메인 콘텐츠
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(12.dp),
                            containerColor = GlassCard,
                            contentColor = TextPrimary
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
            ) {
                // 헤더
                ArchiveHeader(
                    onBackClick = onBackClick,
                    onRefreshClick = viewModel::onRefresh
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 캡처 리스트
                if (uiState.isLoading && uiState.groupedCaptures.isEmpty()) {
                    // 초기 로딩
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryNavy,
                            strokeWidth = 2.dp
                        )
                    }
                } else if (uiState.groupedCaptures.isEmpty() && !uiState.isLoading) {
                    // 빈 상태
                    EmptyArchiveState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    // 날짜별 그룹화된 리스트
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.groupedCaptures.forEach { (dateGroup, captures) ->
                            // 날짜 헤더
                            item(key = "header_$dateGroup") {
                                DateGroupHeader(dateGroup = dateGroup)
                            }

                            // 해당 날짜의 캡처들
                            items(
                                items = captures,
                                key = { it.id }
                            ) { capture ->
                                ArchiveCaptureCard(
                                    capture = capture,
                                    isExpanded = capture.id in uiState.expandedCaptureIds,
                                    onToggleExpand = { viewModel.onToggleExpand(capture.id) },
                                    onCaptureClick = { onCaptureClick(capture.id) }
                                )
                            }
                        }

                        // 로딩 인디케이터
                        if (uiState.isLoading && uiState.groupedCaptures.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = PrimaryNavy,
                                        strokeWidth = 2.dp
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
 * Archive 헤더
 */
@Composable
private fun ArchiveHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = TextPrimary
            )
        }

        // 제목
        Text(
            text = "History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            letterSpacing = 0.3.sp
        )

        // 새로고침 버튼
        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "새로고침",
                tint = TextPrimary
            )
        }
    }
}

/**
 * 날짜 그룹 헤더
 */
@Composable
private fun DateGroupHeader(
    dateGroup: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = dateGroup,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        letterSpacing = 0.3.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/**
 * 빈 Archive 상태
 */
@Composable
private fun EmptyArchiveState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = TextTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "저장된 캡처가 없습니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary,
                letterSpacing = 0.2.sp
            )
            Text(
                text = "캡처를 시작하여 히스토리를 만들어보세요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary.copy(alpha = 0.7f),
                letterSpacing = 0.2.sp
            )
        }
    }
}
