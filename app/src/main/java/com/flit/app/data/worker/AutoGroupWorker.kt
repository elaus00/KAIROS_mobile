package com.flit.app.data.worker

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
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteAiInput
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.FolderRepository
import com.flit.app.domain.repository.NoteAiRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.tracing.AppTrace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
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
        private const val TRACE_AUTO_GROUP_EXECUTION = "auto_group_execution"
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

        return AppTrace.suspendSection(TRACE_AUTO_GROUP_EXECUTION) {
            try {
                val ungroupedNoteIds = noteRepository.getUngroupedNoteIds()
                if (ungroupedNoteIds.isEmpty()) {
                    Log.d(TAG, "그룹화 대상 노트 없음")
                    return@suspendSection Result.success()
                }

                // 노트 입력 데이터 + captureId→noteId 매핑 구축
                val captureToNoteMap = mutableMapOf<String, String>()
                val noteInputs = ungroupedNoteIds.mapNotNull { noteId ->
                    val detail = noteRepository.getNoteDetail(noteId).first() ?: return@mapNotNull null
                    captureToNoteMap[detail.captureId] = noteId
                    NoteAiInput(
                        captureId = detail.captureId,
                        aiTitle = detail.aiTitle ?: "",
                        tags = emptyList(),
                        noteSubType = detail.noteSubType?.name,
                        folderId = detail.folderId
                    )
                }

                val folderList = folderRepository.getAllFolders().first()
                val groups = noteAiRepository.groupNotes(noteInputs, folderList)

                for (group in groups) {
                    val folderType = try {
                        FolderType.valueOf(group.folderType.uppercase())
                    } catch (_: Exception) {
                        FolderType.AI_GROUP
                    }
                    val folder = folderRepository.getOrCreateFolder(group.folderName, folderType)
                    for (captureId in group.captureIds) {
                        val noteId = captureToNoteMap[captureId] ?: continue
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
}
