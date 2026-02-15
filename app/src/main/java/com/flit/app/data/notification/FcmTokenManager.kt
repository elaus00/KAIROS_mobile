package com.flit.app.data.notification

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * FCM 토큰 관리
 * 토큰 획득, 저장, 조회를 담당한다.
 */
@Singleton
class FcmTokenManager @Inject constructor(
    @param:Named("encrypted_prefs") private val prefs: SharedPreferences
) {
    companion object {
        private const val TAG = "FcmTokenManager"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    /**
     * FCM 토큰을 EncryptedSharedPreferences에 저장
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "FCM 토큰 저장 완료")
    }

    /**
     * 저장된 FCM 토큰 조회
     */
    fun getToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Firebase에서 현재 토큰을 가져와 저장
     * 앱 시작 시 호출한다.
     */
    suspend fun fetchAndSaveToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            saveToken(token)
            Log.d(TAG, "FCM 토큰 획득: $token")
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 획득 실패", e)
        }
    }
}
