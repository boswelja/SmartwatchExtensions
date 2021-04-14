/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.smartwatchextensions.common.appmanager

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.IOException
import org.junit.Test
import timber.log.Timber

class AppTest {

    @Test
    fun isSystemApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemAppInfo = context.packageManager.getPackageInfo("com.android.systemui", 0)
        val systemUiInfo = App(context.packageManager, systemAppInfo)
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)

        assertThat(systemUiInfo.isSystemApp).isTrue()
        assertThat(wearableExtensionsInfo.isSystemApp).isFalse()
    }

    @Test
    fun getHasLaunchActivity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val systemAppInfo = context.packageManager.getPackageInfo("com.android.systemui", 0)
        val systemUiInfo = App(context.packageManager, systemAppInfo)
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)

        assertThat(systemUiInfo.hasLaunchActivity).isFalse()
        assertThat(wearableExtensionsInfo.hasLaunchActivity).isTrue()
    }

    @Test
    fun byteArrayConversions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userAppInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val wearableExtensionsInfo = App(context.packageManager, userAppInfo)
        try {
            val byteArray = wearableExtensionsInfo.toByteArray()
            val infoFromByteArray = App.fromByteArray(byteArray)

            assertThat(infoFromByteArray.isSystemApp).isEqualTo(wearableExtensionsInfo.isSystemApp)
            assertThat(infoFromByteArray.hasLaunchActivity)
                .isEqualTo(wearableExtensionsInfo.hasLaunchActivity)
            assertThat(infoFromByteArray.installTime).isEqualTo(wearableExtensionsInfo.installTime)
            assertThat(infoFromByteArray.lastUpdateTime)
                .isEqualTo(wearableExtensionsInfo.lastUpdateTime)
            assertThat(infoFromByteArray.label)
                .isEqualTo(wearableExtensionsInfo.label)
            assertThat(infoFromByteArray.packageName).isEqualTo(wearableExtensionsInfo.packageName)
            assertThat(infoFromByteArray.version).isEqualTo(wearableExtensionsInfo.version)
            assertThat(wearableExtensionsInfo.requestedPermissions)
                .asList()
                .containsExactly(infoFromByteArray.requestedPermissions)
                .inOrder()
        } catch (e: IOException) {
            Timber.e(e)
            assertWithMessage("Failed to convert between ByteArray and AppPackageInfo").fail()
        }
    }
}
