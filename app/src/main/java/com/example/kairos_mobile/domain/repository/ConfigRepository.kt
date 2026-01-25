package com.example.kairos_mobile.domain.repository

/**
 * 설정 Repository 인터페이스
 */
interface ConfigRepository {

    /**
     * Obsidian Vault 경로 조회
     * Phase 1: 하드코딩
     */
    suspend fun getObsidianVaultPath(): String

    /**
     * API 인증 토큰 조회
     * Phase 1: Mock API는 토큰 불필요
     */
    suspend fun getApiToken(): String

    /**
     * 네트워크 연결 상태 확인
     */
    suspend fun isNetworkAvailable(): Boolean
}
