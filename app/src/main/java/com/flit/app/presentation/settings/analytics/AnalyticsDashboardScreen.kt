package com.flit.app.presentation.settings.analytics

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flit.app.domain.model.AnalyticsDashboard
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.ui.theme.FlitTheme

/** 분석 대시보드 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsDashboardContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::loadDashboard,
        onErrorDismissed = viewModel::onErrorDismissed
    )
}

/** 분석 대시보드 화면 Content */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardContent(
    uiState: AnalyticsDashboardUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppFontScaleProvider {
    val colors = FlitTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "분석",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.accent)
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "", color = colors.textMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onRetry) {
                            Text("다시 시도", color = colors.accent, fontSize = 14.sp)
                        }
                    }
                }
            }
            uiState.dashboard != null -> {
                DashboardContent(
                    dashboard = uiState.dashboard!!,
                    modifier = modifier.padding(paddingValues)
                )
            }
        }
    }
    }
}

/** 대시보드 컨텐츠 */
@Composable
private fun DashboardContent(
    dashboard: AnalyticsDashboard,
    fontScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 요약 카드
        StatCard(
            title = "총 캡처",
            value = dashboard.totalCaptures.toString(),
            fontScale = fontScale,
            modifier = Modifier.fillMaxWidth()
        )

        // 유형별 분포
        Text(
            text = "유형별 분포",
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        dashboard.capturesByType.forEach { (type, count) ->
            val label = when (type) {
                "TODO" -> "할 일"
                "SCHEDULE" -> "일정"
                "NOTES" -> "노트"
                "TEMP" -> "미분류"
                else -> type
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.card)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = colors.text, fontSize = 14.sp)
                Text("${count}건", color = colors.textMuted, fontSize = 14.sp)
            }
        }

        // 평균 분류 시간
        StatCard(
            title = "평균 분류 시간",
            value = "${dashboard.avgClassificationTimeMs}ms",
            fontScale = fontScale
        )

        // 인기 태그
        if (dashboard.topTags.isNotEmpty()) {
            Text(
                text = "인기 태그",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            dashboard.topTags.take(5).forEach { (tag, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.card)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("#$tag", color = colors.accent, fontSize = 14.sp)
                    Text("${count}회", color = colors.textMuted, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/** 통계 카드 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    fontScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(16.dp)
    ) {
        Text(title, color = colors.textMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnalyticsDashboardContentPreview() {
    FlitTheme {
        AnalyticsDashboardContent(
            uiState = AnalyticsDashboardUiState(),
            onNavigateBack = {},
            onRetry = {},
            onErrorDismissed = {}
        )
    }
}
