/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.devicemanager.common.Compat
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class UtilsTest {

    @Test
    fun setInterruptionFilter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            assertWithMessage("Checking interrupt filter toggle on works")
                    .that(Utils.setInterruptionFilter(context, true))
                    .isTrue()
            var retryCounter = 0
            // Wait up to 75ms for interrupt filter to change
            while (!Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            assertThat(Compat.isDndEnabled(context)).isTrue()

            assertWithMessage("Checking interrupt filter toggle off works")
                    .that(Utils.setInterruptionFilter(context, false))
                    .isTrue()

            retryCounter = 0
            // Wait up to 75ms for interrupt filter to change
            while (Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            assertThat(Compat.isDndEnabled(context)).isFalse()
        } else {
            assertWithMessage("No notification policy access, setInterruptFilter should return false")
                    .that(Utils.setInterruptionFilter(context, !Compat.isDndEnabled(context)))
                    .isFalse()
        }
    }

    @Test
    fun getAppIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        var sameIcons = Utils.getAppIcon(context, context.packageName).toBitmap()
                .sameAs(context.packageManager.getApplicationIcon(context.packageName).toBitmap())
        assertWithMessage("Checking package icon is correct")
                .that(sameIcons)
                .isTrue()

        sameIcons = Utils.getAppIcon(context, "").toBitmap()
                .sameAs(context.getDrawable(R.drawable.ic_app_icon_unknown)?.toBitmap())
        assertWithMessage("Checking fallback icon is correct")
                .that(sameIcons)
                .isTrue()

        sameIcons = Utils.getAppIcon(context, "", context.getDrawable(R.drawable.ic_app_icon_unknown)).toBitmap()
                .sameAs(context.getDrawable(R.drawable.ic_app_icon_unknown)?.toBitmap())
        assertWithMessage("Checking custom fallback drawable is correct")
                .that(sameIcons)
                .isTrue()
    }

    @Test
    fun complexTypeDp() {
        val resources = Resources.getSystem()
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
