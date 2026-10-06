package com.asthatv.benchmark

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TvNavigationBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun dpadFrameTiming() = rule.measureRepeated(
        packageName = "com.asthatv.app",
        metrics = listOf(FrameTimingMetric()),
        iterations = 6,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        setupBlock = {
            pressHome()
            startActivityAndWait()
        }
    ) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        repeat(8) {
            device.pressDPadRight()
            device.waitForIdle(90)
        }
        repeat(3) {
            device.pressDPadDown()
            device.waitForIdle(90)
        }
        repeat(6) {
            device.pressDPadLeft()
            device.waitForIdle(90)
        }
    }
}
