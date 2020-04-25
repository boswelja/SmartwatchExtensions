package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.devicemanager.common.Compat
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage

class UtilsTest {

    @Test
    fun setInterruptionFilter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            assertWithMessage("Checking interrupt filter toggle on works")
                    .that(Utils.setInterruptionFilter(context, true))
                    .isEqualTo(true)
            var retryCounter = 0
            // Wait up to 75ms for interrupt filter to change
            while (!Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            assertThat(Compat.isDndEnabled(context)).isEqualTo(true)

            assertWithMessage("Checking interrupt filter toggle off works")
                    .that(Utils.setInterruptionFilter(context, false))
                    .isEqualTo(true)

            retryCounter = 0
            // Wait up to 75ms for interrupt filter to change
            while (Compat.isDndEnabled(context)) {
                retryCounter += 1
                if (retryCounter >= 3) break
                Thread.sleep(25)
            }
            assertThat(Compat.isDndEnabled(context)).isEqualTo(false)
        } else {
            assertWithMessage("No notification policy access, setInterruptFilter should return false")
                    .that(Utils.setInterruptionFilter(context, true))
                    .isEqualTo(false)
        }
    }

    @Test
    fun getAppIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        var sameIcons = Utils.getAppIcon(context, context.packageName).toBitmap()
                .sameAs(context.packageManager.getApplicationIcon(context.packageName).toBitmap())
        assertWithMessage("Checking package icon is correct")
                .that(sameIcons)
                .isEqualTo(true)

        sameIcons = Utils.getAppIcon(context, "").toBitmap()
                .sameAs(context.getDrawable(R.drawable.ic_app_icon_unknown)?.toBitmap())
        assertWithMessage("Checking fallback icon is correct")
                .that(sameIcons)
                .isEqualTo(true)

        sameIcons = Utils.getAppIcon(context, "", context.getDrawable(R.drawable.ic_app_icon_unknown)).toBitmap()
                .sameAs(context.getDrawable(R.drawable.ic_app_icon_unknown)?.toBitmap())
        assertWithMessage("Checking custom fallback drawable is correct")
                .that(sameIcons)
                .isEqualTo(true)
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