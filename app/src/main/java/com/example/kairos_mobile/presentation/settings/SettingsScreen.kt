package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.navigation.NavRoutes
import com.example.kairos_mobile.presentation.components.common.GlassBottomNavigation
import com.example.kairos_mobile.presentation.components.common.NavigationTab
import com.example.kairos_mobile.presentation.components.settings.SwitchPreference
import com.example.kairos_mobile.ui.components.AnimatedGlassBackgroundThemed
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 글래스모피즘 스타일의 설정 화면
 * 세련된 디자인과 일관된 미니멀 미학
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val textQuaternaryColor = if (isDarkTheme) TextQuaternary else AiryTextQuaternary
    val dividerColor = if (isDarkTheme) Color(0x20FFFFFF) else AiryGlassBorder

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 테마 인식 애니메이션 배경
        AnimatedGlassBackgroundThemed(isDarkTheme = isDarkTheme)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "설정",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = textPrimaryColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로",
                                tint = textTertiaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.glassButtonThemed(isDarkTheme = isDarkTheme)
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 140.dp) // 하단 네비게이션 공간 확보
                ) {
                    // ========== AI 기능 설정 섹션 ==========
                    Text(
                        text = "AI 기능",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "AI 기반 자동 처리 기능을 설정합니다",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = textTertiaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // M09: 자동 요약
                    SwitchPreference(
                        title = "자동 요약",
                        description = "긴 콘텐츠를 AI가 자동으로 요약합니다 (200자 이상)",
                        checked = uiState.autoSummarizeEnabled,
                        onCheckedChange = viewModel::toggleAutoSummarize,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // M10: 스마트 태그 제안
                    SwitchPreference(
                        title = "스마트 태그 제안",
                        description = "과거 패턴을 학습하여 태그를 자동 제안합니다",
                        checked = uiState.smartTagsEnabled,
                        onCheckedChange = viewModel::toggleSmartTags,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // 구분선
                    Box(
                        modifier = Modifier
                            .fillMaxSize(1f)
                            .height(0.8.dp)
                            .background(
                                color = dividerColor,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ========== 테마 설정 섹션 ==========
                    Text(
                        text = "테마",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "앱의 테마를 설정합니다",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = textTertiaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 다크 모드 전환
                    SwitchPreference(
                        title = "다크 모드",
                        description = "어두운 테마를 사용합니다",
                        checked = uiState.themePreference == com.example.kairos_mobile.domain.model.ThemePreference.DARK,
                        onCheckedChange = viewModel::toggleDarkMode,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // 앱 정보
                    Text(
                        text = "KAIROS Magic Inbox v1.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        color = textQuaternaryColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // 하단 네비게이션
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    GlassBottomNavigation(
                        selectedTab = NavigationTab.SETTINGS,
                        onTabSelected = { tab ->
                            val route = when (tab) {
                                NavigationTab.INSIGHT -> NavRoutes.INSIGHT
                                NavigationTab.SEARCH -> NavRoutes.SEARCH
                                NavigationTab.ARCHIVE -> NavRoutes.ARCHIVE
                                NavigationTab.SETTINGS -> NavRoutes.SETTINGS
                            }
                            if (route != NavRoutes.SETTINGS) {
                                onNavigate(route)
                            }
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}
