package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.AuthToken
import com.example.kairos_mobile.domain.model.User

/** 인증 Repository 인터페이스 */
interface AuthRepository {
    suspend fun loginWithGoogle(idToken: String): User
    suspend fun refreshToken(): AuthToken
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
}
