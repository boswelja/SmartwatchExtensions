package com.boswelja.smartwatchextensions.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class CompatTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create notification channels
        NotificationChannel(
            ENABLED_CHANNEL_ID,
            "Enabled Test Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).also { notificationManager.createNotificationChannel(it) }
        NotificationChannel(
            DISABLED_CHANNEL_ID,
            "Disabled Test Channel",
            NotificationManager.IMPORTANCE_NONE
        ).also { notificationManager.createNotificationChannel(it) }
    }

    @After
    fun tearDown() {
        // Delete notification channels
        notificationManager.deleteNotificationChannel(ENABLED_CHANNEL_ID)
        notificationManager.deleteNotificationChannel(DISABLED_CHANNEL_ID)
    }

    @Test
    fun isDndEnabled() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Compat.setInterruptionFilter(context, true)
            var retryCounter = 0
            while (!Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            expectThat(Compat.isDndEnabled(context)).isTrue()

            Compat.setInterruptionFilter(context, false)
            retryCounter = 0
            while (Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            expectThat(Compat.isDndEnabled(context)).isFalse()
        } else {
            Assert.fail("Missing notification policy access, grant and try again")
        }
    }

    companion object {
        private const val ENABLED_CHANNEL_ID = "enabled_test_noti_channel"
        private const val DISABLED_CHANNEL_ID = "disabled_test_noti_channel"
    }
}
