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
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.usecase.classification.ProcessClassificationResultUseCase
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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

            val captureIds = tempCaptures.map { it.id }
            val classifications = noteAiRepository.inboxClassify(captureIds)

            for ((captureId, suggestedType, confidence) in classifications) {
                try {
                    val type = ClassifiedType.valueOf(suggestedType.uppercase())
                    val confidenceLevel = when {
                        confidence >= 0.8 -> ConfidenceLevel.HIGH
                        confidence >= 0.5 -> ConfidenceLevel.MEDIUM
                        else -> ConfidenceLevel.LOW
                    }
                    captureRepository.updateClassification(
                        captureId = captureId,
                        classifiedType = type,
                        noteSubType = null,
                        aiTitle = "",
                        confidence = confidenceLevel
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "캡처 $captureId 분류 적용 실패", e)
                }
            }

            Log.d(TAG, "${classifications.size}개 캡처 Inbox 분류 완료")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Inbox 분류 실패", e)
            Result.retry()
        }
    }
}
