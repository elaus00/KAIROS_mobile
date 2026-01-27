package com.example.kairos_mobile.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.components.IntegrationCard
import com.example.kairos_mobile.presentation.components.SwitchPreference
import com.example.kairos_mobile.ui.components.glassButton
import com.example.kairos_mobile.ui.theme.NavyDark
import com.example.kairos_mobile.ui.theme.TextPrimary
import com.example.kairos_mobile.ui.theme.TextQuaternary
import com.example.kairos_mobile.ui.theme.TextTertiary

/**
 * 글래스모피즘 스타일의 설정 화면
 * 세련된 디자인과 일관된 미니멀 미학
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.OpenOAuthUrl -> {
                    // Chrome Custom Tab으로 OAuth URL 열기
                    try {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(context, Uri.parse(event.url))
                    } catch (e: Exception) {
                        // Chrome Custom Tab 사용 불가 시 기본 브라우저로 열기
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        context.startActivity(intent)
                    }
                }
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
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "설정",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로",
                                tint = TextTertiary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.glassButton()
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 40.dp)
            ) {
                // ========== 외부 서비스 연동 섹션 ==========
                Text(
                    text = "외부 서비스 연동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "캡처한 내용을 외부 서비스와 자동으로 동기화합니다",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // M11: Google Calendar 연동
                IntegrationCard(
                    title = "Google Calendar",
                    description = "SCHEDULE 타입 캡처를 캘린더에 자동 등록",
                    isConnected = uiState.googleCalendarConnected,
                    lastSyncTime = uiState.googleLastSyncTime,
                    syncedCount = uiState.googleSyncedCount,
                    onConnect = viewModel::connectGoogleCalendar,
                    onDisconnect = viewModel::disconnectGoogleCalendar,
                    onSync = viewModel::syncGoogleCalendar,
                    isLoading = uiState.isGoogleLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // M12: Todoist 연동
                IntegrationCard(
                    title = "Todoist",
                    description = "TODO 타입 캡처를 태스크로 자동 등록",
                    isConnected = uiState.todoistConnected,
                    lastSyncTime = uiState.todoistLastSyncTime,
                    syncedCount = uiState.todoistSyncedCount,
                    onConnect = viewModel::connectTodoist,
                    onDisconnect = viewModel::disconnectTodoist,
                    onSync = viewModel::syncTodoist,
                    isLoading = uiState.isTodoistLoading
                )

                Spacer(modifier = Modifier.height(36.dp))

                // 구분선
                Box(
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .height(0.8.dp)
                        .background(
                            color = Color(0x20FFFFFF),
                            shape = RoundedCornerShape(1.dp)
                        )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ========== AI 기능 설정 섹션 ==========
                Text(
                    text = "AI 기능",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "AI 기반 자동 처리 기능을 설정합니다",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // M09: 자동 요약
                SwitchPreference(
                    title = "자동 요약",
                    description = "긴 콘텐츠를 AI가 자동으로 요약합니다 (200자 이상)",
                    checked = uiState.autoSummarizeEnabled,
                    onCheckedChange = viewModel::toggleAutoSummarize
                )

                Spacer(modifier = Modifier.height(12.dp))

                // M10: 스마트 태그 제안
                SwitchPreference(
                    title = "스마트 태그 제안",
                    description = "과거 패턴을 학습하여 태그를 자동 제안합니다",
                    checked = uiState.smartTagsEnabled,
                    onCheckedChange = viewModel::toggleSmartTags
                )

                Spacer(modifier = Modifier.height(36.dp))

                // 구분선
                Box(
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .height(0.8.dp)
                        .background(
                            color = Color(0x20FFFFFF),
                            shape = RoundedCornerShape(1.dp)
                        )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ========== 테마 설정 섹션 ==========
                Text(
                    text = "테마",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "앱의 테마를 설정합니다",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 다크 모드 전환
                SwitchPreference(
                    title = "다크 모드",
                    description = "어두운 테마를 사용합니다",
                    checked = uiState.themePreference == com.example.kairos_mobile.domain.model.ThemePreference.DARK,
                    onCheckedChange = viewModel::toggleDarkMode
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 앱 정보
                Text(
                    text = "KAIROS Magic Inbox v1.0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = TextQuaternary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
