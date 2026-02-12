package com.example.kairos_mobile.presentation.settings.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.LocalCalendar
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 캘린더 설정 세부 화면
 * - 연동 캘린더 선택 (BottomSheet)
 * - 자동 추가 토글 (auto/suggest 통합)
 * - 알림 토글
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsScreen(
    viewModel: CalendarSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    var showCalendarSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "캘린더 설정",
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

            // 연동 캘린더 섹션
            SectionHeader(title = "연동 캘린더")

            SettingsCard {
                NavigationSettingItem(
                    title = "연동 캘린더",
                    description = uiState.availableCalendars
                        .firstOrNull { it.id == uiState.selectedCalendarId }
                        ?.displayName ?: "선택 필요",
                    onClick = { showCalendarSheet = true }
                )

                if (uiState.availableCalendars.isEmpty()) {
                    SettingsDivider()
                    ActionItem(
                        title = "캘린더 목록 새로고침",
                        description = "기기 캘린더 목록을 다시 불러옵니다",
                        onClick = { viewModel.reloadAvailableCalendars() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 일정 추가 설정 섹션
            SectionHeader(title = "일정 추가")

            SettingsCard {
                ToggleSettingItem(
                    title = "자동 추가",
                    description = if (uiState.isAutoAddEnabled) "신뢰도 높은 일정을 자동으로 캘린더에 추가"
                        else "일정 추가 전 사용자 승인을 요청합니다",
                    isChecked = uiState.isAutoAddEnabled,
                    onToggle = { viewModel.toggleAutoAdd(it) }
                )

                SettingsDivider()

                ToggleSettingItem(
                    title = "알림",
                    description = if (uiState.isAutoAddEnabled) "자동 추가된 일정을 알림으로 확인"
                        else "승인 대기 중인 일정이 있을 때 알림",
                    isChecked = uiState.isNotificationEnabled,
                    onToggle = { viewModel.toggleNotification(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 캘린더 선택 바텀시트
    if (showCalendarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCalendarSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = colors.background
        ) {
            CalendarSelectionSheet(
                calendars = uiState.availableCalendars,
                selectedId = uiState.selectedCalendarId,
                onSelect = { calendarId ->
                    viewModel.setTargetCalendar(calendarId)
                    showCalendarSheet = false
                }
            )
        }
    }
}

// ---- 하위 Composable 컴포넌트 ----

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
            .border(0.5.dp, colors.borderLight, RoundedCornerShape(12.dp)),
        content = content
    )
}

@Composable
private fun NavigationSettingItem(
    title: String,
    description: String? = null,
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
                Text(text = it, color = colors.textMuted, fontSize = 14.sp)
            }
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

@Composable
private fun ActionItem(
    title: String,
    description: String,
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
            Text(text = title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = colors.textMuted, fontSize = 13.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

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
            Text(text = title, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, color = colors.textMuted, fontSize = 13.sp)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.9f),
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

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    val colors = KairosTheme.colors
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = colors.borderLight
    )
}

@Composable
private fun CalendarSelectionSheet(
    calendars: List<LocalCalendar>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    val colors = KairosTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "연동 캘린더 선택",
            color = colors.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (calendars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "사용 가능한 캘린더가 없습니다", color = colors.textMuted, fontSize = 14.sp)
            }
        } else {
            calendars.forEachIndexed { index, calendar ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(calendar.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(calendar.color))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = calendar.displayName, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(text = calendar.accountName, color = colors.textMuted, fontSize = 13.sp)
                    }
                    if (calendar.id == selectedId) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "선택됨",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (index < calendars.lastIndex) {
                    SettingsDivider()
                }
            }
        }
    }
}
