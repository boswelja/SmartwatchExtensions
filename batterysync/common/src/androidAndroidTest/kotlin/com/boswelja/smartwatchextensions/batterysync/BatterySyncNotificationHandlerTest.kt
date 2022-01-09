package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler.Companion.NOTIFICATION_CHANNEL_ID
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncNotificationHandlerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun postChargeNotificationFor_postsNotification() = runTest {
        createNotificationHandler("Device")
            .postChargeNotificationFor("uid", BatteryStats(90, true, 1))
        assertTrue(notificationManager.activeNotifications.isNotEmpty())
    }

    @Test
    fun postLowNotificationFor_postsNotification() = runTest {
        createNotificationHandler("Device")
            .postLowNotificationFor("uid", BatteryStats(10, false, 1))
        assertTrue(notificationManager.activeNotifications.isNotEmpty())
    }

    @Test
    fun postChargeNotificationFor_createsChannel() = runTest {
        createNotificationHandler("Device")
            .postChargeNotificationFor("uid", BatteryStats(90, true, 1))
        assertNotNull(notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID))
    }

    @Test
    fun postLowNotificationFor_createsChannel() = runTest {
        createNotificationHandler("Device")
            .postLowNotificationFor("uid", BatteryStats(10, false, 1))
        assertNotNull(notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID))
    }

    private fun createNotificationHandler(
        deviceName: String
    ): BatterySyncNotificationHandler {
        val handler = object : BatterySyncNotificationHandler(context, notificationManager) {
            override suspend fun onNotificationPosted(targetUid: String) {
                // Ignored
            }
            override suspend fun onNotificationCancelled(targetUid: String) {
                // Ignored
            }
            override suspend fun getDeviceName(targetUid: String): String = deviceName
            override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean = true
            override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean = true
            override suspend fun getChargeThreshold(targetUid: String): Int = DefaultValues.CHARGE_THRESHOLD
            override suspend fun getLowThreshold(targetUid: String): Int = DefaultValues.LOW_THRESHOLD
            override suspend fun getNotificationAlreadySent(targetUid: String): Boolean = false
        }
        return spyk(handler)
    }
}
