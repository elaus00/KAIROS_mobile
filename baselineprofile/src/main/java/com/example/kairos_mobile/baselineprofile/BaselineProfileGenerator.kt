package com.example.kairos_mobile.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.example.kairos_mobile",
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()

            clickFirstAvailable(By.res("tab_notes"), By.desc("Notes"), By.text("Notes"))
            clickFirstAvailable(By.res("notes_folder_system-ideas"), By.text("Ideas"))
            scrollDownRepeatedly(times = 4)
            device.pressBack()
        }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.dismissOnboardingIfVisible() {
        device.wait(Until.findObject(By.text("건너뛰기")), 1_500L)?.click()
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.waitForHomeReady() {
        val homeIndicator = device.wait(
            Until.findObject(By.res("tab_home")),
            6_000L
        ) ?: device.wait(
            Until.findObject(By.desc("Home")),
            6_000L
        )
        checkNotNull(homeIndicator) { "Home screen did not load in time." }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.clickFirstAvailable(
        vararg selectors: androidx.test.uiautomator.BySelector
    ) {
        selectors.forEach { selector ->
            val obj = device.wait(Until.findObject(selector), 6_000L)
            if (obj != null) {
                obj.click()
                device.waitForIdle()
                return
            }
        }
        error("None of the selectors matched.")
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.enterSearchKeyword(keyword: String) {
        val input = findFirstAvailable(
            By.res("search_input"),
            By.text("캡처 검색"),
            By.clazz("android.widget.EditText")
        )
        input.click()
        device.waitForIdle()
        input.text = keyword
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.enterCaptureAndSubmit(text: String) {
        val input = findFirstAvailable(
            By.res("capture_input"),
            By.text("떠오르는 생각을 캡처하세요..."),
            By.clazz("android.widget.EditText")
        )
        input.click()
        device.waitForIdle()
        input.text = text

        val submitButton = findFirstAvailable(By.res("capture_submit"), By.desc("전송"))
        submitButton.click()
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.scrollDownRepeatedly(times: Int) {
        repeat(times) {
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(androidx.test.uiautomator.Direction.DOWN, 1.0f)
            } else {
                val width = device.displayWidth
                val height = device.displayHeight
                device.swipe(
                    width / 2,
                    (height * 0.8).toInt(),
                    width / 2,
                    (height * 0.2).toInt(),
                    18
                )
            }
            device.waitForIdle()
        }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.findFirstAvailable(
        vararg selectors: androidx.test.uiautomator.BySelector
    ): androidx.test.uiautomator.UiObject2 {
        selectors.forEach { selector ->
            val obj = device.wait(Until.findObject(selector), 6_000L)
            if (obj != null) return obj
        }
        error("None of the selectors matched.")
    }
}
