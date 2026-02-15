package com.flit.app.data.remote.interceptor

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * JWT 인증 인터셉터
 * 로그인 상태: Authorization: Bearer {accessToken}
 * 미로그인: 헤더 추가 없음 (DeviceIdInterceptor가 X-Device-ID 추가)
 */
@Singleton
class AuthInterceptor @Inject constructor(
    @param:Named("encrypted_prefs") private val prefs: SharedPreferences
) : Interceptor {

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
