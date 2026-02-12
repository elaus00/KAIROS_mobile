package com.flit.app.domain.usecase.auth

import com.flit.app.domain.model.SubscriptionFeatures
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.model.User
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.SubscriptionRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * LoginWithGoogleUseCase 유닛 테스트
 * 로그인 성공 후 구독 정보 갱신, 구독 조회 실패 시 로그인 결과 유지 검증
 */
class LoginWithGoogleUseCaseTest {

    @MockK lateinit var authRepository: AuthRepository
    @MockK lateinit var subscriptionRepository: SubscriptionRepository

    private lateinit var useCase: LoginWithGoogleUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = LoginWithGoogleUseCase(authRepository, subscriptionRepository)
    }

    @Test
    fun `로그인 성공 후 구독 정보 갱신`() = runTest {
        // Given
        val user = User(id = "u1", email = "test@example.com", subscriptionTier = "FREE")
        coEvery { authRepository.loginWithGoogle("id-token") } returns user
        coEvery { subscriptionRepository.getSubscription() } returns
                (SubscriptionTier.FREE to SubscriptionFeatures())

        // When
        val result = useCase("id-token")

        // Then
        assertEquals("u1", result.id)
        assertEquals("test@example.com", result.email)
        coVerify(exactly = 1) { authRepository.loginWithGoogle("id-token") }
        coVerify(exactly = 1) { subscriptionRepository.getSubscription() }
    }

    @Test
    fun `구독 정보 갱신 실패해도 로그인은 성공`() = runTest {
        // Given
        val user = User(id = "u1", email = "test@example.com", subscriptionTier = "FREE")
        coEvery { authRepository.loginWithGoogle("id-token") } returns user
        coEvery { subscriptionRepository.getSubscription() } throws RuntimeException("서버 오류")

        // When
        val result = useCase("id-token")

        // Then — 구독 조회 실패와 관계없이 User 반환
        assertEquals("u1", result.id)
        assertEquals("FREE", result.subscriptionTier)
    }

    @Test(expected = RuntimeException::class)
    fun `authRepository 로그인 실패 시 예외 전파`() = runTest {
        // Given
        coEvery { authRepository.loginWithGoogle("bad-token") } throws RuntimeException("로그인 실패")

        // When — 예외 발생 기대
        useCase("bad-token")
    }
}
