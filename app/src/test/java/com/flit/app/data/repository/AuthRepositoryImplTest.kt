package com.flit.app.data.repository

import android.content.SharedPreferences
import com.flit.app.data.local.database.FlitDatabase
import com.flit.app.data.remote.DeviceIdProvider
import com.flit.app.data.remote.api.FlitApi
import com.flit.app.data.remote.dto.v2.ApiEnvelope
import com.flit.app.data.remote.dto.v2.AuthGoogleRequest
import com.flit.app.data.remote.dto.v2.AuthRefreshRequest
import com.flit.app.data.remote.dto.v2.AuthResponse
import com.flit.app.data.remote.dto.v2.UserResponse
import com.flit.app.domain.model.ApiException
import io.mockk.MockKAnnotations
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * AuthRepositoryImpl 유닛 테스트
 * Google OAuth 로그인, 토큰 저장/조회, 로그아웃 시나리오 검증
 */
class AuthRepositoryImplTest {

    @MockK lateinit var api: FlitApi
    @MockK lateinit var deviceIdProvider: DeviceIdProvider
    @MockK lateinit var prefs: SharedPreferences
    @MockK lateinit var editor: SharedPreferences.Editor
    @MockK lateinit var database: FlitDatabase

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // SharedPreferences.Editor 체이닝 설정
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } just Runs
        every { deviceIdProvider.getOrCreateDeviceId() } returns "test-device-id"
        every { database.clearAllTables() } just Runs
        every { prefs.getString("auth_user_id", null) } returns null
        every { prefs.getString("sync_last_sync_user_id", null) } returns null

        repository = AuthRepositoryImpl(api, deviceIdProvider, prefs, database)
    }

    // ── loginWithGoogle ──

    @Test
    fun `loginWithGoogle - 성공 시 토큰과 사용자 정보 저장`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            expiresIn = 3600,
            user = UserResponse(id = "user-1", email = "test@example.com", subscriptionTier = "FREE")
        )
        coEvery { api.authGoogle(any()) } returns Response.success(
            ApiEnvelope(status = "ok", data = authResponse)
        )

        // When
        val user = repository.loginWithGoogle("google-id-token")

        // Then
        assertEquals("user-1", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("FREE", user.subscriptionTier)
        verify { editor.putString("auth_access_token", "test-access-token") }
        verify { editor.putString("auth_refresh_token", "test-refresh-token") }
        verify { editor.putString("auth_user_id", "user-1") }
        verify { editor.putString("auth_user_email", "test@example.com") }
    }

    @Test
    fun `loginWithGoogle - 디바이스 ID가 요청에 포함`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "at", refreshToken = "rt", expiresIn = 3600,
            user = UserResponse(id = "u1", email = "e@e.com", subscriptionTier = "FREE")
        )
        coEvery { api.authGoogle(any()) } returns Response.success(
            ApiEnvelope(status = "ok", data = authResponse)
        )

        // When
        repository.loginWithGoogle("token")

        // Then — 디바이스 ID가 "test-device-id"인 요청이 전송되었는지 검증
        coVerify { api.authGoogle(match { it.deviceId == "test-device-id" }) }
    }

    @Test
    fun `loginWithGoogle - 401 응답 시 Unauthorized 예외`() = runTest {
        // Given
        coEvery { api.authGoogle(any()) } returns Response.error(
            401,
            """{"status":"error","error":{"code":"UNAUTHORIZED","message":"인증 실패"}}"""
                .toResponseBody("application/json".toMediaType())
        )

        // When / Then
        try {
            repository.loginWithGoogle("invalid-token")
            fail("Unauthorized 예외가 발생해야 합니다")
        } catch (_: ApiException.Unauthorized) {
            // expected
        }
    }

    // ── refreshToken ──

    @Test
    fun `refreshToken - 성공 시 새 토큰 저장 및 AuthToken 반환`() = runTest {
        // Given
        every { prefs.getString("auth_refresh_token", null) } returns "old-refresh-token"
        val authResponse = AuthResponse(
            accessToken = "new-access", refreshToken = "new-refresh", expiresIn = 7200,
            user = UserResponse(id = "u1", email = "e@e.com", subscriptionTier = "PREMIUM")
        )
        coEvery { api.authRefresh(any()) } returns Response.success(
            ApiEnvelope(status = "ok", data = authResponse)
        )

        // When
        val token = repository.refreshToken()

        // Then
        assertEquals("new-access", token.accessToken)
        assertEquals("new-refresh", token.refreshToken)
        verify { editor.putString("auth_access_token", "new-access") }
        verify { editor.putString("auth_refresh_token", "new-refresh") }
    }

    @Test
    fun `refreshToken - user 없는 응답에서도 성공`() = runTest {
        // Given
        every { prefs.getString("auth_refresh_token", null) } returns "old-refresh-token"
        val authResponse = AuthResponse(
            accessToken = "new-access", refreshToken = "new-refresh", expiresIn = 7200,
            user = null  // 서버가 user를 포함하지 않는 경우
        )
        coEvery { api.authRefresh(any()) } returns Response.success(
            ApiEnvelope(status = "ok", data = authResponse)
        )

        // When
        val token = repository.refreshToken()

        // Then — NPE 없이 토큰 반환
        assertEquals("new-access", token.accessToken)
        assertEquals("new-refresh", token.refreshToken)
    }

    @Test
    fun `refreshToken - 리프레시 토큰 없으면 예외`() = runTest {
        // Given — 저장된 리프레시 토큰 없음
        every { prefs.getString("auth_refresh_token", null) } returns null

        // When / Then
        try {
            repository.refreshToken()
            fail("IllegalStateException 예외가 발생해야 합니다")
        } catch (_: IllegalStateException) {
            // expected
        }
    }

    // ── isLoggedIn ──

    @Test
    fun `isLoggedIn - 액세스 토큰 존재 시 true`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns "some-token"

        // When / Then
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun `isLoggedIn - 토큰 null이면 false`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns null

        // When / Then
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun `isLoggedIn - 토큰 빈 문자열이면 false`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns ""

        // When / Then
        assertFalse(repository.isLoggedIn())
    }

    // ── logout ──

    @Test
    fun `logout - 모든 토큰과 사용자 정보 삭제`() = runTest {
        // When
        repository.logout()

        // Then
        verify(exactly = 0) { database.clearAllTables() }
        verify { editor.remove("auth_access_token") }
        verify { editor.remove("auth_refresh_token") }
        verify { editor.remove("auth_token_expires_at") }
        verify { editor.remove("auth_user_id") }
        verify { editor.remove("auth_user_email") }
        verify { editor.remove("auth_subscription_tier") }
        verify { editor.remove("auth_google_calendar_connected") }
        verify { editor.apply() }
    }

    // ── getCurrentUser ──

    @Test
    fun `getCurrentUser - 저장된 사용자 정보 반환`() = runTest {
        // Given
        every { prefs.getString("auth_user_id", null) } returns "user-1"
        every { prefs.getString("auth_user_email", null) } returns "test@example.com"
        every { prefs.getString("auth_subscription_tier", "FREE") } returns "PREMIUM"
        every { prefs.getBoolean("auth_google_calendar_connected", false) } returns false

        // When
        val user = repository.getCurrentUser()

        // Then
        assertNotNull(user)
        assertEquals("user-1", user?.id)
        assertEquals("test@example.com", user?.email)
        assertEquals("PREMIUM", user?.subscriptionTier)
        assertFalse(user!!.googleCalendarConnected)
    }

    @Test
    fun `getCurrentUser - 사용자 ID 없으면 null`() = runTest {
        // Given
        every { prefs.getString("auth_user_id", null) } returns null

        // When / Then
        assertNull(repository.getCurrentUser())
    }

    @Test
    fun `getCurrentUser - 이메일 없으면 null`() = runTest {
        // Given
        every { prefs.getString("auth_user_id", null) } returns "user-1"
        every { prefs.getString("auth_user_email", null) } returns null

        // When / Then
        assertNull(repository.getCurrentUser())
    }
}
