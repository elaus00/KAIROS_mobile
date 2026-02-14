package com.flit.app.presentation.notes.reorganize

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import com.flit.app.ui.theme.FlitTheme

/**
 * 노트 AI 재구성 화면 - ViewModel 보유
 */
@Composable
fun ReorganizeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReorganizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ReorganizeContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onApply = viewModel::onApply,
        onRetry = viewModel::onRetry
    )
}

/**
 * 노트 AI 재구성 컨텐츠 - UI만 담당
 * Before/After 2컬럼 비교: 현재 매핑 vs AI 제안 매핑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorganizeContent(
    uiState: ReorganizeUiState,
    onNavigateBack: () -> Unit,
    onApply: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppFontScaleProvider {
        val colors = FlitTheme.colors

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AI 재구성",
                            color = colors.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = colors.text
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            },
            containerColor = colors.background
        ) { padding ->
            val error = uiState.error
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = colors.accent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "AI가 노트를 분석 중입니다...",
                                color = colors.textMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error,
                                color = colors.textMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = onRetry) {
                                Text(
                                    text = "다시 시도",
                                    color = colors.accent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // 컬럼 헤더
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "현재",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(32.dp))
                            Text(
                                text = "AI 제안",
                                color = colors.accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 재구성 제안 목록
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.proposals,
                                key = { it.folderName }
                            ) { proposal ->
                                val actionLabel = when (proposal.action) {
                                    "CREATE" -> "새 폴더"
                                    "MOVE" -> "이동"
                                    else -> proposal.action ?: "이동"
                                }
                                MoveComparisonRow(
                                    noteTitle = "${proposal.captureIds.size}개 노트",
                                    currentFolder = actionLabel,
                                    suggestedFolder = proposal.folderName
                                )
                            }
                        }

                        // 적용 버튼
                        if (uiState.proposals.isNotEmpty()) {
                            Button(
                                onClick = onApply,
                                enabled = !uiState.isApplying,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent
                                )
                            ) {
                                if (uiState.isApplying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.text
                                    )
                                } else {
                                    Text(
                                        text = "제안 적용 (${uiState.proposals.size}건)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
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
 * Before/After 비교 Row
 */
@Composable
private fun MoveComparisonRow(
    noteTitle: String,
    currentFolder: String,
    suggestedFolder: String,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(0.5.dp, colors.borderLight, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // 노트 제목
        Text(
            text = noteTitle,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Before → After
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 현재 폴더
            Text(
                text = currentFolder,
                color = colors.textMuted,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp)
            )

            // AI 제안 폴더
            Text(
                text = suggestedFolder,
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 노트 AI 재구성 화면 Preview
 */
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReorganizeContentPreview() {
    FlitTheme {
        ReorganizeContent(
            uiState = ReorganizeUiState(
                isLoading = false,
                isApplying = false,
                proposals = listOf(
                    ProposedItem(
                        folderName = "업무",
                        folderType = "CUSTOM",
                        action = "새 폴더",
                        captureIds = listOf("1", "2", "3")
                    ),
                    ProposedItem(
                        folderName = "개인",
                        folderType = "CUSTOM",
                        action = "이동",
                        captureIds = listOf("4", "5")
                    )
                ),
                error = null
            ),
            onNavigateBack = {},
            onApply = {},
            onRetry = {}
        )
    }
}
