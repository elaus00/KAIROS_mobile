package com.example.kairos_mobile.presentation.todo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.navigation.NavRoutes
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.todo.components.*
import com.example.kairos_mobile.ui.components.AnimatedGlassBackgroundThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 투두 화면
 */
@Composable
fun TodoScreen(
    onNavigate: (String) -> Unit = {},
    isDarkTheme: Boolean = false,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val snackbarBgColor = if (isDarkTheme) GlassCard else AiryGlassCard
    val snackbarContentColor = if (isDarkTheme) TextPrimary else AiryTextPrimary

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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = viewModel::onStartAddingTodo,
                    containerColor = accentColor,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "새 투두 추가",
                        tint = TextPrimary
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = 100.dp)
                ) {
                    // 헤더
                    TodoHeader(
                        activeCount = uiState.activeCount,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 콘텐츠
                    if (uiState.isLoading && uiState.isEmpty) {
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
                    } else if (uiState.isEmpty) {
                        // 빈 상태
                        EmptyTodoState(
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        )
                    } else {
                        // 투두 리스트
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 그룹화된 활성 투두
                            uiState.groupedTodos.forEach { (groupKey, todos) ->
                                // 그룹 헤더
                                item(key = "header_$groupKey") {
                                    TodoGroupHeader(
                                        title = groupKey,
                                        count = todos.size,
                                        isDarkTheme = isDarkTheme
                                    )
                                }

                                // 투두 아이템
                                items(
                                    items = todos,
                                    key = { it.id }
                                ) { todo ->
                                    TodoItem(
                                        todo = todo,
                                        onToggleComplete = { viewModel.onToggleCompletion(todo.id) },
                                        onClick = { viewModel.onSelectTodo(todo) },
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }

                            // 완료된 투두 섹션
                            if (uiState.completedTodos.isNotEmpty()) {
                                item(key = "completed_header") {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        onClick = viewModel::onToggleCompletedSection,
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkTheme) {
                                            GlassCard.copy(alpha = 0.4f)
                                        } else {
                                            AiryGlassCard.copy(alpha = 0.4f)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = textSecondaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "완료됨",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textSecondaryColor
                                                )
                                                Text(
                                                    text = "${uiState.completedCount}",
                                                    fontSize = 13.sp,
                                                    color = textSecondaryColor.copy(alpha = 0.7f)
                                                )
                                            }

                                            Icon(
                                                imageVector = if (uiState.isCompletedExpanded) {
                                                    Icons.Default.ExpandLess
                                                } else {
                                                    Icons.Default.ExpandMore
                                                },
                                                contentDescription = null,
                                                tint = textSecondaryColor
                                            )
                                        }
                                    }
                                }

                                // 펼쳐진 경우 완료된 투두 표시
                                if (uiState.isCompletedExpanded) {
                                    items(
                                        items = uiState.completedTodos,
                                        key = { "completed_${it.id}" }
                                    ) { todo ->
                                        TodoItem(
                                            todo = todo,
                                            onToggleComplete = { viewModel.onToggleCompletion(todo.id) },
                                            onClick = { viewModel.onSelectTodo(todo) },
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 하단 네비게이션
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    KairosBottomNav(
                        selectedTab = KairosTab.CALENDAR,  // Todo는 Calendar 탭에 통합
                        onTabSelected = { tab ->
                            if (tab != KairosTab.CALENDAR) {
                                onNavigate(tab.route)
                            }
                        }
                    )
                }
            }
        }

        // 투두 상세 바텀시트
        uiState.selectedTodo?.let { todo ->
            TodoDetailBottomSheet(
                todo = todo,
                onToggleComplete = {
                    viewModel.onToggleCompletion(todo.id)
                    viewModel.onSelectTodo(null)
                },
                onDelete = { viewModel.onDeleteTodo(todo.id) },
                onDismiss = { viewModel.onSelectTodo(null) },
                isDarkTheme = isDarkTheme
            )
        }

        // TODO: 새 투두 추가 바텀시트 (uiState.isAddingTodo)
    }
}

/**
 * 투두 헤더
 */
@Composable
private fun TodoHeader(
    activeCount: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "할 일",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor,
                letterSpacing = 0.3.sp
            )

            if (activeCount > 0) {
                Text(
                    text = "${activeCount}개의 할 일",
                    fontSize = 14.sp,
                    color = textSecondaryColor
                )
            }
        }
    }
}
