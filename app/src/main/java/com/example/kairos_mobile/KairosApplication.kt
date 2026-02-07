package com.example.kairos_mobile

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.kairos_mobile.data.debug.MockDataInitializer
import com.example.kairos_mobile.data.notification.NotificationHelper
import com.example.kairos_mobile.data.worker.ReclassifyTempWorker
import com.example.kairos_mobile.data.worker.TrashCleanupWorker
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * KAIROS Application 클래스
 * Hilt 및 WorkManager 초기화
 */
@HiltAndroidApp
class KairosApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var mockDataInitializer: MockDataInitializer

    @Inject
    lateinit var syncQueueRepository: SyncQueueRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    // Application 레벨 CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        // 알림 채널 생성
        notificationHelper.createNotificationChannels()

        // WorkManager 초기화는 Configuration.Provider로 처리됨

        // 앱 시작 시 PROCESSING 상태를 PENDING으로 리셋 (비정상 종료 복구)
        applicationScope.launch {
            syncQueueRepository.resetProcessingToPending()
            Log.d(TAG, "SyncQueue PROCESSING → PENDING 리셋 완료")
        }

        // Mock/Benchmark 데이터 초기화
        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "benchmark") {
            applicationScope.launch {
                mockDataInitializer.initializeMockData()
                if (BuildConfig.BUILD_TYPE == "benchmark") {
                    mockDataInitializer.initializeBenchmarkData()
                }
            }
        }

        // 15분 주기 TEMP 재분류 Worker 등록
        ReclassifyTempWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "ReclassifyTempWorker 등록 완료")

        // 일일 휴지통 정리 Worker 등록
        TrashCleanupWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "TrashCleanupWorker 등록 완료")

        // 1시간 주기 분석 이벤트 배치 Worker 등록
        com.example.kairos_mobile.data.worker.AnalyticsBatchWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "AnalyticsBatchWorker 등록 완료")

        // 1시간 주기 캘린더 동기화 재시도 Worker 등록
        com.example.kairos_mobile.data.worker.CalendarSyncWorker.enqueuePeriodicWork(workManager)
        Log.d(TAG, "CalendarSyncWorker 등록 완료")
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
        private const val TAG = "KairosApplication"
    }
}
