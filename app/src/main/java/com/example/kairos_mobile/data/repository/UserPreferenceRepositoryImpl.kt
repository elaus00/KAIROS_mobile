package com.example.kairos_mobile.data.repository

import android.content.SharedPreferences
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 사용자 설정 Repository 구현체
 * EncryptedSharedPreferences를 사용하여 설정값을 암호화 저장
 */
@Singleton
class UserPreferenceRepositoryImpl @Inject constructor(
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : UserPreferenceRepository {

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override fun getThemePreference(): Flow<ThemePreference> {
        return observeKey(KEY_THEME).map { value ->
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
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return prefs.getString(KEY_ONBOARDING_COMPLETED, null) == "true"
    }

    override suspend fun setOnboardingCompleted() {
        prefs.edit().putString(KEY_ONBOARDING_COMPLETED, "true").apply()
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    override suspend fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /**
     * SharedPreferences 키 변경을 Flow로 관찰
     */
    private fun observeKey(key: String): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, changedKey ->
            if (changedKey == key) {
                trySend(sp.getString(key, null))
            }
        }
        // 초기값 발행
        trySend(prefs.getString(key, null))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
