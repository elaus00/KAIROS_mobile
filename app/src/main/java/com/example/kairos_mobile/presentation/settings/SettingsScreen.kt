package com.example.kairos_mobile.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.BuildConfig
import com.example.kairos_mobile.data.remote.oauth.GoogleOAuthUrlBuilder
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var showExchangeDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }
    var showPresetDropdown by remember { mutableStateOf(false) }
    var showPremiumGateSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var premiumGateFeatureName by remember { mutableStateOf("AI 분류 설정") }
    val isPremiumSubscriber = uiState.subscriptionTier == SubscriptionTier.PREMIUM
    val snackbarHostState = remember { SnackbarHostState() }

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
            SectionHeader(title = "Google Calendar")

            SettingsCard {
                ToggleSettingItem(
                    title = "Google Calendar 연동",
                    description = "일정을 Google Calendar에 동기화",
                    isChecked = uiState.isCalendarEnabled,
                    onToggle = { viewModel.toggleCalendar(it) }
                )

                SettingsDivider()

                CalendarActionItem(
                    title = "Google OAuth 시작 (자동)",
                    description = "브라우저 인증 후 앱으로 자동 복귀",
                    enabled = !uiState.calendarAuthLoading,
                    onClick = {
                        val oauthUrl = GoogleOAuthUrlBuilder.buildAuthorizationUrl(
                            clientId = BuildConfig.GOOGLE_OAUTH_CLIENT_ID
                        )
                        if (oauthUrl.isNullOrBlank()) {
                            viewModel.showCalendarAuthMessage("GOOGLE_OAUTH_CLIENT_ID를 설정해야 합니다.")
                        } else {
                            CustomTabsIntent.Builder().build()
                                .launchUrl(context, Uri.parse(oauthUrl))
                        }
                    }
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

                    // 알림 (캘린더 활성화 시에만 의미 있음)
                    SettingsDivider()

                    ToggleSettingItem(
                        title = "알림",
                        description = "일정 추가 확인 및 제안 알림",
                        isChecked = uiState.isNotificationEnabled,
                        onToggle = { viewModel.toggleNotification(it) }
                    )

                    // 디버그 전용 항목
                    if (BuildConfig.DEBUG) {
                        SettingsDivider()

                        CalendarActionItem(
                            title = "OAuth code 교환",
                            description = "calendar/token/exchange 호출",
                            enabled = !uiState.calendarAuthLoading,
                            onClick = { showExchangeDialog = true }
                        )

                        SettingsDivider()

                        CalendarActionItem(
                            title = "토큰 직접 저장",
                            description = "calendar/token 호출",
                            enabled = !uiState.calendarAuthLoading,
                            onClick = { showTokenDialog = true }
                        )

                        SettingsDivider()

                        CalendarActionItem(
                            title = "이벤트 조회 테스트",
                            description = "calendar/events 조회",
                            enabled = !uiState.calendarAuthLoading,
                            onClick = { viewModel.fetchCalendarEventsPreview() }
                        )
                    }
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

    if (showExchangeDialog) {
        CalendarCodeExchangeDialog(
            loading = uiState.calendarAuthLoading,
            onDismiss = { showExchangeDialog = false },
            onSubmit = { code, redirectUri ->
                viewModel.exchangeCalendarCode(code, redirectUri)
                showExchangeDialog = false
            }
        )
    }

    if (showTokenDialog) {
        CalendarTokenSaveDialog(
            loading = uiState.calendarAuthLoading,
            onDismiss = { showTokenDialog = false },
            onSubmit = { accessToken, refreshToken, expiresIn ->
                viewModel.saveCalendarToken(accessToken, refreshToken, expiresIn)
                showTokenDialog = false
            }
        )
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

@Composable
private fun CalendarCodeExchangeDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (code: String, redirectUri: String) -> Unit
) {
    val colors = KairosTheme.colors
    var code by remember { mutableStateOf("") }
    var redirectUri by remember { mutableStateOf("com.kairos.app:/oauth2redirect") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = { Text("OAuth Code 교환", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("authorization code") },
                    singleLine = true,
                    enabled = !loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedLabelColor = colors.accent,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.accent
                    )
                )
                OutlinedTextField(
                    value = redirectUri,
                    onValueChange = { redirectUri = it },
                    label = { Text("redirect_uri") },
                    singleLine = true,
                    enabled = !loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedLabelColor = colors.accent,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.accent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = { onSubmit(code, redirectUri) }
            ) { Text("요청", color = colors.accent) }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("취소", color = colors.textSecondary) }
        }
    )
}

@Composable
private fun CalendarTokenSaveDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (accessToken: String, refreshToken: String, expiresIn: String) -> Unit
) {
    val colors = KairosTheme.colors
    var accessToken by remember { mutableStateOf("") }
    var refreshToken by remember { mutableStateOf("") }
    var expiresIn by remember { mutableStateOf("") }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.border,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = { Text("토큰 직접 저장", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = { Text("access_token") },
                    singleLine = true,
                    enabled = !loading,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = refreshToken,
                    onValueChange = { refreshToken = it },
                    label = { Text("refresh_token (optional)") },
                    singleLine = true,
                    enabled = !loading,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = expiresIn,
                    onValueChange = { expiresIn = it },
                    label = { Text("expires_in seconds (optional)") },
                    singleLine = true,
                    enabled = !loading,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = { onSubmit(accessToken, refreshToken, expiresIn) }
            ) { Text("저장", color = colors.accent) }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("취소", color = colors.textSecondary) }
        }
    )
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
