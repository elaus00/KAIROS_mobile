package com.example.kairos_mobile.domain.usecase.auth

import com.example.kairos_mobile.domain.model.User
import com.example.kairos_mobile.domain.repository.AuthRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
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
