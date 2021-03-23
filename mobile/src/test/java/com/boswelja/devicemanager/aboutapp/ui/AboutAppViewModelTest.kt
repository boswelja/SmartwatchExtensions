package com.boswelja.devicemanager.aboutapp.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.liveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
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
class AboutAppViewModelTest {

    private val watchId = "123456"

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var messageClient: MessageClient

    @RelaxedMockK
    private lateinit var customTabs: CustomTabsIntent

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var viewModelAbout: AboutAppViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModelAbout = AboutAppViewModel(
            ApplicationProvider.getApplicationContext(),
            messageClient,
            watchManager,
            customTabs
        )
        verify { messageClient.addListener(any()) }
    }

    @Test
    fun `Requesting watch version with empty watch ID fails`() {
        every { watchManager.selectedWatch } returns liveData { Watch("", "", "") }
        viewModelAbout.requestUpdateWatchVersion()
        viewModelAbout.watchAppVersion.getOrAwaitValue { assertThat(it).isNull() }
    }

    @Test
    fun `Requesting watch version sends a request`() {
        every { watchManager.selectedWatch } returns liveData { Watch(watchId, "", "") }
        viewModelAbout.requestUpdateWatchVersion()
        verify(exactly = 1) { messageClient.sendMessage(watchId, REQUEST_APP_VERSION, null) }
        confirmVerified(messageClient)
    }
}
