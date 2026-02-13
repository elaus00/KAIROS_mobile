package com.flit.app.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.flit.app"
private const val WAIT_TIMEOUT_MS = 6_000L

internal fun MacrobenchmarkScope.dismissOnboardingIfVisible() {
    device.wait(Until.findObject(By.text("건너뛰기")), 1_500L)?.click()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.waitForHomeReady() {
    val homeIndicator = device.wait(
        Until.findObject(By.res(TARGET_PACKAGE, "tab_home")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.res("tab_home")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.desc("홈")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.text("홈")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.desc("Home")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.res(TARGET_PACKAGE, "capture_input")),
        WAIT_TIMEOUT_MS
    ) ?: device.wait(
        Until.findObject(By.res("capture_input")),
        WAIT_TIMEOUT_MS
    )
    checkNotNull(homeIndicator) { "Home screen did not load in time." }
}

internal fun MacrobenchmarkScope.tapNotesTab() {
    val notesTab = findFirstAvailableOrNull(
        By.res(TARGET_PACKAGE, "tab_notes"),
        By.res("tab_notes"),
        By.res(TARGET_PACKAGE, "tab_notes_button"),
        By.res("tab_notes_button"),
        By.descContains("노트"),
        By.desc("노트"),
        By.text("노트"),
        By.descContains("Notes"),
        By.desc("Notes"),
        By.text("Notes")
    )
    if (notesTab != null) {
        notesTab.click()
    } else {
        val width = device.displayWidth
        val height = device.displayHeight
        device.click((width * 0.22f).toInt(), (height * 0.95f).toInt())
    }
    val enteredNotes = device.wait(Until.findObject(By.text("노트")), WAIT_TIMEOUT_MS)
        ?: device.wait(Until.findObject(By.desc("검색")), WAIT_TIMEOUT_MS)
    if (enteredNotes == null) {
        val width = device.displayWidth
        val height = device.displayHeight
        device.click((width * 0.22f).toInt(), (height * 0.95f).toInt())
    }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.openIdeasFolder() {
    clickFirstAvailable(
        By.res(TARGET_PACKAGE, "notes_folder_system-ideas"),
        By.res("notes_folder_system-ideas"),
        By.text("아이디어"),
        By.textContains("아이디어"),
        By.text("Ideas")
    )
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

private fun MacrobenchmarkScope.findFirstAvailableOrNull(vararg selectors: BySelector): UiObject2? {
    selectors.forEach { selector ->
        val obj = device.wait(Until.findObject(selector), WAIT_TIMEOUT_MS)
        if (obj != null) return obj
    }
    return null
}

private fun MacrobenchmarkScope.clickFirstAvailable(vararg selectors: BySelector) {
    findFirstAvailableOrNull(*selectors)?.click()
        ?: error("None of the selectors matched.")
    device.waitForIdle()
}
