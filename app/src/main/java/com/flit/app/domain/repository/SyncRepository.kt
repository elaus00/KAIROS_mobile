package com.flit.app.domain.repository

import com.flit.app.domain.model.SyncResult

interface SyncRepository {
    suspend fun pushLocalData(currentUserId: String): SyncResult
    suspend fun pullServerData(currentUserId: String): SyncResult
    suspend fun getLastSyncAt(): String?
}
