package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 테마 설정 변경 Use Case
 *
 * 비즈니스 로직:
 * - 테마 설정 변경 및 저장
 */
@Singleton
class SetThemePreferenceUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    /**
     * 테마 설정 변경
     *
     * @param theme 설정할 테마
     */
    suspend operator fun invoke(theme: ThemePreference) {
        repository.setThemePreference(theme)
    }

    /**
     * 테마 토글 (다크 ↔ 라이트)
     *
     * @param currentTheme 현재 테마
     * @return 변경될 테마
     */
    suspend fun toggle(currentTheme: ThemePreference): ThemePreference {
        val newTheme = when (currentTheme) {
            ThemePreference.DARK -> ThemePreference.LIGHT
            ThemePreference.LIGHT -> ThemePreference.DARK
        }
        repository.setThemePreference(newTheme)
        return newTheme
    }
}
