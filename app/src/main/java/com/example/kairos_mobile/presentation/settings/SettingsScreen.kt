package com.example.kairos_mobile.presentation.settings

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.kairos_mobile.BuildConfig
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.presentation.components.common.PremiumBadge
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.presentation.subscription.PremiumGateSheet
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
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    var showPresetDropdown by remember { mutableStateOf(false) }
    var showPremiumGateSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var premiumGateFeatureName by remember { mutableStateOf("AI 분류 설정") }
    val isPremiumSubscriber = uiState.subscriptionTier == SubscriptionTier.PREMIUM
    val snackbarHostState = remember { SnackbarHostState() }
    var showCalendarDropdown by remember { mutableStateOf(false) }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val readGranted = result[Manifest.permission.READ_CALENDAR] == true
        val writeGranted = result[Manifest.permission.WRITE_CALENDAR] == true
        viewModel.onCalendarPermissionResult(readGranted && writeGranted)
    }

    // 캘린더 인증 메시지 Snackbar 표시
    LaunchedEffect(uiState.calendarAuthMessage) {
        uiState.calendarAuthMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissCalendarAuthMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshCalendarPermissionState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "설정",
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

            // 화면 섹션 (테마 + 글씨 크기 통합)
            SectionHeader(title = "화면")

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

                // 굵은 구분선 (테마 ↔ 글씨 크기)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = colors.border
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThemeOptionItem(
                    title = "작게",
                    isSelected = uiState.captureFontSize == "SMALL",
                    onClick = { viewModel.setCaptureFontSize("SMALL") }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "보통",
                    isSelected = uiState.captureFontSize == "MEDIUM",
                    onClick = { viewModel.setCaptureFontSize("MEDIUM") }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "크게",
                    isSelected = uiState.captureFontSize == "LARGE",
                    onClick = { viewModel.setCaptureFontSize("LARGE") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 캘린더 섹션
            SectionHeader(title = "캘린더 연동")

            SettingsCard {
                ToggleSettingItem(
                    title = "캘린더 연동",
                    description = "일정을 기기 캘린더에 동기화",
                    isChecked = uiState.isCalendarEnabled,
                    onToggle = { enabled ->
                        if (enabled && !uiState.isCalendarPermissionGranted) {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        } else {
                            viewModel.toggleCalendar(enabled)
                        }
                    }
                )

                if (!uiState.isCalendarPermissionGranted) {
                    SettingsDivider()
                    CalendarActionItem(
                        title = "권한 요청",
                        description = "캘린더 연동을 위해 권한을 허용해주세요",
                        enabled = true,
                        onClick = {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        }
                    )
                } else if (uiState.isCalendarEnabled) {
                    SettingsDivider()

                    Box {
                        NavigationSettingItem(
                            title = "연동 캘린더",
                            description = uiState.availableCalendars
                                .firstOrNull { it.id == uiState.selectedCalendarId }
                                ?.displayName ?: "선택 필요",
                            onClick = { showCalendarDropdown = true }
                        )

                        DropdownMenu(
                            expanded = showCalendarDropdown,
                            onDismissRequest = { showCalendarDropdown = false }
                        ) {
                            if (uiState.availableCalendars.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("사용 가능한 캘린더가 없습니다.") },
                                    onClick = { showCalendarDropdown = false }
                                )
                            } else {
                                uiState.availableCalendars.forEach { calendar ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (calendar.isPrimary) {
                                                    "${calendar.displayName} (기본)"
                                                } else {
                                                    calendar.displayName
                                                }
                                            )
                                        },
                                        onClick = {
                                            viewModel.setTargetCalendar(calendar.id)
                                            showCalendarDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.availableCalendars.isEmpty()) {
                        SettingsDivider()
                        CalendarActionItem(
                            title = "캘린더 목록 새로고침",
                            description = "기기 캘린더 목록을 다시 불러옵니다",
                            enabled = true,
                            onClick = { viewModel.reloadAvailableCalendars() }
                        )
                    }

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

                    SettingsDivider()

                    ToggleSettingItem(
                        title = "알림",
                        description = "일정 추가 확인 및 제안 알림",
                        isChecked = uiState.isNotificationEnabled,
                        onToggle = { viewModel.toggleNotification(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI 분류 섹션
            SectionHeader(title = "AI 분류")

            SettingsCard {
                val isPremium = isPremiumSubscriber

                // 분류 프리셋 드롭다운
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isPremium) {
                                    showPresetDropdown = true
                                } else {
                                    premiumGateFeatureName = "AI 분류 설정"
                                    showPremiumGateSheet = true
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "분류 프리셋",
                                    color = colors.text,
                                    fontSize = 15.sp,
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
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 프리셋 드롭다운 메뉴
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
                                    viewModel.setPreset(preset.id)
                                    showPresetDropdown = false
                                }
                            )
                        }
                    }
                }

                SettingsDivider()

                // 분류 규칙 (커스텀 인스트럭션)
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "분류 규칙",
                                color = colors.text,
                                fontSize = 15.sp,
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
                            onValueChange = { viewModel.setCustomInstruction(it) },
                            placeholder = { Text("예: 업무 관련 내용은 일정으로 분류", color = colors.placeholder) },
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
                                onClick = { viewModel.saveCustomInstruction() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("저장", color = colors.accent, fontSize = 14.sp)
                            }
                        }
                    }
                    // 비프리미엄 시 TextField 터치를 가로채는 투명 오버레이
                    if (!isPremium) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    premiumGateFeatureName = "AI 분류 설정"
                                    showPremiumGateSheet = true
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 계정 섹션
            SectionHeader(title = "계정")

            SettingsCard {
                if (uiState.user != null) {
                    // 로그인 상태: 이메일 + 로그아웃
                    NavigationSettingItem(
                        title = uiState.user!!.email,
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsDivider()
                    NavigationSettingItem(
                        title = "로그아웃",
                        onClick = { showLogoutDialog = true }
                    )
                } else {
                    // 미로그인 상태
                    NavigationSettingItem(
                        title = "로그인",
                        onClick = onNavigateToLogin
                    )
                }
                SettingsDivider()
                NavigationSettingItem(
                    title = "구독 관리",
                    onClick = onNavigateToSubscription
                )
                SettingsDivider()
                NavigationSettingItem(
                    title = "사용 분석",
                    description = "캡처 통계 및 분류 현황",
                    onClick = {
                        if (isPremiumSubscriber) {
                            onNavigateToAnalytics()
                        } else {
                            premiumGateFeatureName = "사용 분석"
                            showPremiumGateSheet = true
                        }
                    }
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
                    description = BuildConfig.VERSION_NAME,
                    showArrow = false,
                    onClick = { }
                )
            }

            // 개발자 도구 (디버그 빌드에서만 노출)
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader(title = "개발자 도구")

                SettingsCard {
                    DebugImageUploadItem(
                        isSubmitting = uiState.debugSubmitting,
                        result = uiState.debugResult,
                        onImageSelected = { uri -> viewModel.debugSubmitImage(uri) },
                        onDismissResult = { viewModel.dismissDebugResult() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Premium 게이트 시트
    if (showPremiumGateSheet) {
        PremiumGateSheet(
            featureName = premiumGateFeatureName,
            onUpgrade = {
                showPremiumGateSheet = false
                onNavigateToSubscription()
            },
            onDismiss = { showPremiumGateSheet = false }
        )
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        val dialogColors = KairosTheme.colors
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = dialogColors.card,
            titleContentColor = dialogColors.text,
            textContentColor = dialogColors.text,
            title = { Text("로그아웃", color = dialogColors.text) },
            text = { Text("로그아웃하시겠습니까?", color = dialogColors.text) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                }) { Text("로그아웃", color = dialogColors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소", color = dialogColors.textSecondary)
                }
            }
        )
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
private fun CalendarActionItem(
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = KairosTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) colors.text else colors.textMuted,
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

        if (enabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.textMuted
            )
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

/**
 * 디버그: 이미지 업로드 테스트 아이템
 * 갤러리에서 이미지를 선택하면 캡처로 제출
 */
@Composable
private fun DebugImageUploadItem(
    isSubmitting: Boolean,
    result: String?,
    onImageSelected: (Uri) -> Unit,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSubmitting) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "이미지 캡처 테스트",
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "갤러리에서 이미지를 선택하여 캡처로 제출",
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }

            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.textMuted
                )
            }
        }

        // 결과 표시
        if (result != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result,
                color = if (result.startsWith("실패")) colors.danger else colors.success,
                fontSize = 12.sp
            )
        }
    }
}
