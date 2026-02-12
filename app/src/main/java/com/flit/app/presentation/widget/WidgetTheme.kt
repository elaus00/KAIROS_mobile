package com.flit.app.presentation.widget

import android.content.Context
import android.content.res.Configuration
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.first

internal suspend fun resolveWidgetDarkTheme(
    context: Context,
    userPreferenceRepository: UserPreferenceRepository
): Boolean {
    val preference = runCatching {
        userPreferenceRepository.getThemePreference().first()
    }.getOrDefault(ThemePreference.SYSTEM)

    return when (preference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> {
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMode == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
