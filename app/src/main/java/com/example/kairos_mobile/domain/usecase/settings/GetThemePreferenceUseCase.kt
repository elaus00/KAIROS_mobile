package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 테마 설정 조회 UseCase
 */
@Singleton
class GetThemePreferenceUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    operator fun invoke(): Flow<ThemePreference> {
        return userPreferenceRepository.getThemePreference()
    }
}
