package com.boswelja.smartwatchextensions.appmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages.START_SERVICE
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotEqualTo
import strikt.assertions.isTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AppManagerTest {

    private val watch = Watch("Watch 1", "watch-id", "platform")
    private val selectedWatch = MutableLiveData(watch)
    private val app = App(
        null,
        "v1.0.0",
        "com.dummy.app",
        "Dummy App 1",
        isSystemApp = false,
        hasLaunchActivity = false,
        installTime = 0,
        lastUpdateTime = 0,
        requestedPermissions = emptyArray()
    )

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var appManager: AppManager

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        appManager = AppManager(watchManager, TestCoroutineDispatcher())
    }

    @Test
    fun `expectPackages updates state and progress`() {
        appManager.expectPackages(1)
        expectThat(appManager.state.getOrAwaitValue())
            .isEqualTo(State.LOADING_APPS)
        // Progress should be reset to 0
        expectThat(appManager.progress.getOrAwaitValue())
            .isEqualTo(0)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `startAppManagerService updates state, clears app list and sends message`() {
        every { watchManager.selectedWatch } returns selectedWatch
        appManager = AppManager(watchManager, TestCoroutineDispatcher())

        appManager.startAppManagerService()
        expectThat(appManager.state.getOrAwaitValue())
            .isEqualTo(State.CONNECTING)
        expectThat(appManager.userApps.getOrAwaitValue()).isEmpty()
        expectThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
        coVerify { watchManager.sendMessage(watch, START_SERVICE, null) }
    }

    @Test
    fun `startAppManagerService handles null watchId`() {
        // watchId should be inherently null by default, since we don't mock selectedWatch
        appManager.startAppManagerService()
        coVerify(inverse = true) { watchManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `stopAppManagerService handles null watchId`() {
        // watchId should be inherently null by default, since we don't mock selectedWatch
        appManager.stopAppManagerService()
        coVerify(inverse = true) { watchManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `State is connecting on init`() {
        expectThat(appManager.state.getOrAwaitValue())
            .isEqualTo(State.CONNECTING)
    }

    @Test
    fun `Message listener is registered on init and removed on destroy`() {
        verify { watchManager.registerMessageListener(any()) }

        appManager.destroy()
        verify { watchManager.unregisterMessageListener(any()) }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `selectedWatch is observed for the lifetime of AppManager`() {
        every { watchManager.selectedWatch } returns selectedWatch
        appManager = AppManager(watchManager, TestCoroutineDispatcher())

        expectThat(selectedWatch.hasActiveObservers()).isTrue()

        appManager.destroy()
        expectThat(selectedWatch.hasObservers()).isFalse()
    }

    @Test
    fun `serviceRunning moves state to READY if we've received all expected packages`() {
        // expectedPackageCount is 0 by default, and _apps.count() should also be 0
        appManager.serviceRunning()
        expectThat(appManager.state.getOrAwaitValue()).isEqualTo(State.READY)
    }

    @Test
    fun `serviceRunning doesn't change state if we're still waiting for packages`() {
        // _apps.count() should be 0 by default
        appManager.expectPackages(1)
        appManager.serviceRunning()
        expectThat(appManager.state.getOrAwaitValue()).isNotEqualTo(State.READY)
    }

    @Test
    fun `addPackage updates apps once all expected packages are received`() {
        // expectedPackageCount should be 0 by default
        appManager.addPackage(app)
        expectThat(appManager.userApps.getOrAwaitValue()).containsExactly(app)
    }

    @Test
    fun `addPackage updates progress while receiving expected packages`() {
        appManager.expectPackages(10)
        appManager.addPackage(app)
        expectThat(appManager.progress.getOrAwaitValue()).isEqualTo(10)
    }

    @Test
    fun `addPackage sets state to READY if all expected packages are received`() {
        // expectedPackageCount should be 0 by default
        appManager.addPackage(app)
        expectThat(appManager.state.getOrAwaitValue()).isEqualTo(State.READY)
    }

    @Test
    fun `removePackage correctly removes an app`() {
        appManager.addPackage(app)
        appManager.removePackage(app.packageName)
        expectThat(appManager.userApps.getOrAwaitValue()).isEmpty()
        val newSystemApp = App(
            null,
            "v2.0.0",
            "com.dummy.app",
            "Dummy App 1",
            isSystemApp = true,
            hasLaunchActivity = false,
            installTime = 0,
            lastUpdateTime = 10,
            requestedPermissions = emptyArray()
        )
        appManager.addPackage(newSystemApp)
        appManager.removePackage(newSystemApp.packageName)
        expectThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun `removePackage handles app that hasn't been added`() {
        appManager.removePackage(app.packageName)
        expectThat(appManager.userApps.getOrAwaitValue()).isEmpty()
        expectThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
    }
}
