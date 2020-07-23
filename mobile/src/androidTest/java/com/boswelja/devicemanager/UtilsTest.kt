/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.boswelja.devicemanager.common.Compat
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test

class UtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val notiPolicyPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_NOTIFICATION_POLICY)

    @Test
    fun setInterruptionFilter() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            assertWithMessage("Checking interrupt filter toggle on works")
                .that(Compat.setInterruptionFilter(context, true))
                .isTrue()
            assertWithMessage("Checking interrupt filter toggle off works")
                .that(Compat.setInterruptionFilter(context, false))
                .isTrue()
        } else {
            assertWithMessage("No notification policy access, setInterruptFilter should return false")
                .that(Compat.setInterruptionFilter(context, !Compat.isDndEnabled(context)))
                .isFalse()
        }
    }

    @Test
    fun complexTypeDp() {
        val resources = context.resources
        assertThat(Utils.complexTypeDp(resources, 4f))
            .isEqualTo((4f * resources.displayMetrics.density))

        assertThat(Utils.complexTypeDp(resources, 2f))
            .isEqualTo((2f * resources.displayMetrics.density))

        assertThat(Utils.complexTypeDp(resources, 16f))
            .isEqualTo((16f * resources.displayMetrics.density))

        assertThat(Utils.complexTypeDp(resources, 32f))
            .isEqualTo((32f * resources.displayMetrics.density))
    }
}
