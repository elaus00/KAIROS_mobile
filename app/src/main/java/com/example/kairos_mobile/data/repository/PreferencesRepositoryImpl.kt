package com.example.kairos_mobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore 인스턴스 생성
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kairos_settings")

/**
 * 앱 설정 Repository 구현체
 * DataStore를 사용하여 설정 값을 저장/조회
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    companion object {
        // M09: AI 요약 설정 키
        private val KEY_AUTO_SUMMARIZE_ENABLED = booleanPreferencesKey("auto_summarize_enabled")

        // M10: 스마트 태그 설정 키
        private val KEY_SMART_TAGS_ENABLED = booleanPreferencesKey("smart_tags_enabled")

        // M11: Google Calendar 연동 설정 키
        private val KEY_GOOGLE_CALENDAR_ENABLED = booleanPreferencesKey("google_calendar_enabled")

        // M12: Todoist 연동 설정 키
        private val KEY_TODOIST_ENABLED = booleanPreferencesKey("todoist_enabled")

        // 테마 설정 키
        private val KEY_THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }

    // ========== M09: AI 요약 설정 ==========

    override fun getAutoSummarizeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_AUTO_SUMMARIZE_ENABLED] ?: true  // 기본값: 활성화
        }
    }

    override suspend fun setAutoSummarizeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_SUMMARIZE_ENABLED] = enabled
        }
    }

    // ========== M10: 스마트 태그 설정 ==========

    override fun getSmartTagsEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_SMART_TAGS_ENABLED] ?: true  // 기본값: 활성화
        }
    }

    override suspend fun setSmartTagsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SMART_TAGS_ENABLED] = enabled
        }
    }

    // ========== M11: Google Calendar 연동 설정 ==========

    override fun getGoogleCalendarEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_GOOGLE_CALENDAR_ENABLED] ?: false  // 기본값: 비활성화
        }
    }

    override suspend fun setGoogleCalendarEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GOOGLE_CALENDAR_ENABLED] = enabled
        }
    }

    // ========== M12: Todoist 연동 설정 ==========

    override fun getTodoistEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_TODOIST_ENABLED] ?: false  // 기본값: 비활성화
        }
    }

    override suspend fun setTodoistEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TODOIST_ENABLED] = enabled
        }
    }

    // ========== 테마 설정 ==========

    override fun getThemePreference(): Flow<ThemePreference> {
        return context.dataStore.data.map { preferences ->
            val themeName = preferences[KEY_THEME_PREFERENCE] ?: ThemePreference.DARK.name
            try {
                ThemePreference.valueOf(themeName)
            } catch (e: Exception) {
                ThemePreference.DARK  // 기본값: 다크 모드
            }
        }
    }

    override suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_PREFERENCE] = theme.name
        }
    }
}
