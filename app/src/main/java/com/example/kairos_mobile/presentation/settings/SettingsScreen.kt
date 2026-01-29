package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * SettingsScreen (PRD v4.0)
 * 미니멀 모노크롬 디자인 설정 화면
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SettingsEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            KairosBottomNav(
                selectedTab = KairosTab.SETTINGS,
                onTabSelected = { tab ->
                    onNavigate(tab.route)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .verticalScroll(rememberScrollState())
        ) {
            // 헤더
            Text(
                text = "Settings",
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI 기능 섹션
            SectionHeader(title = "AI 기능")

            SettingsCard {
                // 자동 요약
                SwitchSettingItem(
                    title = "자동 요약",
                    description = "긴 콘텐츠를 AI가 자동으로 요약합니다",
                    checked = uiState.autoSummarizeEnabled,
                    onCheckedChange = viewModel::toggleAutoSummarize
                )

                SettingsDivider()

                // 스마트 태그 제안
                SwitchSettingItem(
                    title = "스마트 태그 제안",
                    description = "과거 패턴을 학습하여 태그를 자동 제안합니다",
                    checked = uiState.smartTagsEnabled,
                    onCheckedChange = viewModel::toggleSmartTags
                )

                SettingsDivider()

                // 자동 분류
                SwitchSettingItem(
                    title = "자동 분류",
                    description = "캡처 내용을 AI가 자동으로 분류합니다",
                    checked = uiState.autoClassifyEnabled,
                    onCheckedChange = viewModel::toggleAutoClassify
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 화면 설정 섹션
            SectionHeader(title = "화면")

            SettingsCard {
                // 다크 모드
                SwitchSettingItem(
                    title = "다크 모드",
                    description = "어두운 테마를 사용합니다",
                    checked = uiState.themePreference == ThemePreference.DARK,
                    onCheckedChange = viewModel::toggleDarkMode
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 연동 섹션
            SectionHeader(title = "연동")

            SettingsCard {
                // Google Calendar
                NavigationSettingItem(
                    title = "Google Calendar",
                    description = if (uiState.googleCalendarConnected) "연결됨" else "연결 안됨",
                    onClick = { /* TODO: Google Calendar 연동 */ }
                )

                SettingsDivider()

                // Obsidian
                NavigationSettingItem(
                    title = "Obsidian",
                    description = if (uiState.obsidianConnected) "연결됨" else "연결 안됨",
                    onClick = { /* TODO: Obsidian 연동 */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 정보 섹션
            SectionHeader(title = "정보")

            SettingsCard {
                NavigationSettingItem(
                    title = "버전",
                    description = "1.0.0",
                    showArrow = false,
                    onClick = { }
                )

                SettingsDivider()

                NavigationSettingItem(
                    title = "개인정보 처리방침",
                    onClick = { /* TODO: 개인정보 처리방침 */ }
                )

                SettingsDivider()

                NavigationSettingItem(
                    title = "이용약관",
                    onClick = { /* TODO: 이용약관 */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 앱 정보
            Text(
                text = "KAIROS v1.0",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * 설정 카드 컨테이너
 */
@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = KairosTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card),
        content = content
    )
}

/**
 * 스위치 설정 아이템
 */
@Composable
private fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = colors.textMuted,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (colors.isDark) colors.background else colors.card,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.card,
                uncheckedTrackColor = colors.chipBg
            )
        )
    }
}

/**
 * 네비게이션 설정 아이템
 */
@Composable
private fun NavigationSettingItem(
    title: String,
    description: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            description?.let {
                Text(
                    text = it,
                    color = colors.textMuted,
                    fontSize = 14.sp
                )
            }

            if (showArrow) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 설정 구분선
 */
@Composable
private fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = colors.borderLight
    )
}
