package com.example.kairos_mobile

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
// import com.example.kairos_mobile.data.worker.SyncCaptureWorker
import dagger.hilt.android.HiltAndroidApp
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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        // WorkManager 초기화는 Configuration.Provider로 처리됨

        // 주기적 동기화 작업 시작 (임시 비활성화)
        // SyncCaptureWorker.enqueuePeriodicSync(workManager)
        // Log.d(TAG, "Periodic sync work scheduled")
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
