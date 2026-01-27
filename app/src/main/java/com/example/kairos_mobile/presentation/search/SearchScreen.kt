package com.example.kairos_mobile.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
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
import com.example.kairos_mobile.presentation.components.*
import com.example.kairos_mobile.ui.components.AnimatedGlassBackgroundThemed
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * Search 화면
 * 캡처 검색 기능 제공
 */
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCaptureClick: (String) -> Unit,
    onNavigate: (String) -> Unit = {},
    isDarkTheme: Boolean = false,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // 테마에 따른 색상 설정
    val snackbarBgColor = if (isDarkTheme) GlassCard else AiryGlassCard
    val snackbarContentColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
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

    // 스크롤이 끝에 도달하면 더 로드
    LaunchedEffect(listState.canScrollForward) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= uiState.searchResults.size - 3 &&
                    uiState.hasMore &&
                    !uiState.isLoading
                ) {
                    viewModel.onLoadMore()
                }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = 100.dp) // 하단 네비게이션 공간 확보
                ) {
                // 헤더
                SearchHeader(
                    onBackClick = onBackClick,
                    onClearFilters = viewModel::onClearFilters,
                    hasFilters = uiState.selectedTypes.isNotEmpty(),
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 검색 바
                GlassSearchBar(
                    text = uiState.searchText,
                    onTextChange = viewModel::onSearchTextChanged,
                    onSearch = viewModel::onSearch,
                    onClear = { viewModel.onSearchTextChanged("") },
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 필터 칩
                FilterChipRow(
                    selectedTypes = uiState.selectedTypes,
                    onTypeToggle = viewModel::onTypeFilterToggle,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 검색 결과
                if (uiState.hasSearched) {
                    if (uiState.isLoading && uiState.searchResults.isEmpty()) {
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
                    } else if (uiState.searchResults.isEmpty() && !uiState.isLoading) {
                        // 결과 없음
                        EmptySearchState(
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        )
                    } else {
                        // 검색 결과 리스트
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.searchResults,
                                key = { it.id }
                            ) { capture ->
                                SearchResultCard(
                                    capture = capture,
                                    onClick = { onCaptureClick(capture.id) },
                                    isDarkTheme = isDarkTheme
                                )
                            }

                            // 페이징 로딩 인디케이터
                            if (uiState.isLoading && uiState.searchResults.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = accentColor,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 초기 상태 (검색 전)
                    InitialSearchState(
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    )
                }
            }

            // 하단 네비게이션
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                GlassBottomNavigation(
                    selectedTab = NavigationTab.SEARCH,
                    onTabSelected = { tab ->
                        val route = when (tab) {
                            NavigationTab.CAPTURE -> NavRoutes.CAPTURE
                            NavigationTab.SEARCH -> NavRoutes.SEARCH
                            NavigationTab.ARCHIVE -> NavRoutes.ARCHIVE
                            NavigationTab.SETTINGS -> NavRoutes.SETTINGS
                        }
                        if (route != NavRoutes.SEARCH) {
                            onNavigate(route)
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
        }
    }
}

/**
 * 검색 헤더
 */
@Composable
private fun SearchHeader(
    onBackClick: () -> Unit,
    onClearFilters: () -> Unit,
    hasFilters: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

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
                tint = textPrimaryColor
            )
        }

        // 제목
        Text(
            text = "Search",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimaryColor,
            letterSpacing = 0.3.sp
        )

        // 필터 초기화 버튼
        if (hasFilters) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = "초기화",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textTertiaryColor
                )
            }
        } else {
            // 공간 유지
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

/**
 * 초기 상태 (검색 전)
 */
@Composable
private fun InitialSearchState(
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
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = textTertiaryColor.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "캡처를 검색하세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * 빈 검색 결과 상태
 */
@Composable
private fun EmptySearchState(
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
            Text(
                text = "🔍",
                fontSize = 48.sp
            )
            Text(
                text = "검색 결과가 없습니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor,
                letterSpacing = 0.2.sp
            )
            Text(
                text = "다른 검색어나 필터를 시도해보세요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor.copy(alpha = 0.7f),
                letterSpacing = 0.2.sp
            )
        }
    }
}
