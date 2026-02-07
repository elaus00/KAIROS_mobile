package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * SettingsScreen
 * 설정 화면 (다크모드 3옵션 + 개인정보처리방침 + 이용약관 + 앱 버전)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onNavigateToTrash: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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

            // 테마 섹션
            SectionHeader(title = "테마")

            SettingsCard {
                // 다크모드 3옵션
                ThemeOptionItem(
                    title = "시스템 설정",
                    description = "기기 설정에 따름",
                    isSelected = uiState.themePreference == ThemePreference.SYSTEM,
                    onClick = { viewModel.setTheme(ThemePreference.SYSTEM) }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "라이트 모드",
                    isSelected = uiState.themePreference == ThemePreference.LIGHT,
                    onClick = { viewModel.setTheme(ThemePreference.LIGHT) }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "다크 모드",
                    isSelected = uiState.themePreference == ThemePreference.DARK,
                    onClick = { viewModel.setTheme(ThemePreference.DARK) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 캘린더 섹션
            SectionHeader(title = "Google Calendar")

            SettingsCard {
                ToggleSettingItem(
                    title = "Google Calendar 연동",
                    description = "일정을 Google Calendar에 동기화",
                    isChecked = uiState.isCalendarEnabled,
                    onToggle = { viewModel.toggleCalendar(it) }
                )

                if (uiState.isCalendarEnabled) {
                    SettingsDivider()

                    ThemeOptionItem(
                        title = "자동 추가",
                        description = "신뢰도 높은 일정은 자동 추가",
                        isSelected = uiState.calendarMode == "auto",
                        onClick = { viewModel.setCalendarMode("auto") }
                    )

                    SettingsDivider()

                    ThemeOptionItem(
                        title = "제안 모드",
                        description = "일정 추가 전 승인 요청",
                        isSelected = uiState.calendarMode == "suggest",
                        onClick = { viewModel.setCalendarMode("suggest") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 알림 섹션
            SectionHeader(title = "알림")

            SettingsCard {
                ToggleSettingItem(
                    title = "알림",
                    description = "일정 추가 확인 및 제안 알림",
                    isChecked = uiState.isNotificationEnabled,
                    onToggle = { viewModel.toggleNotification(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 정보 섹션
            SectionHeader(title = "정보")

            SettingsCard {
                NavigationSettingItem(
                    title = "개인정보 처리방침",
                    onClick = onNavigateToPrivacyPolicy
                )

                SettingsDivider()

                NavigationSettingItem(
                    title = "이용약관",
                    onClick = onNavigateToTermsOfService
                )

                SettingsDivider()

                NavigationSettingItem(
                    title = "앱 버전",
                    description = "1.0.0",
                    showArrow = false,
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 테마 옵션 아이템 (라디오 버튼 스타일)
 */
@Composable
private fun ThemeOptionItem(
    title: String,
    description: String? = null,
    isSelected: Boolean,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 설정 카드 컨테이너 (외곽선 포함)
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
            .background(colors.card)
            .border(
                width = 0.5.dp,
                color = colors.borderLight,
                shape = RoundedCornerShape(12.dp)
            ),
        content = content
    )
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
 * 토글 설정 아이템 (Switch)
 */
@Composable
private fun ToggleSettingItem(
    title: String,
    description: String? = null,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
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
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.85f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.accent.copy(alpha = 0.3f),
                checkedBorderColor = colors.accent.copy(alpha = 0.3f),
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.borderLight,
                uncheckedBorderColor = colors.border
            )
        )
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
