package com.flit.app.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingGfxInfoMetric
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.OptIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class ScrollJankBenchmark {
    companion object {
        private const val SEARCH_BENCHMARK_KEYWORD = "bench55555"
    }

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun notesScrollJank() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingGfxInfoMetric()),
            iterations = 10,
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()
            tapNotesTab()
            openIdeasFolder()
            scrollDownRepeatedly(times = 15)
        }
    }

    @Test
    fun searchScrollJank() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingGfxInfoMetric()),
            iterations = 10,
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()
            tapHomeTab()
            enterCaptureAndSubmit("search seed $SEARCH_BENCHMARK_KEYWORD")
            tapNotesTab()
            openSearchScreenFromNotes()
            enterSearchKeyword(SEARCH_BENCHMARK_KEYWORD)
            scrollDownRepeatedly(times = 15)
        }
    }
}
