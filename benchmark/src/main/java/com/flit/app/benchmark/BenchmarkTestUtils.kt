package com.flit.app.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.flit.app"
private const val WAIT_TIMEOUT_MS = 6_000L
private const val CLASSIFICATION_WAIT_MS = 3_000L

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
    clickFirstAvailable(
        By.res(TARGET_PACKAGE, "tab_notes"),
        By.res("tab_notes"),
        By.desc("노트"),
        By.text("노트"),
        By.desc("Notes"),
        By.text("Notes")
    )
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.tapHomeTab() {
    clickFirstAvailable(
        By.res(TARGET_PACKAGE, "tab_home"),
        By.res("tab_home"),
        By.desc("홈"),
        By.text("홈"),
        By.desc("Home"),
        By.text("Home")
    )
    // Pager 전환 반영 대기
    device.wait(Until.findObject(By.res(TARGET_PACKAGE, "capture_input")), WAIT_TIMEOUT_MS)
        ?: device.wait(Until.findObject(By.res("capture_input")), WAIT_TIMEOUT_MS)
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

internal fun MacrobenchmarkScope.openSearchScreenFromNotes() {
    clickFirstAvailable(
        By.res(TARGET_PACKAGE, "search_button"),
        By.res("search_button"),
        By.desc("검색"),
        By.text("검색"),
        By.descContains("검색"),
        By.desc("Search"),
        By.text("Search")
    )
}

internal fun MacrobenchmarkScope.enterCaptureAndSubmit(text: String) {
    val input = findFirstAvailable(
        By.res(TARGET_PACKAGE, "capture_input"),
        By.res("capture_input"),
        By.text("떠오르는 생각을 자유롭게..."),
        By.textContains("떠오르는 생각"),
        By.text("떠오르는 생각을 캡처하세요..."),
        By.clazz("android.widget.EditText")
    )
    input.click()
    device.waitForIdle()
    runCatching { input.text = text }
        .onFailure {
            // Compose 재합성으로 객체 핸들이 무효화될 수 있어 재조회 후 입력
            findFirstAvailable(
                By.res(TARGET_PACKAGE, "capture_input"),
                By.res("capture_input"),
                By.clazz("android.widget.EditText")
            ).text = text
        }

    val submitButton = findFirstAvailableOrNull(
        By.res(TARGET_PACKAGE, "capture_submit"),
        By.res("capture_submit"),
        By.desc("전송"),
        By.text("전송")
    )
    if (submitButton != null) {
        submitButton.click()
    } else {
        // 테스트 태그/콘텐츠 설명이 누락된 경우를 대비한 좌표 fallback (우하단 전송 버튼 위치)
        val width = device.displayWidth
        val height = device.displayHeight
        device.click((width * 0.92f).toInt(), (height * 0.92f).toInt())
    }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.enterFirstCharacter() {
    repeat(3) { attempt ->
        val input = findFirstAvailable(
            By.res(TARGET_PACKAGE, "capture_input"),
            By.res("capture_input"),
            By.text("떠오르는 생각을 자유롭게..."),
            By.textContains("떠오르는 생각"),
            By.text("떠오르는 생각을 캡처하세요..."),
            By.clazz("android.widget.EditText")
        )
        input.click()
        device.waitForIdle()
        val success = runCatching {
            input.text = "a"
            true
        }.getOrElse { false }
        if (success) {
            device.waitForIdle()
            return
        }
    }
    error("Failed to type first character into capture input after retries.")
}

internal fun MacrobenchmarkScope.enterSearchKeyword(keyword: String) {
    repeat(3) { attempt ->
        val input = findFirstAvailable(
            By.res(TARGET_PACKAGE, "search_input"),
            By.res("search_input"),
            By.text("캡처 검색"),
            By.textContains("검색"),
            By.clazz("android.widget.EditText")
        )
        input.click()
        device.waitForIdle()
        val success = runCatching {
            input.text = keyword
            true
        }.getOrElse { false }
        if (success) return
        if (attempt < 2) device.waitForIdle()
    }
    error("Failed to type into search input after retries.")
}

private fun MacrobenchmarkScope.findFirstAvailableOrNull(vararg selectors: BySelector): UiObject2? {
    selectors.forEach { selector ->
        val obj = device.wait(Until.findObject(selector), WAIT_TIMEOUT_MS)
        if (obj != null) return obj
    }
    return null
}

private fun MacrobenchmarkScope.clickFirstAvailable(vararg selectors: BySelector) {
    findFirstAvailable(*selectors).click()
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
    // Mock API 분류 지연 + WorkManager 스케줄링 + DB 저장 여유 시간을 포함한다.
    SystemClock.sleep(CLASSIFICATION_WAIT_MS)
}

private fun MacrobenchmarkScope.findFirstAvailable(vararg selectors: BySelector): UiObject2 {
    return findFirstAvailableOrNull(*selectors)
        ?: error("None of the selectors matched.")
}
