package com.example.kairos_mobile.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
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
import com.example.kairos_mobile.presentation.components.notifications.NotificationCard
import com.example.kairos_mobile.ui.components.AnimatedGlassBackgroundThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 알림 화면
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (String?) -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 테마에 따른 색상 설정
    val snackbarBgColor = if (isDarkTheme) GlassCard else AiryGlassCard
    val snackbarContentColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val accentColor = if (isDarkTheme) AccentBlue else AiryAccentBlue
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

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
        // 테마 인식 애니메이션 배경
        AnimatedGlassBackgroundThemed(isDarkTheme = isDarkTheme)

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
                            containerColor = snackbarBgColor,
                            contentColor = snackbarContentColor
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
                NotificationsHeader(
                    onBackClick = onBack,
                    onRefreshClick = viewModel::onRefresh,
                    unreadCount = uiState.unreadCount,
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 필터 탭
                FilterTabs(
                    selectedFilter = uiState.selectedFilter,
                    onFilterChanged = viewModel::onFilterChanged,
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 알림 리스트
                if (uiState.isLoading && uiState.notifications.isEmpty()) {
                    // 초기 로딩
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    }
                } else if (uiState.notifications.isEmpty() && !uiState.isLoading) {
                    // 빈 상태
                    EmptyNotificationsState(
                        filter = uiState.selectedFilter,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    // 알림 리스트
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    viewModel.onNotificationClick(notification.id)
                                    onNotificationClick(notification.relatedCaptureId)
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 알림 헤더
 */
@Composable
private fun NotificationsHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    unreadCount: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val accentColor = if (isDarkTheme) AccentBlue else AiryAccentBlue

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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = textPrimaryColor
            )
        }

        // 제목 + 읽지 않은 알림 개수
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "알림",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimaryColor,
                letterSpacing = 0.3.sp
            )
            if (unreadCount > 0) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = unreadCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 새로고침 버튼
        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "새로고침",
                tint = textPrimaryColor
            )
        }
    }
}

/**
 * 필터 탭
 */
@Composable
private fun FilterTabs(
    selectedFilter: NotificationFilter,
    onFilterChanged: (NotificationFilter) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterTab(
            text = "전체",
            isSelected = selectedFilter == NotificationFilter.ALL,
            onClick = { onFilterChanged(NotificationFilter.ALL) },
            isDarkTheme = isDarkTheme
        )
        FilterTab(
            text = "읽지 않음",
            isSelected = selectedFilter == NotificationFilter.UNREAD,
            onClick = { onFilterChanged(NotificationFilter.UNREAD) },
            isDarkTheme = isDarkTheme
        )
        FilterTab(
            text = "읽음",
            isSelected = selectedFilter == NotificationFilter.READ,
            onClick = { onFilterChanged(NotificationFilter.READ) },
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * 필터 탭 버튼
 */
@Composable
private fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val cardColor = if (isDarkTheme) GlassCard else AiryGlassCard
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.3f) else cardColor.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) textPrimaryColor else textSecondaryColor,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * 빈 알림 상태
 */
@Composable
private fun EmptyNotificationsState(
    filter: NotificationFilter,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = textTertiaryColor.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = when (filter) {
                    NotificationFilter.ALL -> "알림이 없습니다"
                    NotificationFilter.UNREAD -> "읽지 않은 알림이 없습니다"
                    NotificationFilter.READ -> "읽은 알림이 없습니다"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}
