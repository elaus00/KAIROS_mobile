package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * 앱 설정 Repository 인터페이스
 * Phase 3: 스마트 처리 기능 설정 관리
 */
interface PreferencesRepository {

    // ========== M09: AI 요약 설정 ==========

    /**
     * 자동 요약 활성화 여부 조회
     */
    fun getAutoSummarizeEnabled(): Flow<Boolean>

    /**
     * 자동 요약 활성화 설정
     */
    suspend fun setAutoSummarizeEnabled(enabled: Boolean)

    // ========== M10: 스마트 태그 설정 ==========

    /**
     * 스마트 태그 제안 활성화 여부 조회
     */
    fun getSmartTagsEnabled(): Flow<Boolean>

    /**
     * 스마트 태그 제안 활성화 설정
     */
    suspend fun setSmartTagsEnabled(enabled: Boolean)

    // ========== M11: Google Calendar 연동 설정 ==========

    /**
     * Google Calendar 연동 활성화 여부 조회
     */
    fun getGoogleCalendarEnabled(): Flow<Boolean>

    /**
     * Google Calendar 연동 활성화 설정
     */
    suspend fun setGoogleCalendarEnabled(enabled: Boolean)

    // ========== M12: Todoist 연동 설정 ==========

    /**
     * Todoist 연동 활성화 여부 조회
     */
    fun getTodoistEnabled(): Flow<Boolean>

    /**
     * Todoist 연동 활성화 설정
     */
    suspend fun setTodoistEnabled(enabled: Boolean)

    // ========== 테마 설정 ==========

    /**
     * 테마 설정 조회
     */
    fun getThemePreference(): Flow<ThemePreference>

    /**
     * 테마 설정 변경
     */
    suspend fun setThemePreference(theme: ThemePreference)

    // ========== QuickCapture Overlay 설정 ==========

    /**
     * 앱 시작 시 QuickCapture 오버레이 표시 여부 조회
     */
    fun getShowOverlayOnLaunch(): Flow<Boolean>

    /**
     * 앱 시작 시 QuickCapture 오버레이 표시 여부 설정
     */
    suspend fun setShowOverlayOnLaunch(enabled: Boolean)
}
