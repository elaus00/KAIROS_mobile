package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 테마 설정 변경 UseCase
 */
@Singleton
class SetThemePreferenceUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(theme: ThemePreference) {
        userPreferenceRepository.setThemePreference(theme)
    }
}
