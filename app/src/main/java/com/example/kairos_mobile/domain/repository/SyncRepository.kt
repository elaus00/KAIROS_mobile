package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.SyncResult

interface SyncRepository {
    suspend fun pushLocalData(currentUserId: String): SyncResult
    suspend fun pullServerData(currentUserId: String): SyncResult
    suspend fun getLastSyncAt(): String?
}
