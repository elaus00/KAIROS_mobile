package com.flit.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flit.app.benchmark.TARGET_PACKAGE
import com.flit.app.benchmark.dismissOnboardingIfVisible
import com.flit.app.benchmark.openIdeasFolder
import com.flit.app.benchmark.scrollDownRepeatedly
import com.flit.app.benchmark.tapNotesTab
import com.flit.app.benchmark.waitForHomeReady
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
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            dismissOnboardingIfVisible()
            waitForHomeReady()

            tapNotesTab()
            openIdeasFolder()
            scrollDownRepeatedly(times = 4)
            device.pressBack()
        }
    }
}
