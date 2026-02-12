package com.flit.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.flit.app.domain.model.CaptureSource
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.navigation.FlitNavGraph
import com.flit.app.navigation.NavRoutes
import com.flit.app.ui.theme.FlitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Flit 메인 액티비티
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var submitCaptureUseCase: SubmitCaptureUseCase

    @Inject
    lateinit var userPreferenceRepository: UserPreferenceRepository

    /** 위젯에서 진입 시 캡처 입력 자동 포커스 (Compose 재합성 트리거) */
    private val autoFocusCapture = mutableStateOf(false)

    /** 위젯에서 할 일 탭 시 상세 화면으로 이동할 captureId */
    private val pendingCaptureId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 위젯 딥링크 처리
        autoFocusCapture.value = intent?.getBooleanExtra("from_widget", false) == true
        pendingCaptureId.value = intent?.getStringExtra("navigate_to_capture_id")

        // 공유 인텐트 처리 (텍스트 캡처 저장 + 토스트)
        handleShareIntent(intent)

        enableEdgeToEdge()
        setContent {
            val themePreference by userPreferenceRepository.getThemePreference().collectAsState(initial = ThemePreference.DARK)
            val isDarkTheme = themePreference == ThemePreference.DARK

            // 온보딩 완료 여부에 따라 시작 화면 결정
            val startDestination by produceState<String?>(initialValue = null) {
                value = if (userPreferenceRepository.isOnboardingCompleted()) {
                    NavRoutes.HOME
                } else {
                    NavRoutes.ONBOARDING
                }
            }

            FlitTheme(darkTheme = isDarkTheme) {
                startDestination?.let { destination ->
                    FlitNavGraph(
                        navController = rememberNavController(),
                        startDestination = destination,
                        autoFocusCapture = autoFocusCapture.value,
                        pendingCaptureId = pendingCaptureId.value,
                        onPendingCaptureHandled = { pendingCaptureId.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        autoFocusCapture.value = intent?.getBooleanExtra("from_widget", false) == true
        pendingCaptureId.value = intent.getStringExtra("navigate_to_capture_id")
        handleShareIntent(intent)
    }

    /**
     * 공유 인텐트에서 텍스트 추출 → 캡처 저장 → 토스트 표시
     * 기능명세서 4.1: 텍스트 수신 (Phase 1)
     */
    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        when {
            intent.type == "text/plain" -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text.isNullOrBlank()) return

                lifecycleScope.launch {
                    try {
                        submitCaptureUseCase(text, CaptureSource.SHARE_INTENT)
                        Toast.makeText(this@MainActivity, "캡처 완료", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "캡처 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            intent.type?.startsWith("image/") == true -> {
                // Phase 2a: 이미지 공유 수신
                Toast.makeText(this, "텍스트만 지원됩니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
