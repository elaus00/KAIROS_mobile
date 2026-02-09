package com.example.kairos_mobile.data.repository

import android.content.SharedPreferences
import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.DeviceIdProvider
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.AuthGoogleRequest
import com.example.kairos_mobile.data.remote.dto.v2.AuthRefreshRequest
import com.example.kairos_mobile.domain.model.AuthToken
import com.example.kairos_mobile.domain.model.User
import com.example.kairos_mobile.domain.repository.AuthRepository
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
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : AuthRepository {

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "auth_token_expires_at"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_SUBSCRIPTION_TIER = "auth_subscription_tier"
    }

    override suspend fun loginWithGoogle(idToken: String): User {
        val request = AuthGoogleRequest(
            idToken = idToken,
            deviceId = deviceIdProvider.getOrCreateDeviceId()
        )
        val response = ApiResponseHandler.safeCall { api.authGoogle(request) }
        saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
        val user = User(
            id = response.user.id,
            email = response.user.email,
            subscriptionTier = response.user.subscriptionTier
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
        val user = User(
            id = response.user.id,
            email = response.user.email,
            subscriptionTier = response.user.subscriptionTier
        )
        saveUser(user)
        return AuthToken(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000)
        )
    }

    override suspend fun logout() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRES_AT)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_SUBSCRIPTION_TIER)
            .apply()
    }

    override suspend fun getCurrentUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val tier = prefs.getString(KEY_SUBSCRIPTION_TIER, "FREE") ?: "FREE"
        return User(id = id, email = email, subscriptionTier = tier)
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
            .apply()
    }
}
