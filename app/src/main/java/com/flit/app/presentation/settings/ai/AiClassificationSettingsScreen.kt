package com.flit.app.presentation.settings.ai

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.flit.app.domain.model.ClassificationPreset
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.PremiumBadge
import com.flit.app.presentation.components.common.SectionHeader
import com.flit.app.presentation.settings.components.SettingsCard
import com.flit.app.presentation.subscription.PremiumGateSheet
import com.flit.app.ui.theme.FlitTheme

/**
 * AI 분류 설정 화면 - ViewModel 보유
 */
@Composable
fun AiClassificationSettingsScreen(
    viewModel: AiClassificationSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AiClassificationSettingsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToSubscription = onNavigateToSubscription,
        onSetPreset = viewModel::setPreset,
        onSetCustomInstruction = viewModel::setCustomInstruction,
        onSaveCustomInstruction = viewModel::saveCustomInstruction
    )
}

/**
 * AI 분류 설정 컨텐츠 - UI만 담당
 * - 분류 프리셋 드롭다운
 * - 분류 규칙 (커스텀 인스트럭션) 텍스트필드
 * - 비프리미엄 시 PremiumGateSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiClassificationSettingsContent(
    uiState: AiClassificationSettingsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onSetPreset: (String) -> Unit,
    onSetCustomInstruction: (String) -> Unit,
    onSaveCustomInstruction: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppFontScaleProvider {
    val colors = FlitTheme.colors
    val isPremium = uiState.subscriptionTier == SubscriptionTier.PREMIUM
    var showPresetDropdown by remember { mutableStateOf(false) }
    var showPremiumGateSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI 분류 설정",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(
                title = "분류 프리셋",
                fontSize = 12.sp
            )

            SettingsCard {
                // 프리셋 드롭다운
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable {
                                if (isPremium) {
                                    showPresetDropdown = true
                                } else {
                                    showPremiumGateSheet = true
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "분류 프리셋",
                                    color = colors.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    PremiumBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.presets.find { it.id == uiState.selectedPresetId }?.name ?: "기본",
                                color = colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showPresetDropdown,
                        onDismissRequest = { showPresetDropdown = false }
                    ) {
                        uiState.presets.forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = preset.name,
                                            fontWeight = if (preset.id == uiState.selectedPresetId) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = preset.description,
                                            fontSize = 12.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                },
                                onClick = {
                                    onSetPreset(preset.id)
                                    showPresetDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "분류 규칙",
                fontSize = 12.sp
            )

            SettingsCard {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "분류 규칙",
                                color = colors.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (!isPremium) {
                                Spacer(modifier = Modifier.width(8.dp))
                                PremiumBadge()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.customInstruction,
                            onValueChange = onSetCustomInstruction,
                            placeholder = {
                                Text(
                                    "예: 업무 관련 내용은 일정으로 분류",
                                    color = colors.placeholder,
                                    fontSize = 14.sp
                                )
                            },
                            enabled = isPremium,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text,
                                disabledTextColor = colors.textMuted,
                                disabledBorderColor = colors.borderLight,
                                cursorColor = colors.accent
                            )
                        )
                        if (isPremium && uiState.customInstruction.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = onSaveCustomInstruction,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("저장", color = colors.accent, fontSize = 14.sp)
                            }
                        }
                    }
                    // 비프리미엄 시 터치 가로채기
                    if (!isPremium) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showPremiumGateSheet = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPremiumGateSheet) {
        PremiumGateSheet(
            featureName = "AI 분류 설정",
            onUpgrade = {
                showPremiumGateSheet = false
                onNavigateToSubscription()
            },
            onDismiss = { showPremiumGateSheet = false }
        )
    }
    }
}

/**
 * AI 분류 설정 화면 Preview
 */
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AiClassificationSettingsContentPreview() {
    FlitTheme {
        AiClassificationSettingsContent(
            uiState = AiClassificationSettingsUiState(
                presets = listOf(
                    ClassificationPreset(
                        id = "default",
                        name = "기본",
                        description = "일반적인 분류 규칙"
                    ),
                    ClassificationPreset(
                        id = "work",
                        name = "업무",
                        description = "업무 중심 분류"
                    ),
                    ClassificationPreset(
                        id = "personal",
                        name = "개인",
                        description = "개인 생활 중심 분류"
                    )
                ),
                selectedPresetId = "default",
                customInstruction = "",
                subscriptionTier = SubscriptionTier.PREMIUM
            ),
            onNavigateBack = {},
            onNavigateToSubscription = {},
            onSetPreset = {},
            onSetCustomInstruction = {},
            onSaveCustomInstruction = {}
        )
    }
}
