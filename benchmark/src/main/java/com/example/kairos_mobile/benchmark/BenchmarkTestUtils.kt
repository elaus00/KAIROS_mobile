package com.example.kairos_mobile.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.example.kairos_mobile"
private const val WAIT_TIMEOUT_MS = 6_000L

internal fun MacrobenchmarkScope.dismissOnboardingIfVisible() {
    device.wait(Until.findObject(By.text("건너뛰기")), 1_500L)?.click()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.waitForHomeReady() {
    val homeIndicator = device.wait(
        Until.findObject(By.res("tab_home")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.desc("Home")),
        WAIT_TIMEOUT_MS
    )
    checkNotNull(homeIndicator) { "Home screen did not load in time." }
}

internal fun MacrobenchmarkScope.tapNotesTab() {
    clickFirstAvailable(
        By.res("tab_notes"),
        By.desc("Notes"),
        By.text("Notes")
    )
}

internal fun MacrobenchmarkScope.tapHomeTab() {
    clickFirstAvailable(
        By.res("tab_home"),
        By.desc("Home"),
        By.text("Home")
    )
}

internal fun MacrobenchmarkScope.openIdeasFolder() {
    clickFirstAvailable(
        By.res("notes_folder_system-ideas"),
        By.text("Ideas")
    )
}

internal fun MacrobenchmarkScope.openSearchScreenFromNotes() {
    clickFirstAvailable(By.desc("검색"))
}

internal fun MacrobenchmarkScope.enterCaptureAndSubmit(text: String) {
    val input = findFirstAvailable(
        By.res("capture_input"),
        By.text("떠오르는 생각을 캡처하세요..."),
        By.clazz("android.widget.EditText")
    )
    input.click()
    device.waitForIdle()
    input.text = text

    val submitButton = findFirstAvailable(
        By.res("capture_submit"),
        By.desc("전송")
    )
    submitButton.click()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.enterFirstCharacter() {
    val input = findFirstAvailable(
        By.res("capture_input"),
        By.text("떠오르는 생각을 캡처하세요..."),
        By.clazz("android.widget.EditText")
    )
    input.click()
    device.waitForIdle()
    input.text = "a"
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.enterSearchKeyword(keyword: String) {
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

internal fun MacrobenchmarkScope.scrollDownRepeatedly(times: Int) {
    repeat(times) {
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null) {
            scrollable.scroll(Direction.DOWN, 1.0f)
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

internal fun MacrobenchmarkScope.waitForClassificationCompletionWindow() {
    // Mock API의 분류 지연(약 500ms) + 워커 스케줄링 여유 시간을 포함한다.
    SystemClock.sleep(2_000L)
}

private fun MacrobenchmarkScope.clickFirstAvailable(vararg selectors: BySelector) {
    findFirstAvailable(*selectors).click()
    device.waitForIdle()
}

private fun MacrobenchmarkScope.findFirstAvailable(vararg selectors: BySelector): UiObject2 {
    selectors.forEach { selector ->
        val obj = device.wait(Until.findObject(selector), WAIT_TIMEOUT_MS)
        if (obj != null) return obj
    }
    error("None of the selectors matched.")
}
