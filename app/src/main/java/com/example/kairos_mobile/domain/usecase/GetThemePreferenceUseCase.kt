package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 테마 설정 조회 Use Case
 *
 * 비즈니스 로직:
 * - 현재 테마 설정 조회
 */
@Singleton
class GetThemePreferenceUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    /**
     * 테마 설정 조회
     *
     * @return 테마 설정 Flow
     */
    operator fun invoke(): Flow<ThemePreference> {
        return repository.getThemePreference()
    }
}
