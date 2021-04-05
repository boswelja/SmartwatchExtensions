package com.boswelja.devicemanager.appmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages.START_SERVICE
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AppManagerTest {

    private val watch = Watch("watch-id", "Watch 1", "platform")
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
    private lateinit var messageClient: MessageClient
    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var appManager: AppManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        appManager = AppManager(messageClient, watchManager)
    }

    @Test
    fun `expectPackages updates state and progress`() {
        appManager.expectPackages(1)
        assertThat(appManager.state.getOrAwaitValue())
            .isEquivalentAccordingToCompareTo(State.LOADING_APPS)
        // Progress should be reset to 0
        assertThat(appManager.progress.getOrAwaitValue())
            .isEqualTo(0)
    }

    @Test
    fun `startAppManagerService updates state, clears app list and sends message`() {
        every { watchManager.selectedWatch } returns selectedWatch
        appManager = AppManager(messageClient, watchManager)

        appManager.startAppManagerService()
        assertThat(appManager.state.getOrAwaitValue())
            .isEquivalentAccordingToCompareTo(State.CONNECTING)
        assertThat(appManager.userApps.getOrAwaitValue()).isEmpty()
        assertThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
        verify { watchManager.sendMessage(watch, START_SERVICE, null) }
    }

    @Test
    fun `startAppManagerService handles null watchId`() {
        // watchId should be inherently null by default, since we don't mock selectedWatch
        appManager.startAppManagerService()
        verify(inverse = true) { watchManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `stopAppManagerService handles null watchId`() {
        // watchId should be inherently null by default, since we don't mock selectedWatch
        appManager.stopAppManagerService()
        verify(inverse = true) { watchManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `State is connecting on init`() {
        assertThat(appManager.state.getOrAwaitValue())
            .isEquivalentAccordingToCompareTo(State.CONNECTING)
    }

    @Test
    fun `Message listener is registered on init and removed on destroy`() {
        verify { messageClient.addListener(any()) }

        appManager.destroy()
        verify { messageClient.removeListener(any()) }
    }

    @Test
    fun `selectedWatch is observed for the lifetime of AppManager`() {
        every { watchManager.selectedWatch } returns selectedWatch
        appManager = AppManager(messageClient, watchManager)

        assertThat(selectedWatch.hasActiveObservers()).isTrue()

        appManager.destroy()
        assertThat(selectedWatch.hasObservers()).isFalse()
    }

    @Test
    fun `serviceRunning moves state to READY if we've received all expected packages`() {
        // expectedPackageCount is 0 by default, and _apps.count() should also be 0
        appManager.serviceRunning()
        assertThat(appManager.state.getOrAwaitValue()).isEquivalentAccordingToCompareTo(State.READY)
    }

    @Test
    fun `serviceRunning doesn't change state if we're still waiting for packages`() {
        // _apps.count() should be 0 by default
        appManager.expectPackages(1)
        appManager.serviceRunning()
        assertThat(appManager.state.getOrAwaitValue()).isNotEqualTo(State.READY)
    }

    @Test
    fun `addPackage updates apps once all expected packages are received`() {
        // expectedPackageCount should be 0 by default
        appManager.addPackage(app)
        assertThat(appManager.userApps.getOrAwaitValue()).containsExactly(app)
    }

    @Test
    fun `addPackage updates progress while receiving expected packages`() {
        appManager.expectPackages(10)
        appManager.addPackage(app)
        assertThat(appManager.progress.getOrAwaitValue()).isEqualTo(10)
    }

    @Test
    fun `removePackage correctly removes an app`() {
        appManager.addPackage(app)
        appManager.removePackage(app.packageName)
        assertThat(appManager.userApps.getOrAwaitValue()).isEmpty()
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
        assertThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun `removePackage handles app that hasn't been added`() {
        appManager.removePackage(app.packageName)
        assertThat(appManager.userApps.getOrAwaitValue()).isEmpty()
        assertThat(appManager.systemApps.getOrAwaitValue()).isEmpty()
    }
}
