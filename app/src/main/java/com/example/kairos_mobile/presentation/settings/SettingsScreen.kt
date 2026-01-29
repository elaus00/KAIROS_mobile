package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
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

            // 프로필 섹션
            ProfileSection(
                userName = "사용자",
                userEmail = "user@email.com",
                onClick = { /* TODO: 프로필 상세 */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 연동 섹션
            SectionHeader(title = "연동")

            SettingsCard {
                // Google Calendar
                IntegrationSettingItem(
                    title = "Google Calendar",
                    isConnected = uiState.googleCalendarConnected,
                    onToggle = { viewModel.toggleGoogleCalendar() }
                )

                SettingsDivider()

                // Obsidian
                IntegrationSettingItem(
                    title = "Obsidian",
                    isConnected = uiState.obsidianConnected,
                    onToggle = { viewModel.toggleObsidian() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 화면 설정 섹션
            SectionHeader(title = "화면")

            SettingsCard {
                // 다크 모드
                SwitchSettingItem(
                    title = "다크 모드",
                    checked = uiState.themePreference == ThemePreference.DARK,
                    onCheckedChange = viewModel::toggleDarkMode
                )

                SettingsDivider()

                // 노트 보기 방식
                ViewModeSettingItem(
                    selectedMode = uiState.viewMode,
                    onModeSelected = viewModel::setViewMode
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI 설정 섹션
            SectionHeader(title = "AI 설정")

            SettingsCard {
                // 자동 분류
                SwitchSettingItem(
                    title = "자동 분류",
                    description = "입력 내용을 자동으로 분류",
                    checked = uiState.autoClassifyEnabled,
                    onCheckedChange = viewModel::toggleAutoClassify
                )

                SettingsDivider()

                // 스마트 배치
                SwitchSettingItem(
                    title = "스마트 배치",
                    description = "할 일 시간대 자동 추천",
                    checked = uiState.smartScheduleEnabled,
                    onCheckedChange = viewModel::toggleSmartSchedule
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
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 프로필 섹션
 */
@Composable
private fun ProfileSection(
    userName: String,
    userEmail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 아이콘
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.chipBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 사용자 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName,
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = userEmail,
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        // 화살표
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
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
 * 연동 설정 아이템
 */
@Composable
private fun IntegrationSettingItem(
    title: String,
    isConnected: Boolean,
    onToggle: () -> Unit,
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
        Column {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isConnected) "연동됨" else "연동 안됨",
                color = if (isConnected) colors.success else colors.textSecondary,
                fontSize = 13.sp
            )
        }

        // 연동/해제 버튼
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) colors.chipBg else colors.accent,
                contentColor = if (isConnected) colors.textSecondary else colors.card
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = if (isConnected) "해제" else "연동",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 스위치 설정 아이템
 */
@Composable
private fun SwitchSettingItem(
    title: String,
    description: String? = null,
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
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }
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
 * 노트 보기 방식 설정 아이템
 */
@Composable
private fun ViewModeSettingItem(
    selectedMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "노트 보기 방식",
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 버튼 그룹 (크기 축소)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Button(
                    onClick = { onModeSelected(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) colors.accent else colors.chipBg,
                        contentColor = if (isSelected) colors.card else colors.textSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        text = when (mode) {
                            ViewMode.DETAIL -> "자세히"
                            ViewMode.GRID -> "그리드"
                            ViewMode.LIST -> "리스트"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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
