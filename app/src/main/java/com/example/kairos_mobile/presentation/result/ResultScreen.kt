package com.example.kairos_mobile.presentation.result

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.presentation.result.components.AutoSaveResultCard
import com.example.kairos_mobile.presentation.result.components.ConfirmResultCard
import com.example.kairos_mobile.presentation.result.components.ResultEditBottomSheet
import com.example.kairos_mobile.presentation.result.components.TypeSelectionCard
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 결과 화면 (PRD v4.0)
 * 신뢰도 기반 UI 분기: 자동저장/확인/선택 모드
 */
@Composable
fun ResultScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = KairosTheme.colors

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResultEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // 헤더
            ResultHeader(onBackClick = onNavigateBack)

            Spacer(modifier = Modifier.height(24.dp))

            // 메인 콘텐츠
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "resultContent"
                ) { isLoading ->
                    when {
                        isLoading -> {
                            // 로딩 상태
                            CircularProgressIndicator(
                                color = colors.accent,
                                strokeWidth = 3.dp
                            )
                        }

                        uiState.isSaved -> {
                            // 저장 완료 상태
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "✓",
                                    fontSize = 48.sp,
                                    color = colors.success
                                )
                                Text(
                                    text = "저장되었습니다",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.text
                                )
                            }
                        }

                        else -> {
                            // 신뢰도 기반 UI 분기
                            ResultContent(
                                uiState = uiState,
                                onStopAutoSave = viewModel::onStopAutoSave,
                                onEnterEditMode = viewModel::onEnterEditMode,
                                onTypeSelected = viewModel::onTypeSelected,
                                onConfirmSave = viewModel::onConfirmSave
                            )
                        }
                    }
                }
            }
        }

        // 수정 바텀시트
        if (uiState.isEditMode) {
            ResultEditBottomSheet(
                currentType = uiState.currentType,
                currentTitle = uiState.currentTitle,
                currentTags = uiState.currentTags,
                onTypeChanged = viewModel::onTypeSelected,
                onTitleChanged = viewModel::onTitleChanged,
                onTagAdded = viewModel::onTagAdded,
                onTagRemoved = viewModel::onTagRemoved,
                onSave = viewModel::onConfirmSave,
                onDismiss = viewModel::onExitEditMode,
                isSaving = uiState.isSaving
            )
        }
    }
}

/**
 * 결과 헤더
 */
@Composable
private fun ResultHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = colors.text
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "분류 결과",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text
        )
    }
}

/**
 * 신뢰도 기반 결과 콘텐츠
 */
@Composable
private fun ResultContent(
    uiState: ResultUiState,
    onStopAutoSave: () -> Unit,
    onEnterEditMode: () -> Unit,
    onTypeSelected: (com.example.kairos_mobile.domain.model.CaptureType) -> Unit,
    onConfirmSave: () -> Unit
) {
    val classification = uiState.classification

    when (uiState.confidenceLevel) {
        ConfidenceLevel.HIGH -> {
            // 95% 이상: 자동저장 모드
            if (classification != null && uiState.isAutoSaveActive) {
                AutoSaveResultCard(
                    classification = classification,
                    content = uiState.content,
                    progress = uiState.autoSaveProgress,
                    countdown = uiState.autoSaveCountdown,
                    onEdit = onStopAutoSave
                )
            } else if (classification != null) {
                // 자동저장 중지됨 → 확인 모드로 전환
                ConfirmResultCard(
                    classification = classification,
                    content = uiState.content,
                    onConfirm = onConfirmSave,
                    onEdit = onEnterEditMode,
                    isSaving = uiState.isSaving
                )
            }
        }

        ConfidenceLevel.MEDIUM -> {
            // 80-95%: 확인 모드
            if (classification != null) {
                ConfirmResultCard(
                    classification = classification,
                    content = uiState.content,
                    onConfirm = onConfirmSave,
                    onEdit = onEnterEditMode,
                    isSaving = uiState.isSaving
                )
            }
        }

        ConfidenceLevel.LOW -> {
            // 80% 미만: 선택 모드
            TypeSelectionCard(
                content = uiState.content,
                selectedType = uiState.editedType,
                onTypeSelected = onTypeSelected,
                onConfirm = onConfirmSave,
                isSaving = uiState.isSaving
            )
        }
    }
}
