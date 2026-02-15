package com.flit.app.presentation.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.flit.app.BuildConfig
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.FlitWordmark
import com.flit.app.presentation.components.common.FlitWordmarkSize
import com.flit.app.ui.theme.FlitTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

/**
 * 로그인 화면
 * Google 로그인 버튼 + 미로그인 안내
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.LaunchGoogleLogin -> {
                    scope.launch {
                        val idToken = requestGoogleIdToken(context = context, credentialManager = credentialManager)
                        if (idToken != null) {
                            viewModel.onGoogleIdTokenReceived(idToken)
                        } else {
                            viewModel.onGoogleLoginError(
                                if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                                    "GOOGLE_WEB_CLIENT_ID를 설정해주세요."
                                } else {
                                    "Google 로그인에 실패했습니다. 다시 시도해주세요."
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onStartGoogleLogin = viewModel::startGoogleLogin
    )
}

/**
 * 로그인 화면 Content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onNavigateBack: () -> Unit,
    onStartGoogleLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "로그인",
                        color = colors.text,
                        style = MaterialTheme.typography.headlineSmall
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
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FlitWordmark(
                size = FlitWordmarkSize.SPLASH,
                color = colors.accent,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "적으면, 알아서 정리됩니다",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartGoogleLogin,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.background,
                    disabledContainerColor = colors.accentBg,
                    disabledContentColor = colors.textMuted
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.background
                    )
                } else {
                    Text(
                        text = "Google로 로그인",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }

            val error = uiState.error
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = colors.danger,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "로그인 없이도 기본 기능을 사용할 수 있습니다",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "다른 Google 계정으로 로그인하면 기존 로컬 데이터는 초기화됩니다",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginContentPreview() {
    FlitTheme {
        LoginContent(
            uiState = LoginUiState(),
            onNavigateBack = {},
            onStartGoogleLogin = {}
        )
    }
}

private suspend fun requestGoogleIdToken(
    context: android.content.Context,
    credentialManager: CredentialManager
): String? {
    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
        return null
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } else {
            null
        }
    } catch (_: GetCredentialException) {
        null
    } catch (_: GoogleIdTokenParsingException) {
        null
    }
}
