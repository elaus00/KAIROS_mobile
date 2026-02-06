package com.example.kairos_mobile.presentation.settings

import androidx.compose.runtime.Composable

private const val TERMS_OF_SERVICE_URL = "https://kairos-app.com/terms"

/**
 * 이용약관 화면 (WebView URL)
 */
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit = {}
) {
    LegalWebViewScreen(
        title = "이용약관",
        url = TERMS_OF_SERVICE_URL,
        onNavigateBack = onNavigateBack
    )
}

