package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.UserPreferenceDao
import com.example.kairos_mobile.data.local.database.entities.UserPreferenceEntity
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 설정 Repository 구현체
 */
@Singleton
class UserPreferenceRepositoryImpl @Inject constructor(
    private val preferenceDao: UserPreferenceDao
) : UserPreferenceRepository {

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override fun getThemePreference(): Flow<ThemePreference> {
        return preferenceDao.observe(KEY_THEME).map { value ->
            value?.let {
                try {
                    ThemePreference.valueOf(it)
                } catch (e: Exception) {
                    ThemePreference.SYSTEM
                }
            } ?: ThemePreference.SYSTEM
        }
    }

    override suspend fun setThemePreference(theme: ThemePreference) {
        preferenceDao.set(
            UserPreferenceEntity(key = KEY_THEME, value = theme.name)
        )
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return preferenceDao.get(KEY_ONBOARDING_COMPLETED) == "true"
    }

    override suspend fun setOnboardingCompleted() {
        preferenceDao.set(
            UserPreferenceEntity(key = KEY_ONBOARDING_COMPLETED, value = "true")
        )
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        return preferenceDao.get(key) ?: defaultValue
    }

    override suspend fun setString(key: String, value: String) {
        preferenceDao.set(
            UserPreferenceEntity(key = key, value = value)
        )
    }
}
