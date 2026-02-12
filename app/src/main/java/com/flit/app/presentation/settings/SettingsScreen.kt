package com.flit.app.presentation.settings

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
import com.flit.app.BuildConfig
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.model.ThemePreference
import com.flit.app.presentation.components.common.SectionHeader
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.settings.components.NavigationSettingItem
import com.flit.app.presentation.settings.components.SettingsCard
import com.flit.app.presentation.settings.components.SettingsDivider
import com.flit.app.presentation.settings.components.ToggleSettingItem
import com.flit.app.presentation.subscription.PremiumGateSheet
import com.flit.app.ui.theme.FlitTheme

/**
 * SettingsScreen
 * 설정 화면 — 화면(테마+글씨크기), 캘린더 토글, AI 분류 진입, 계정, 정보
 * 캘린더 세부 설정과 AI 분류 설정은 각각의 세부 화면으로 분리됨
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
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToCalendarSettings: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {}
) {
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors
    var showPremiumGateSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var premiumGateFeatureName by remember { mutableStateOf("AI 분류 설정") }
    val isPremiumSubscriber = uiState.subscriptionTier == SubscriptionTier.PREMIUM
    val snackbarHostState = remember { SnackbarHostState() }
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
            SectionHeader(
                title = "화면",
                fontSize = 12.sp
            )

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
                Spacer(modifier = Modifier.height(4.dp))

                // 글씨 크기 라벨
                Text(
                    text = "글씨 크기",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                ThemeOptionItem(
                    title = "작게",
                    isSelected = uiState.captureFontSize == FontSizePreference.SMALL.name,
                    onClick = { viewModel.setCaptureFontSize(FontSizePreference.SMALL.name) }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "보통",
                    isSelected = uiState.captureFontSize == FontSizePreference.MEDIUM.name,
                    onClick = { viewModel.setCaptureFontSize(FontSizePreference.MEDIUM.name) }
                )

                SettingsDivider()

                ThemeOptionItem(
                    title = "크게",
                    isSelected = uiState.captureFontSize == FontSizePreference.LARGE.name,
                    onClick = { viewModel.setCaptureFontSize(FontSizePreference.LARGE.name) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 캘린더 섹션 — 토글만 남기고 세부 설정은 세부 화면으로 분리
            SectionHeader(
                title = "캘린더 연동",
                fontSize = 12.sp
            )

            SettingsCard {
                ToggleSettingItem(
                    title = "캘린더 연동",
                    description = if (!uiState.isCalendarPermissionGranted) "활성화하면 캘린더 권한을 요청합니다"
                        else "일정을 기기 캘린더에 동기화",
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

                // 캘린더 연동이 켜져 있을 때만 세부 설정 진입점 표시
                if (uiState.isCalendarPermissionGranted && uiState.isCalendarEnabled) {
                    SettingsDivider()

                    NavigationSettingItem(
                        title = "캘린더 설정",
                        onClick = onNavigateToCalendarSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI 분류 섹션 — 세부 화면으로 진입하는 단일 항목
            SectionHeader(
                title = "AI 분류",
                fontSize = 12.sp
            )

            SettingsCard {
                NavigationSettingItem(
                    title = "AI 분류 설정",
                    onClick = {
                        if (isPremiumSubscriber) {
                            onNavigateToAiSettings()
                        } else {
                            premiumGateFeatureName = "AI 분류 설정"
                            showPremiumGateSheet = true
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 계정 섹션
            SectionHeader(
                title = "계정",
                fontSize = 12.sp
            )

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
            SectionHeader(
                title = "정보",
                fontSize = 12.sp
            )

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

                SectionHeader(
                    title = "개발자 도구",
                    fontSize = 12.sp
                )

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
        val dialogColors = FlitTheme.colors
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
    val colors = FlitTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
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
    val colors = FlitTheme.colors

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
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
