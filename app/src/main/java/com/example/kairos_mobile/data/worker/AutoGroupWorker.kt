package com.example.kairos_mobile.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 자동 노트 그룹화 Worker
 * Premium 사용자 한정, 1시간 주기로 Inbox 노트를 AI 그룹화
 */
@HiltWorker
class AutoGroupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteAiRepository: NoteAiRepository,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val subscriptionRepository: SubscriptionRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AutoGroupWorker"
        private const val WORK_NAME = "auto_group"

        fun enqueuePeriodicWork(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<AutoGroupWorker>(1, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            Log.d(TAG, "Premium 구독 아님, 스킵")
            return Result.success()
        }

        return try {
            val ungroupedNotes = noteRepository.getUngroupedNoteIds()
            if (ungroupedNotes.isEmpty()) {
                Log.d(TAG, "그룹화 대상 노트 없음")
                return Result.success()
            }

            val groups = noteAiRepository.groupNotes(ungroupedNotes)
            for (group in groups) {
                val folder = folderRepository.getOrCreateFolder(group.groupName, FolderType.AI_GROUP)
                for (noteId in group.noteIds) {
                    noteRepository.moveToFolder(noteId, folder.id)
                }
            }
            Log.d(TAG, "${groups.size}개 그룹 생성 완료")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "그룹화 실패", e)
            Result.retry()
        }
    }
}
