package com.flit.app.domain.repository

import com.flit.app.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 설정 Repository 인터페이스
 * K-V 기반 설정 저장
 */
interface UserPreferenceRepository {

    /** 다크 모드 설정 조회 */
    fun getThemePreference(): Flow<ThemePreference>

    /** 다크 모드 설정 변경 */
    suspend fun setThemePreference(theme: ThemePreference)

    /** 온보딩 완료 여부 조회 */
    suspend fun isOnboardingCompleted(): Boolean

    /** 온보딩 완료 처리 */
    suspend fun setOnboardingCompleted()

    /** 문자열 설정값 조회 */
    suspend fun getString(key: String, defaultValue: String): String

    /** 문자열 설정값 실시간 관찰 */
    fun observeString(key: String, defaultValue: String): Flow<String>

    /** 문자열 설정값 저장 */
    suspend fun setString(key: String, value: String)
}
