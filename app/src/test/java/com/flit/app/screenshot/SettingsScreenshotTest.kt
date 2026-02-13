package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.model.User
import com.flit.app.presentation.settings.SettingsContent
import com.flit.app.presentation.settings.SettingsUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * SettingsScreen 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsContent_default_notLoggedIn() {
        composeRule.setContent {
            FlitTheme {
                SettingsContent(
                    uiState = SettingsUiState(),
                    onNavigateBack = {},
                    onNavigateToPrivacyPolicy = {},
                    onNavigateToTermsOfService = {},
                    onNavigateToLogin = {},
                    onNavigateToSubscription = {},
                    onNavigateToAnalytics = {},
                    onNavigateToCalendarSettings = {},
                    onNavigateToAiSettings = {},
                    onSetTheme = {},
                    onSetCaptureFontSize = {},
                    onSetNoteViewType = {},
                    onToggleCalendar = {},
                    onRequestCalendarPermission = {},
                    onLogout = {},
                    onDebugSubmitImage = {},
                    onDismissDebugResult = {},
                    onDismissCalendarAuthMessage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/settings_default_not_logged_in.png")
    }

    @Test
    fun settingsContent_loggedIn_premium() {
        composeRule.setContent {
            FlitTheme {
                SettingsContent(
                    uiState = SettingsUiState(
                        user = User(
                            id = "user1",
                            email = "user@example.com",
                            subscriptionTier = "premium"
                        ),
                        subscriptionTier = SubscriptionTier.PREMIUM,
                        themePreference = ThemePreference.DARK,
                        isCalendarEnabled = true,
                        isCalendarPermissionGranted = true
                    ),
                    onNavigateBack = {},
                    onNavigateToPrivacyPolicy = {},
                    onNavigateToTermsOfService = {},
                    onNavigateToLogin = {},
                    onNavigateToSubscription = {},
                    onNavigateToAnalytics = {},
                    onNavigateToCalendarSettings = {},
                    onNavigateToAiSettings = {},
                    onSetTheme = {},
                    onSetCaptureFontSize = {},
                    onSetNoteViewType = {},
                    onToggleCalendar = {},
                    onRequestCalendarPermission = {},
                    onLogout = {},
                    onDebugSubmitImage = {},
                    onDismissDebugResult = {},
                    onDismissCalendarAuthMessage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/settings_logged_in_premium.png")
    }

    @Test
    fun settingsContent_lightTheme_selected() {
        composeRule.setContent {
            FlitTheme {
                SettingsContent(
                    uiState = SettingsUiState(
                        themePreference = ThemePreference.LIGHT
                    ),
                    onNavigateBack = {},
                    onNavigateToPrivacyPolicy = {},
                    onNavigateToTermsOfService = {},
                    onNavigateToLogin = {},
                    onNavigateToSubscription = {},
                    onNavigateToAnalytics = {},
                    onNavigateToCalendarSettings = {},
                    onNavigateToAiSettings = {},
                    onSetTheme = {},
                    onSetCaptureFontSize = {},
                    onSetNoteViewType = {},
                    onToggleCalendar = {},
                    onRequestCalendarPermission = {},
                    onLogout = {},
                    onDebugSubmitImage = {},
                    onDismissDebugResult = {},
                    onDismissCalendarAuthMessage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/settings_light_theme.png")
    }
}
