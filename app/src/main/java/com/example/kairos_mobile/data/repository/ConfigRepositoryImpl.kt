package com.example.kairos_mobile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.kairos_mobile.domain.repository.ConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 설정 Repository 구현체
 */
@Singleton
class ConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ConfigRepository {

    /**
     * Obsidian Vault 경로 조회
     * Phase 1: 하드코딩 (나중에 설정 화면에서 변경 가능하도록 확장)
     */
    override suspend fun getObsidianVaultPath(): String {
        return "KAIROS_Vault"
    }

    /**
     * API 인증 토큰 조회
     * Phase 1: Mock API는 토큰 불필요
     */
    override suspend fun getApiToken(): String {
        return "mock_token"
    }

    /**
     * 네트워크 연결 상태 확인
     */
    override suspend fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
