package com.flit.app.domain.usecase.auth

import com.flit.app.domain.model.User
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** Google 로그인 후 구독 정보도 함께 로드 */
class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(idToken: String): User {
        val user = authRepository.loginWithGoogle(idToken)
        // 로그인 후 구독 정보 캐시 갱신
        try { subscriptionRepository.getSubscription() } catch (_: Exception) {}
        return user
    }
}
