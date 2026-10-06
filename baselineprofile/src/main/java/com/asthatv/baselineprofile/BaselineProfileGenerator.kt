package com.asthatv.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.asthatv.app",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.waitForIdle()

        // Exercise TV focus paths without requiring network/torrent availability.
        repeat(5) {
            device.pressDPadRight()
            device.waitForIdle(120)
        }
        repeat(2) {
            device.pressDPadDown()
            device.waitForIdle(120)
        }
        repeat(3) {
            device.pressDPadRight()
            device.waitForIdle(120)
        }
        device.pressDPadLeft()
        device.waitForIdle()
    }
}
