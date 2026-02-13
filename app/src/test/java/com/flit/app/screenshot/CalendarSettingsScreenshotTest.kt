package com.flit.app.screenshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.LocalCalendar
import com.flit.app.presentation.settings.calendar.CalendarSettingsContent
import com.flit.app.presentation.settings.calendar.CalendarSettingsUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * CalendarSettingsContent 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalendarSettingsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calendar_settings_default() {
        composeRule.setContent {
            FlitTheme {
                CalendarSettingsContent(
                    uiState = CalendarSettingsUiState(
                        availableCalendars = listOf(
                            LocalCalendar(
                                id = 1L,
                                displayName = "개인 캘린더",
                                accountName = "user@gmail.com",
                                color = -16711936,
                                isPrimary = true
                            )
                        ),
                        selectedCalendarId = 1L,
                        isAutoAddEnabled = false,
                        isNotificationEnabled = true
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = {},
                    onToggleAutoAdd = {},
                    onToggleNotification = {},
                    onSetTargetCalendar = {},
                    onReloadCalendars = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/calendar_settings_default.png")
    }

    @Test
    fun calendar_settings_auto_add_enabled() {
        composeRule.setContent {
            FlitTheme {
                CalendarSettingsContent(
                    uiState = CalendarSettingsUiState(
                        availableCalendars = listOf(
                            LocalCalendar(
                                id = 1L,
                                displayName = "개인 캘린더",
                                accountName = "user@gmail.com",
                                color = -16711936,
                                isPrimary = true
                            ),
                            LocalCalendar(
                                id = 2L,
                                displayName = "업무 캘린더",
                                accountName = "work@company.com",
                                color = -16776961,
                                isPrimary = false
                            )
                        ),
                        selectedCalendarId = 2L,
                        isAutoAddEnabled = true,
                        isNotificationEnabled = true
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = {},
                    onToggleAutoAdd = {},
                    onToggleNotification = {},
                    onSetTargetCalendar = {},
                    onReloadCalendars = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/calendar_settings_auto_add_enabled.png")
    }
}
