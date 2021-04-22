package com.boswelja.smartwatchextensions.batterysync.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BatterySyncViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("Name", "id", "platform")
    private val dummyWatchLive = MutableLiveData(dummyWatch)
    private val testDispatcher = TestCoroutineDispatcher()

    @RelaxedMockK private lateinit var watchManager: WatchManager

    private lateinit var viewModel: BatterySyncViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { watchManager.selectedWatch } returns dummyWatchLive
        viewModel = BatterySyncViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager,
            testDispatcher
        )
    }

    @Test
    fun `setPhoneLowNotiEnabled calls WatchManager`() {
        viewModel.setPhoneLowNotiEnabled(true)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_PHONE_LOW_NOTI_KEY, true)
        }
        viewModel.setPhoneLowNotiEnabled(false)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_PHONE_LOW_NOTI_KEY, false)
        }
    }

    @Test
    fun `setWatchLowNotiEnabled calls WatchManager`() {
        viewModel.setWatchLowNotiEnabled(true)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_WATCH_LOW_NOTI_KEY, true)
        }
        viewModel.setWatchLowNotiEnabled(false)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_WATCH_LOW_NOTI_KEY, false)
        }
    }

    @Test
    fun `setLowBatteryThreshold calls WatchManager`() {
        viewModel.setLowBatteryThreshold(5)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_LOW_THRESHOLD_KEY, 5)
        }
        viewModel.setLowBatteryThreshold(15)
        coVerify(exactly = 1) {
            watchManager.updatePreference(dummyWatch, BATTERY_LOW_THRESHOLD_KEY, 15)
        }
    }
}
