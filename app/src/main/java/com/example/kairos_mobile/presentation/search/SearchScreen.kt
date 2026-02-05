package com.example.kairos_mobile.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.components.search.FilterChipRow
import com.example.kairos_mobile.presentation.components.search.SearchBar
import com.example.kairos_mobile.presentation.components.search.SearchResultCard
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Search 화면
 * 캡처 검색 기능 제공
 */
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCaptureClick: (String) -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val colors = KairosTheme.colors

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
    LaunchedEffect(Unit) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val currentState = viewModel.uiState.value
            Triple(lastVisibleIndex, currentState.hasMore, currentState.isLoading)
        }.collect { (lastVisibleIndex, hasMore, isLoading) ->
            val resultsSize = viewModel.uiState.value.searchResults.size
            if (lastVisibleIndex != null &&
                lastVisibleIndex >= resultsSize - 3 &&
                hasMore &&
                !isLoading
            ) {
                viewModel.onLoadMore()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = colors.card,
                        contentColor = colors.text
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = 80.dp) // 하단 네비게이션 공간 확보
            ) {
                // 헤더
                SearchHeader(
                    onBackClick = onBackClick,
                    onClearFilters = viewModel::onClearFilters,
                    hasFilters = uiState.selectedTypes.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 검색 바
                SearchBar(
                    text = uiState.searchText,
                    onTextChange = viewModel::onSearchTextChanged,
                    onSearch = viewModel::onSearch,
                    onClear = { viewModel.onSearchTextChanged("") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 필터 칩
                FilterChipRow(
                    selectedTypes = uiState.selectedTypes,
                    onTypeToggle = viewModel::onTypeFilterToggle,
                    modifier = Modifier.padding(horizontal = 16.dp)
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
                                color = colors.accent,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (uiState.searchResults.isEmpty() && !uiState.isLoading) {
                        // 결과 없음
                        EmptySearchState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    } else {
                        // 검색 결과 리스트
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.searchResults,
                                key = { it.id }
                            ) { capture ->
                                SearchResultCard(
                                    capture = capture,
                                    onClick = { onCaptureClick(capture.id) }
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
                                            color = colors.accent,
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // 하단 네비게이션
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                KairosBottomNav(
                    selectedTab = KairosTab.HOME,
                    onTabSelected = { tab ->
                        onNavigate(tab.route)
                    }
                )
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
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

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
                tint = colors.text
            )
        }

        // 제목
        Text(
            text = "Search",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text,
            letterSpacing = 0.3.sp
        )

        // 필터 초기화 버튼
        if (hasFilters) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = "초기화",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted
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
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "캡처를 검색하세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textMuted,
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
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "검색 결과가 없습니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textMuted,
                letterSpacing = 0.2.sp
            )
            Text(
                text = "다른 검색어나 필터를 시도해보세요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textMuted.copy(alpha = 0.7f),
                letterSpacing = 0.2.sp
            )
        }
    }
}
