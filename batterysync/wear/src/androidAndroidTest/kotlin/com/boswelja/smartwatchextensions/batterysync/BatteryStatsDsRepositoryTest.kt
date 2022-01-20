package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BatteryStatsDsRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getPhoneBatteryStats_flowsUpdates() = runBlocking {
        val repository = BatteryStatsDsRepository(context)
        val initial = BatteryStats(50, false, 0)
        repository.updatePhoneBatteryStats(initial)
        repository.getPhoneBatteryStats().test {
            // Check initial value
            assertEquals(initial, awaitItem())

            // Simulate charging
            (0 until 100).forEach {
                val stats = BatteryStats(it, true, System.currentTimeMillis())
                repository.updatePhoneBatteryStats(stats)
                assertEquals(stats, awaitItem())
            }

            // Simulate discharging
            (100 downTo  0).forEach {
                val stats = BatteryStats(it, false, System.currentTimeMillis())
                repository.updatePhoneBatteryStats(stats)
                assertEquals(stats, awaitItem())
            }
        }
    }
}
