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
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.model.NoteAiInput
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Inbox 자동 분류 Worker
 * Premium 사용자 한정, 1시간 주기로 TEMP 캡처를 AI 분류
 */
@HiltWorker
class InboxClassifyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteAiRepository: NoteAiRepository,
    private val captureRepository: CaptureRepository,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val subscriptionRepository: SubscriptionRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "InboxClassifyWorker"
        private const val WORK_NAME = "inbox_classify"

        fun enqueuePeriodicWork(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<InboxClassifyWorker>(1, TimeUnit.HOURS)
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
            val tempCaptures = captureRepository.getTempCaptures()
            if (tempCaptures.isEmpty()) {
                Log.d(TAG, "분류 대상 TEMP 캡처 없음")
                return Result.success()
            }

            // 노트 입력 데이터 구축
            val noteInputs = tempCaptures.map { capture ->
                NoteAiInput(
                    captureId = capture.id,
                    aiTitle = capture.aiTitle ?: "",
                    tags = emptyList(),
                    noteSubType = capture.noteSubType?.name,
                    folderId = null
                )
            }
            val folderList = folderRepository.getAllFolders().first()

            val result = noteAiRepository.inboxClassify(noteInputs, folderList)

            // 새 폴더 생성
            for (newFolder in result.newFolders) {
                val folderType = try {
                    FolderType.valueOf(newFolder.type.uppercase())
                } catch (_: Exception) {
                    FolderType.USER
                }
                folderRepository.getOrCreateFolder(newFolder.name, folderType)
            }

            // 분류 결과 적용
            for (assignment in result.assignments) {
                try {
                    val noteSubType = assignment.newNoteSubType.takeIf { it.isNotBlank() }?.let {
                        try { NoteSubType.valueOf(it.uppercase()) } catch (_: Exception) { null }
                    }
                    captureRepository.updateClassification(
                        captureId = assignment.captureId,
                        classifiedType = ClassifiedType.NOTES,
                        noteSubType = noteSubType,
                        aiTitle = "",
                        confidence = ConfidenceLevel.HIGH
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "캡처 ${assignment.captureId} 분류 적용 실패", e)
                }
            }

            Log.d(TAG, "${result.assignments.size}개 캡처 Inbox 분류 완료")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Inbox 분류 실패", e)
            Result.retry()
        }
    }
}
