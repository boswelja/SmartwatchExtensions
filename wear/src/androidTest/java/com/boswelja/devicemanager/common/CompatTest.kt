/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

class CompatTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(ENABLED_CHANNEL_ID, "Enabled Test Channel", NotificationManager.IMPORTANCE_DEFAULT).also {
            notificationManager.createNotificationChannel(it)
        }
        NotificationChannel(DISABLED_CHANNEL_ID, "Disabled Test Channel", NotificationManager.IMPORTANCE_NONE).also {
            notificationManager.createNotificationChannel(it)
        }
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(ENABLED_CHANNEL_ID)
        notificationManager.deleteNotificationChannel(DISABLED_CHANNEL_ID)
    }

    @Test
    fun isDndEnabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertWithMessage("We can't normally have notification policy access on Wear OS")
                .that(Compat.setInterruptionFilter(context, true))
                .isFalse()
    }

    @Test
    fun areNotificationsEnabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertWithMessage("Checking overall notification status is correct")
                .that(Compat.areNotificationsEnabled(context))
                .isEqualTo(NotificationManagerCompat.from(context).areNotificationsEnabled())

        assertWithMessage("Checking notification channel enabled status is correct")
                .that(Compat.areNotificationsEnabled(context, ENABLED_CHANNEL_ID))
                .isEqualTo(true)
        assertWithMessage("Checking notification channel disabled status is correct")
                .that(Compat.areNotificationsEnabled(context, DISABLED_CHANNEL_ID))
                .isEqualTo(false)
    }

    companion object {
        private const val ENABLED_CHANNEL_ID = "enabled_test_noti_channel"
        private const val DISABLED_CHANNEL_ID = "disabled_test_noti_channel"
    }
}
