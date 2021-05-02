package com.boswelja.smartwatchextensions.common.appmanager

import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Assert
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import timber.log.Timber

class AppTest {

    @Test
    fun isSystemApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemAppInfo = context.packageManager.getPackageInfo("com.android.systemui", 0)
        val systemUiInfo = App(context.packageManager, systemAppInfo)
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)

        expectThat(systemUiInfo.isSystemApp).isTrue()
        expectThat(wearableExtensionsInfo.isSystemApp).isFalse()
    }

    @Test
    fun getHasLaunchActivity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemAppInfo = context.packageManager.getPackageInfo("com.android.systemui", 0)
        val systemUiInfo = App(context.packageManager, systemAppInfo)
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)

        expectThat(systemUiInfo.hasLaunchActivity).isFalse()
        expectThat(wearableExtensionsInfo.hasLaunchActivity).isTrue()
    }

    @Test
    fun byteArrayConversions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)
        try {
            val byteArray = wearableExtensionsInfo.toByteArray()
            val infoFromByteArray = App.fromByteArray(byteArray)

            expectThat(infoFromByteArray.isSystemApp).isEqualTo(wearableExtensionsInfo.isSystemApp)
            expectThat(infoFromByteArray.hasLaunchActivity)
                .isEqualTo(wearableExtensionsInfo.hasLaunchActivity)
            expectThat(infoFromByteArray.installTime).isEqualTo(wearableExtensionsInfo.installTime)
            expectThat(infoFromByteArray.lastUpdateTime)
                .isEqualTo(wearableExtensionsInfo.lastUpdateTime)
            expectThat(infoFromByteArray.label)
                .isEqualTo(wearableExtensionsInfo.label)
            expectThat(infoFromByteArray.packageName).isEqualTo(wearableExtensionsInfo.packageName)
            expectThat(infoFromByteArray.version).isEqualTo(wearableExtensionsInfo.version)
            expectThat(wearableExtensionsInfo.requestedPermissions.toList())
                .containsExactly(infoFromByteArray.requestedPermissions)
        } catch (e: IOException) {
            Timber.e(e)
            Assert.fail("Failed to convert between ByteArray and App")
        }
    }
}
