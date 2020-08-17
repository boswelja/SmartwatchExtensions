/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.info

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.boswelja.devicemanager.appmanager.ui.getOrAwaitValue
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_UNINSTALL_PACKAGE
import com.google.android.gms.wearable.MessageClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class AppPackageInfoViewModelTest {

    private val watchId = "123456"

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: AppPackageInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = AppPackageInfoViewModel(messageClient)
        viewModel.watchId = watchId
    }

    @Test
    fun `Setting App Info correctly updates view transforms`() {
        val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())
        val testAppInfo = getTestApp(isSystemApp = false, isLaunchable = true)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.appName.getOrAwaitValue {
            assertThat(it).isEqualTo(testAppInfo.packageLabel)
        }
        viewModel.appIcon.getOrAwaitValue {
            assertThat(it).isEqualTo(testAppInfo.packageIcon)
        }
        viewModel.installTime.getOrAwaitValue {
            assertThat(it).isEqualTo(dateFormatter.format(testAppInfo.installTime))
        }
        viewModel.lastUpdateTime.getOrAwaitValue {
            assertThat(it).isEqualTo(dateFormatter.format(testAppInfo.lastUpdateTime))
        }
        viewModel.versionText.getOrAwaitValue {
            assertThat(it).isEqualTo(testAppInfo.versionName ?: testAppInfo.versionCode.toString())
        }
        viewModel.shouldShowLastUpdateTime.getOrAwaitValue {
            assertThat(it).isEqualTo(testAppInfo.installTime != testAppInfo.lastUpdateTime)
        }
    }

    @Test
    fun `Setting launchable system App Info correctly updates transforms`() {
        val testAppInfo = getTestApp(isSystemApp = true, isLaunchable = true)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.canOpen.getOrAwaitValue {
            assertThat(it).isTrue()
        }
        viewModel.canUninstall.getOrAwaitValue {
            assertThat(it).isFalse()
        }
        viewModel.shouldShowInstallTime.getOrAwaitValue {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `Setting non-launchable system App Info correctly updates transforms`() {
        val testAppInfo = getTestApp(isSystemApp = true, isLaunchable = false)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.canOpen.getOrAwaitValue {
            assertThat(it).isFalse()
        }
        viewModel.canUninstall.getOrAwaitValue {
            assertThat(it).isFalse()
        }
        viewModel.shouldShowInstallTime.getOrAwaitValue {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `Setting launchable user App Info correctly updates transforms`() {
        val testAppInfo = getTestApp(isSystemApp = false, isLaunchable = true)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.canOpen.getOrAwaitValue {
            assertThat(it).isTrue()
        }
        viewModel.canUninstall.getOrAwaitValue {
            assertThat(it).isTrue()
        }
        viewModel.shouldShowInstallTime.getOrAwaitValue {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `Setting non-launchable user App Info correctly updates transforms`() {
        val testAppInfo = getTestApp(isSystemApp = false, isLaunchable = false)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.canOpen.getOrAwaitValue {
            assertThat(it).isFalse()
        }
        viewModel.canUninstall.getOrAwaitValue {
            assertThat(it).isTrue()
        }
        viewModel.shouldShowInstallTime.getOrAwaitValue {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `Uninstall app makes a request and finished activity`() {
        val testAppInfo = getTestApp(isSystemApp = false, isLaunchable = true)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.sendUninstallRequestMessage()
        verify(exactly = 1) {
            messageClient.sendMessage(
                watchId,
                REQUEST_UNINSTALL_PACKAGE,
                testAppInfo.packageName.toByteArray(Charsets.UTF_8)
            )
        }
        viewModel.finishActivity.getOrAwaitValue {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `Open app request sends`() {
        val testAppInfo = getTestApp(isSystemApp = true, isLaunchable = true)
        viewModel.appInfo.postValue(testAppInfo)
        viewModel.sendOpenRequestMessage()
        verify(exactly = 1) {
            messageClient.sendMessage(
                watchId,
                REQUEST_OPEN_PACKAGE,
                testAppInfo.packageName.toByteArray(Charsets.UTF_8)
            )
        }
    }

    private fun getTestApp(isSystemApp: Boolean, isLaunchable: Boolean) =
        AppPackageInfo(
            null,
            1,
            "1.0.0",
            "com.package.name",
            "Label",
            isSystemApp,
            isLaunchable,
            installTime = System.currentTimeMillis(),
            lastUpdateTime = System.currentTimeMillis(),
            arrayOf("permission1", "permission2")
        )
}
