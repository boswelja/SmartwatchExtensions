package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.message.MessageClient
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

class PhoneBatteryUpdateReceiverTest {

    @get:Rule
    val koinTest = KoinTestRule.create {
        modules(
            module {
                factory { messageClient }
                factory { batteryStatsRepository }
                factory { batterySyncStateRepository }
                factory { batterySyncNotificationHandler }
            }
        )
    }

    private lateinit var messageClient: MessageClient
    private lateinit var batteryStatsRepository: BatteryStatsRepository
    private lateinit var batterySyncStateRepository: BatterySyncStateRepository
    private lateinit var batterySyncNotificationHandler: BatterySyncNotificationHandler

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        messageClient = mockk {
            coEvery { sendMessage(any(), any()) } returns true
        }
        batteryStatsRepository = mockk {
            coEvery { updatePhoneBatteryStats(any()) } just Runs
        }
        batterySyncStateRepository = mockk {
            // Mock battery sync enabled
            every { getBatterySyncState() } returns flowOf(
                BatterySyncState(
                    batterySyncEnabled = true,
                    phoneChargeNotificationEnabled = true,
                    phoneLowNotificationEnabled = true,
                    phoneChargeThreshold = 90,
                    phoneLowThreshold = 15,
                    notificationPosted = false
                )
            )
        }
        batterySyncNotificationHandler = mockk {
            coEvery { handleNotificationsFor(any(), any()) } just Runs
        }
        // Fix for crash caused by ComplicationDataSourceUpdateRequester::requestUpdateAll
        mockkObject(PhoneBatteryComplicationProvider.Companion)
        every { PhoneBatteryComplicationProvider.updateAll(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onMessageReceived_ignoresMessageWhenBatterySyncDisabled() = runBlocking {
        val receiver = PhoneBatteryUpdateReceiver()

        // Mock battery sync disabled
        every { batterySyncStateRepository.getBatterySyncState() } returns flowOf(
            BatterySyncState(
                batterySyncEnabled = false,
                phoneChargeNotificationEnabled = true,
                phoneLowNotificationEnabled = true,
                phoneChargeThreshold = 90,
                phoneLowThreshold = 15,
                notificationPosted = false
            )
        )

        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                "uid",
                BatteryStatus,
                BatteryStats(0, false, 0)
            )
        )

        coVerify(inverse = true) {
            batteryStatsRepository.updatePhoneBatteryStats(any())
            batterySyncNotificationHandler.handleNotificationsFor(any(), any())
            messageClient.sendMessage(any(), any())
        }
    }

    @Test
    fun onMessageReceived_sendsNewStatsToRepository() = runBlocking {
        val receiver = PhoneBatteryUpdateReceiver()
        val batteryStats = BatteryStats(50, false, 0)
        val uid = Watch.createUid("platform", "uid")

        receiver.onMessageReceived(context, ReceivedMessage(uid, BatteryStatus, batteryStats))

        coVerify { batteryStatsRepository.updatePhoneBatteryStats(batteryStats) }
    }

    @Test
    fun onMessageReceived_startsNotificationHandler() = runBlocking {
        val receiver = PhoneBatteryUpdateReceiver()
        val batteryStats = BatteryStats(50, false, 0)
        val uid = Watch.createUid("platform", "uid")

        receiver.onMessageReceived(context, ReceivedMessage(uid, BatteryStatus, batteryStats))

        coVerify { batterySyncNotificationHandler.handleNotificationsFor(uid, batteryStats) }
    }

    @Test
    fun onMessageReceived_sendsBatteryStatsUpdate() = runBlocking {
        val receiver = PhoneBatteryUpdateReceiver()
        val uid = Watch.createUid("platform", "uid")

        receiver.onMessageReceived(
            context,
            ReceivedMessage(uid, BatteryStatus, BatteryStats(50, false, 0))
        )

        coVerify {
            messageClient.sendMessage(
                uid,
                match {
                    it.path == BatteryStatus && it.data != null
                }
            )
        }
    }
}
