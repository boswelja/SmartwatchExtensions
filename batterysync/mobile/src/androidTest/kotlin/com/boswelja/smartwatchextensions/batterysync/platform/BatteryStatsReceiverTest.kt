package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.watchconnection.common.message.ReceivedMessage
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryStatsReceiverTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory { batteryStatsRepository }
                factory { batterySyncNotificationHandler }
            }
        )
    }

    private lateinit var batteryStatsRepository: BatteryStatsRepository
    private lateinit var batterySyncNotificationHandler: BatterySyncNotificationHandler
    private lateinit var context: Context

    @Before
    fun setUp() {
        batteryStatsRepository = mockk {
            coEvery { updateStatsFor(any(), any()) } just Runs
        }
        batterySyncNotificationHandler = mockk {
            coEvery { handleNotificationsFor(any(), any()) } just Runs
        }

        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onMessageReceived_storesBatteryStats() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            50,
            true,
            1
        )
        val receiver = BatteryStatsReceiver()

        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                targetUid,
                BatteryStatus,
                batteryStats
            )
        )

        coVerify {
            batteryStatsRepository.updateStatsFor(targetUid, batteryStats)
        }
    }

    @Test
    fun onMessageReceived_callsNotificationHandler() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            50,
            true,
            1
        )
        val receiver = BatteryStatsReceiver()

        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                targetUid,
                BatteryStatus,
                batteryStats
            )
        )

        coVerify {
            batterySyncNotificationHandler.handleNotificationsFor(targetUid, batteryStats)
        }
    }
}
