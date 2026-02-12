package com.flit.app.presentation.settings.calendar

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
import com.flit.app.domain.model.LocalCalendar
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.SectionHeader
import com.flit.app.presentation.settings.components.NavigationSettingItem
import com.flit.app.presentation.settings.components.SettingsCard
import com.flit.app.presentation.settings.components.SettingsDivider
import com.flit.app.presentation.settings.components.ToggleSettingItem
import com.flit.app.ui.theme.FlitTheme

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
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors
    val fontScale = 1f
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
            SectionHeader(
                title = "연동 캘린더",
                fontSize = 12.sp
            )

            SettingsCard {
                NavigationSettingItem(
                    title = "연동 캘린더",
                    description = uiState.availableCalendars
                        .firstOrNull { it.id == uiState.selectedCalendarId }
                        ?.displayName ?: "선택 필요",
                    fontScale = fontScale,
                    onClick = { showCalendarSheet = true }
                )

                if (uiState.availableCalendars.isEmpty()) {
                    SettingsDivider()
                    ActionItem(
                        title = "캘린더 목록 새로고침",
                        description = "기기 캘린더 목록을 다시 불러옵니다",
                        fontScale = fontScale,
                        onClick = { viewModel.reloadAvailableCalendars() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 일정 추가 설정 섹션
            SectionHeader(
                title = "일정 추가",
                fontSize = 12.sp
            )

            SettingsCard {
                ToggleSettingItem(
                    title = "자동 추가",
                    description = if (uiState.isAutoAddEnabled) "신뢰도 높은 일정을 자동으로 캘린더에 추가"
                        else "일정 추가 전 사용자 승인을 요청합니다",
                    isChecked = uiState.isAutoAddEnabled,
                    fontScale = fontScale,
                    onToggle = { viewModel.toggleAutoAdd(it) }
                )

                SettingsDivider()

                ToggleSettingItem(
                    title = "알림",
                    description = if (uiState.isAutoAddEnabled) "자동 추가된 일정을 알림으로 확인"
                        else "승인 대기 중인 일정이 있을 때 알림",
                    isChecked = uiState.isNotificationEnabled,
                    fontScale = fontScale,
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
                fontScale = fontScale,
                onSelect = { calendarId ->
                    viewModel.setTargetCalendar(calendarId)
                    showCalendarSheet = false
                }
            )
        }
    }
    }
}

// ---- 하위 Composable 컴포넌트 ----

@Composable
private fun ActionItem(
    title: String,
    description: String,
    fontScale: Float = 1f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
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
private fun CalendarSelectionSheet(
    calendars: List<LocalCalendar>,
    selectedId: Long?,
    fontScale: Float = 1f,
    onSelect: (Long) -> Unit
) {
    val colors = FlitTheme.colors
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
