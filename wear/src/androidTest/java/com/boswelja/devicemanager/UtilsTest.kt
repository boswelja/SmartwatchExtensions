package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilsTest {

    @Test
    fun checkDnDAccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertThat(Utils.checkDnDAccess(context))
                .isEqualTo(notificationManager.isNotificationPolicyAccessGranted)
    }

    @Test
    fun isAppInstalled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(Utils.isAppInstalled(context.packageManager, context.packageName))
                .isTrue()
    }
}