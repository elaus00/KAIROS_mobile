package com.flit.app.presentation.settings

import androidx.compose.runtime.Composable

private const val PRIVACY_POLICY_URL = "https://flit-app.com/privacy-policy"

/**
 * 개인정보 처리방침 화면 (WebView URL)
 */
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit = {}
) {
    LegalWebViewScreen(
        title = "개인정보 처리방침",
        url = PRIVACY_POLICY_URL,
        onNavigateBack = onNavigateBack
    )
}
