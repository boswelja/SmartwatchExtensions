package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BatterySyncStateDsRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getPhoneBatteryStats_flowsUpdates() = runBlocking {
        val repository = BatterySyncStateDsRepository(context)
        val initial = BatterySyncState(
            batterySyncEnabled = false,
            phoneChargeNotificationEnabled = false,
            phoneLowNotificationEnabled = false,
            phoneChargeThreshold = 90,
            phoneLowThreshold = 15,
            notificationPosted = false
        )
        repository.updateBatterySyncState { initial }
        repository.getBatterySyncState().test {
            // Check initial value
            assertEquals(initial, awaitItem())

            val updatedItem = BatterySyncState(
                batterySyncEnabled = true,
                phoneChargeNotificationEnabled = true,
                phoneLowNotificationEnabled = true,
                phoneChargeThreshold = 80,
                phoneLowThreshold = 20,
                notificationPosted = true
            )
            repository.updateBatterySyncState { updatedItem }
            assertEquals(updatedItem, awaitItem())
        }
    }
}
