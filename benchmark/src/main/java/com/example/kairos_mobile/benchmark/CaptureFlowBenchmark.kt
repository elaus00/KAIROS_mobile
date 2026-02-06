package com.example.kairos_mobile.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.OptIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class CaptureFlowBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun captureSaveCompletionTime() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(TraceSectionMetric("capture_save_completion")),
            iterations = 10,
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()
            tapHomeTab()
            enterCaptureAndSubmit("macro save benchmark ${SystemClock.uptimeMillis()}")
        }
    }

    @Test
    fun aiClassificationCompletionTime() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(TraceSectionMetric("ai_classification_completion")),
            iterations = 10,
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()
            tapHomeTab()
            enterCaptureAndSubmit("macro classify benchmark ${SystemClock.uptimeMillis()}")
            waitForClassificationCompletionWindow()
        }
    }
}
