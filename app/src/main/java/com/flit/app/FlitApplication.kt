package com.flit.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.flit.app.data.notification.FcmTokenManager
import com.flit.app.data.notification.NotificationHelper
import com.flit.app.data.worker.AutoGroupWorker
import com.flit.app.data.worker.InboxClassifyWorker
import com.flit.app.data.worker.ReclassifyTempWorker
import com.flit.app.data.worker.TrashCleanupWorker
import com.flit.app.domain.repository.SyncQueueRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Flit Application 클래스
 * Hilt 및 WorkManager 초기화
 */
@HiltAndroidApp
class FlitApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var syncQueueRepository: SyncQueueRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    // Application 레벨 CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        // 알림 채널 생성
        notificationHelper.createNotificationChannels()

        // FCM 토큰 획득 및 저장
        applicationScope.launch {
            fcmTokenManager.fetchAndSaveToken()
        }

        // WorkManager 초기화는 Configuration.Provider로 처리됨

        // 앱 시작 시 PROCESSING 상태를 PENDING으로 리셋 (비정상 종료 복구)
        applicationScope.launch {
            syncQueueRepository.resetProcessingToPending()
            Log.d(TAG, "SyncQueue PROCESSING → PENDING 리셋 완료")
        }

        // 15분 주기 TEMP 재분류 Worker 등록
        ReclassifyTempWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "ReclassifyTempWorker 등록 완료")

        // 일일 휴지통 정리 Worker 등록
        TrashCleanupWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "TrashCleanupWorker 등록 완료")

        // 1시간 주기 분석 이벤트 배치 Worker 등록
        com.flit.app.data.worker.AnalyticsBatchWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "AnalyticsBatchWorker 등록 완료")

        // 1시간 주기 캘린더 동기화 재시도 Worker 등록
        com.flit.app.data.worker.CalendarSyncWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "CalendarSyncWorker 등록 완료")

        // 1시간 주기 AI 노트 자동 그룹화 Worker 등록 (Premium)
        AutoGroupWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "AutoGroupWorker 등록 완료")

        // 1시간 주기 Inbox 자동 분류 Worker 등록 (Premium)
        InboxClassifyWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "InboxClassifyWorker 등록 완료")
    }

    /**
     * WorkManager Configuration 제공
     * Hilt Worker Factory 사용
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

    companion object {
        private const val TAG = "FlitApplication"
    }
}
