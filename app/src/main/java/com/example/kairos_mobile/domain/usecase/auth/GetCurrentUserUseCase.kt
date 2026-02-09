package com.example.kairos_mobile.domain.usecase.auth

import com.example.kairos_mobile.domain.model.User
import com.example.kairos_mobile.domain.repository.AuthRepository
import javax.inject.Inject

/** 현재 로그인 사용자 조회 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}
