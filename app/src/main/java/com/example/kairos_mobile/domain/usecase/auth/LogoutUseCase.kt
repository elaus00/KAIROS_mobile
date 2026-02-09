package com.example.kairos_mobile.domain.usecase.auth

import com.example.kairos_mobile.domain.repository.AuthRepository
import javax.inject.Inject

/** 로그아웃 처리 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
