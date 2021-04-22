package com.boswelja.smartwatchextensions.aboutapp.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
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

    private val selectedWatch = MutableLiveData<Watch>()

    private val dummyWatch1 = Watch("Watch 1", "id1", "")
    private val dummyWatch2 = Watch("Watch 2", "id2", "")

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
            watchManager,
            customTabIntent
        )
    }

    @Test
    fun `Watch version update requested on init`() {
        coVerify { watchManager.sendMessage(dummyWatch1, Messages.REQUEST_APP_VERSION, null) }
    }

    @Test
    fun `Switching selected watch requests new watch version`() {
        selectedWatch.postValue(dummyWatch1)
        coVerify { watchManager.sendMessage(dummyWatch1, Messages.REQUEST_APP_VERSION, null) }
        selectedWatch.postValue(dummyWatch2)
        coVerify { watchManager.sendMessage(dummyWatch1, Messages.REQUEST_APP_VERSION, null) }
    }
}
