package com.flit.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.SemanticSearchResult
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.FilterChipRow
import com.flit.app.presentation.components.common.FlitChip
import com.flit.app.ui.theme.FlitTheme

/**
 * 검색 화면
 * FTS 기반 전체 캡처 검색 (실시간 결과)
 */
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onCaptureClick: (String) -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = FlitTheme.colors
    val focusRequester = remember { FocusRequester() }

    // 자동 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 에러 스낵바
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            // 헤더: 뒤로가기 + 검색 입력
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = colors.text
                    )
                }

                // 검색 입력 필드
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.card)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.searchText.isEmpty()) {
                                Text(
                                    text = "캡처 검색",
                                    color = colors.placeholder,
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = uiState.searchText,
                                onValueChange = viewModel::onSearchTextChanged,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = colors.text,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_input")
                                    .focusRequester(focusRequester)
                            )
                        }
                        if (uiState.searchText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchTextChanged("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "지우기",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // AI 시맨틱 검색 토글 — 프리미엄 구독자에게만 표시
                if (uiState.isPremium) {
                    FlitChip(
                        text = "AI",
                        selected = uiState.isSemanticMode,
                        onClick = { viewModel.toggleSemanticMode(!uiState.isSemanticMode) }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            // 분류 유형 필터 칩 (시맨틱 모드가 아닐 때만)
            if (!uiState.isSemanticMode) {
                FilterChipRow(
                    selectedType = uiState.selectedType,
                    onTypeSelected = { type -> viewModel.setTypeFilter(type) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 검색 결과
            if (uiState.isSemanticMode) {
                // 시맨틱 검색 결과
                when {
                    uiState.isSemanticLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.accent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    !uiState.hasSearched -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colors.textMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "AI로 의미 기반 검색",
                                    color = colors.textMuted,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                    uiState.semanticResults.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "결과 없음",
                                    color = colors.textMuted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("semantic_results_list"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.semanticResults,
                                key = { it.captureId }
                            ) { result ->
                                SemanticResultItem(
                                    result = result,
                                    onClick = { onCaptureClick(result.captureId) }
                                )
                            }
                        }
                    }
                }
            } else {
                // FTS 검색 결과
                when {
                    !uiState.hasSearched -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colors.textMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "캡처를 검색하세요",
                                    color = colors.textMuted,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                    uiState.searchResults.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "결과 없음",
                                    color = colors.textMuted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "다른 키워드로 검색해보세요",
                                    color = colors.textMuted.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("search_results_list"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.searchResults,
                                key = { it.id }
                            ) { capture ->
                                SearchResultItem(
                                    capture = capture,
                                    onClick = { onCaptureClick(capture.id) }
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
 * 검색 결과 아이템
 */
@Composable
private fun SearchResultItem(
    capture: Capture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val title = capture.aiTitle ?: capture.originalText.take(30)
    val preview = if (capture.aiTitle != null && capture.aiTitle != capture.originalText) {
        capture.originalText
    } else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 제목
                Text(
                    text = title,
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 분류 칩
                if (capture.classifiedType != ClassifiedType.TEMP) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (capture.classifiedType) {
                            ClassifiedType.TODO -> "할 일"
                            ClassifiedType.SCHEDULE -> "일정"
                            ClassifiedType.NOTES -> "노트"
                            ClassifiedType.TEMP -> ""
                        },
                        color = colors.chipText,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.chipBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 미리보기
            if (preview != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preview,
                    color = colors.textMuted,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 시맨틱 검색 결과 아이템
 */
@Composable
private fun SemanticResultItem(
    result: SemanticSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val scorePercent = (result.score * 100).toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.snippet.take(40),
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${scorePercent}%",
                    color = colors.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            if (result.snippet.length > 40) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.snippet,
                    color = colors.textMuted,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
