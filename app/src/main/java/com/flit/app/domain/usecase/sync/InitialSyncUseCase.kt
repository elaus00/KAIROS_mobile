package com.flit.app.domain.usecase.sync

import com.flit.app.domain.model.SyncResult
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.SyncRepository
import javax.inject.Inject

class InitialSyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): SyncResult {
        val currentUser = authRepository.getCurrentUser()
            ?: return SyncResult(success = false, skipped = true, message = "로그인 사용자 정보가 없습니다.")
        val lastSync = syncRepository.getLastSyncAt()
        return if (lastSync == null) {
            syncRepository.pushLocalData(currentUser.id)
        } else {
            syncRepository.pullServerData(currentUser.id)
        }
    }
}
