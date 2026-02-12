package com.flit.app.presentation.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.settings.PreferenceKeys
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface FontScaleEntryPoint {
    fun userPreferenceRepository(): UserPreferenceRepository
}

@Composable
fun rememberAppFontScale(): Float {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FontScaleEntryPoint::class.java
        )
    }
    val sizeKey by produceState(initialValue = FontSizePreference.MEDIUM.name, key1 = entryPoint) {
        value = runCatching {
            entryPoint.userPreferenceRepository().getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
        }.getOrDefault(FontSizePreference.MEDIUM.name)
    }

    val preference = FontSizePreference.fromString(sizeKey)
    return preference.bodyFontSize / FontSizePreference.MEDIUM.bodyFontSize.toFloat()
}

@Composable
fun AppFontScaleProvider(content: @Composable () -> Unit) {
    val appScale = rememberAppFontScale()
    val currentDensity = LocalDensity.current
    val scaledDensity = remember(currentDensity, appScale) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * appScale
        )
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        content()
    }
}
