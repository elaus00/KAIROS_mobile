package com.example.kairos_mobile

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
import com.example.kairos_mobile.navigation.KairosNavGraph
import com.example.kairos_mobile.ui.theme.KAIROS_mobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * KAIROS Magic Inbox 메인 액티비티
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getThemePreferenceUseCase: GetThemePreferenceUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // M07: 공유 인텐트 처리
        val sharedContent = handleShareIntent(intent)

        enableEdgeToEdge()
        setContent {
            // 테마 설정 수집
            val themePreference by getThemePreferenceUseCase().collectAsState(initial = ThemePreference.DARK)
            val isDarkTheme = themePreference == ThemePreference.DARK

            KAIROS_mobileTheme(darkTheme = isDarkTheme) {
                KairosNavGraph(
                    navController = rememberNavController(),
                    sharedText = sharedContent?.text,
                    sharedImageUri = sharedContent?.imageUri
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // 새로운 공유 인텐트 수신 시 화면 재구성
        val sharedContent = handleShareIntent(intent)

        if (sharedContent != null) {
            recreate()
        }
    }

    /**
     * M07: 공유 인텐트에서 데이터 추출
     */
    private fun handleShareIntent(intent: Intent?): SharedContent? {
        if (intent?.action != Intent.ACTION_SEND) {
            return null
        }

        return when {
            // 텍스트 공유 (URL 포함)
            intent.type == "text/plain" -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                sharedText?.let { SharedContent(text = it) }
            }
            // 이미지 공유
            intent.type?.startsWith("image/") == true -> {
                val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
                imageUri?.let { SharedContent(imageUri = it) }
            }
            else -> null
        }
    }

    /**
     * 공유된 콘텐츠
     */
    private data class SharedContent(
        val text: String? = null,
        val imageUri: Uri? = null
    )
}
