package com.boswelja.devicemanager.aboutapp.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.connection.Messages
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
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
class AboutAppViewModelTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val selectedWatch = MutableLiveData<Watch?>()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", "")
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", "")

    @RelaxedMockK private lateinit var messageClient: MessageClient
    @RelaxedMockK private lateinit var watchManager: WatchManager
    @RelaxedMockK private lateinit var customTabIntent: CustomTabsIntent

    private lateinit var viewModel: AboutAppViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        selectedWatch.postValue(dummyWatch1)
        every { watchManager.selectedWatch } returns selectedWatch

        viewModel = AboutAppViewModel(
            ApplicationProvider.getApplicationContext(),
            messageClient,
            watchManager,
            customTabIntent
        )
    }

    @Test
    fun `Watch version update requested on init`() {
        verify { messageClient.sendMessage(dummyWatch1.id, Messages.REQUEST_APP_VERSION, null) }
    }

    @Test
    fun `Switching selected watch requests new watch version`() {
        selectedWatch.postValue(dummyWatch1)
        verify { messageClient.sendMessage(dummyWatch1.id, Messages.REQUEST_APP_VERSION, null) }
        selectedWatch.postValue(dummyWatch2)
        verify { messageClient.sendMessage(dummyWatch2.id, Messages.REQUEST_APP_VERSION, null) }
    }
}
