package com.boswelja.smartwatchextensions.batterysync

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseBatterySyncNotificationHandlerTest {

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun handleNotificationsFor_cancelsNotificationsWhenNoNotificationsEnabled() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            50,
            false,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = false,
            lowNotificationEnabled = false,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = true
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify { notificationHandler.cancelNotificationFor(targetUid) }
    }

    @Test
    fun handleNotificationsFor_cancelsNotificationsWhenNotificationConditionsNotMet() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            50,
            false,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = true,
            lowNotificationEnabled = true,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = false
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify { notificationHandler.cancelNotificationFor(targetUid) }
    }

    @Test
    fun handleNotificationsFor_postsChargeNotificationOnConditionsMet() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            95,
            true,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = true,
            lowNotificationEnabled = false,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = false
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify { notificationHandler.postChargeNotificationFor(targetUid, batteryStats) }
    }
    @Test
    fun handleNotificationsFor_doesNotPostDuplicateChargeNotifications() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            95,
            true,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = true,
            lowNotificationEnabled = false,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = true
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify(inverse = true) { notificationHandler.postChargeNotificationFor(targetUid, batteryStats) }
    }

    @Test
    fun handleNotificationsFor_postsLowNotificationOnConditionsMet() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            5,
            false,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = false,
            lowNotificationEnabled = true,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = false
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify { notificationHandler.postLowNotificationFor(targetUid, batteryStats) }
    }

    @Test
    fun handleNotificationsFor_doesNotPostDuplicateLowNotifications() = runTest {
        val targetUid = "uid"
        val batteryStats = BatteryStats(
            5,
            false,
            1
        )
        val notificationHandler = createBatterySyncNotificationHandler(
            chargeNotificationEnabled = false,
            lowNotificationEnabled = true,
            chargeThreshold = 90,
            lowThreshold = 10,
            alreadyNotified = true
        )

        notificationHandler.handleNotificationsFor(targetUid, batteryStats)

        coVerify(inverse = true) { notificationHandler.postLowNotificationFor(targetUid, batteryStats) }
    }

    private fun createBatterySyncNotificationHandler(
        chargeNotificationEnabled: Boolean,
        lowNotificationEnabled: Boolean,
        chargeThreshold: Int,
        lowThreshold: Int,
        alreadyNotified: Boolean
    ): BaseBatterySyncNotificationHandler {
        return spyk {
            coEvery { getChargeNotificationsEnabled(any()) } returns chargeNotificationEnabled
            coEvery { getLowNotificationsEnabled(any()) } returns lowNotificationEnabled
            coEvery { getChargeThreshold(any()) } returns chargeThreshold
            coEvery { getLowThreshold(any()) } returns lowThreshold
            coEvery { getNotificationAlreadySent(any()) } returns alreadyNotified
            coEvery { postChargeNotificationFor(any(), any()) } just Runs
            coEvery { postLowNotificationFor(any(), any()) } just Runs
            coEvery { cancelNotificationFor(any()) } just Runs
        }
    }
}
