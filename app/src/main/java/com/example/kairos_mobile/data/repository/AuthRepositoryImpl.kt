package com.example.kairos_mobile.data.repository

import android.content.SharedPreferences
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.AuthGoogleRequest
import com.example.kairos_mobile.data.remote.dto.v2.AuthRefreshRequest
import com.example.kairos_mobile.domain.model.AuthToken
import com.example.kairos_mobile.domain.model.User
import com.example.kairos_mobile.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 인증 Repository 구현체
 * Google OAuth → JWT 교환, 토큰 저장/갱신/로그아웃
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val deviceIdProvider: DeviceIdProvider,
    @Named("encrypted_prefs") private val prefs: SharedPreferences,
    private val database: KairosDatabase
) : AuthRepository {

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "auth_token_expires_at"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_SUBSCRIPTION_TIER = "auth_subscription_tier"
        private const val KEY_GOOGLE_CALENDAR_CONNECTED = "auth_google_calendar_connected"
    }

    override suspend fun loginWithGoogle(idToken: String): User {
        val request = AuthGoogleRequest(
            idToken = idToken,
            deviceId = deviceIdProvider.getOrCreateDeviceId()
        )
        val response = ApiResponseHandler.safeCall { api.authGoogle(request) }
        saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
        val userDto = response.user
            ?: throw IllegalStateException("로그인 응답에 사용자 정보가 없습니다.")
        val user = User(
            id = userDto.id,
            email = userDto.email,
            subscriptionTier = userDto.subscriptionTier,
            googleCalendarConnected = userDto.googleCalendarConnected
        )
        saveUser(user)
        return user
    }

    override suspend fun refreshToken(): AuthToken {
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
            ?: throw IllegalStateException("리프레시 토큰이 없습니다.")
        val response = ApiResponseHandler.safeCall {
            api.authRefresh(AuthRefreshRequest(refreshToken))
        }
        saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
        // 리프레시 응답에 user가 포함되면 업데이트, 없으면 기존 캐시 유지
        response.user?.let { userDto ->
            val user = User(
                id = userDto.id,
                email = userDto.email,
                subscriptionTier = userDto.subscriptionTier,
                googleCalendarConnected = userDto.googleCalendarConnected
            )
            saveUser(user)
        }
        return AuthToken(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000)
        )
    }

    override suspend fun logout() {
        // 로컬 DB 데이터 전체 삭제 (계정 간 데이터 혼재 방지)
        // clearAllTables()는 동기 API이므로 IO 디스패처에서 실행
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }

        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRES_AT)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_SUBSCRIPTION_TIER)
            .remove(KEY_GOOGLE_CALENDAR_CONNECTED)
            .apply()
    }

    override suspend fun getCurrentUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val tier = prefs.getString(KEY_SUBSCRIPTION_TIER, "FREE") ?: "FREE"
        val calendarConnected = prefs.getBoolean(KEY_GOOGLE_CALENDAR_CONNECTED, false)
        return User(id = id, email = email, subscriptionTier = tier, googleCalendarConnected = calendarConnected)
    }

    override fun isLoggedIn(): Boolean {
        return !prefs.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank()
    }

    private fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresIn * 1000)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            .apply()
    }

    private fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_SUBSCRIPTION_TIER, user.subscriptionTier)
            .putBoolean(KEY_GOOGLE_CALENDAR_CONNECTED, user.googleCalendarConnected)
            .apply()
    }
}
