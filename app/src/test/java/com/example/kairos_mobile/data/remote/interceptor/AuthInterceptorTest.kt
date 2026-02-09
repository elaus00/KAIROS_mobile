package com.example.kairos_mobile.data.remote.interceptor

import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * AuthInterceptor 유닛 테스트
 * JWT 토큰 존재 시 Authorization 헤더 추가, 미존재 시 헤더 생략 검증
 */
class AuthInterceptorTest {

    @MockK lateinit var prefs: SharedPreferences
    @MockK lateinit var chain: Interceptor.Chain

    private lateinit var interceptor: AuthInterceptor

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        interceptor = AuthInterceptor(prefs)
    }

    @Test
    fun `토큰 존재 시 Authorization Bearer 헤더 추가`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns "my-jwt-token"
        val originalRequest = Request.Builder().url("https://api.example.com/test").build()
        every { chain.request() } returns originalRequest

        val requestSlot = slot<Request>()
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        // When
        interceptor.intercept(chain)

        // Then
        assertEquals("Bearer my-jwt-token", requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `토큰 null이면 Authorization 헤더 없음`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns null
        val originalRequest = Request.Builder().url("https://api.example.com/test").build()
        every { chain.request() } returns originalRequest

        val requestSlot = slot<Request>()
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        // When
        interceptor.intercept(chain)

        // Then — 원본 요청이 그대로 전달됨 (Authorization 헤더 없음)
        assertNull(requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `토큰 빈 문자열이면 Authorization 헤더 없음`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns ""
        val originalRequest = Request.Builder().url("https://api.example.com/test").build()
        every { chain.request() } returns originalRequest

        val requestSlot = slot<Request>()
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        // When
        interceptor.intercept(chain)

        // Then
        assertNull(requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `chain proceed가 반드시 호출됨`() {
        // Given
        every { prefs.getString("auth_access_token", null) } returns "token"
        val originalRequest = Request.Builder().url("https://api.example.com/test").build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        // When
        interceptor.intercept(chain)

        // Then
        verify(exactly = 1) { chain.proceed(any()) }
    }
}
