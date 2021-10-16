package com.boswelja.smartwatchextensions.batterysync.ui

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.watchmanager.Capability
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BatterySyncViewModelTest {

    private val dummyWatch = Watch("Name", "id", "platform")
    private val hasCapability = MutableStateFlow(false)
    private val dummyWatchLive = MutableStateFlow(dummyWatch)
    private val testDispatcher = TestCoroutineDispatcher()

    @RelaxedMockK private lateinit var watchManager: WatchManager

    private lateinit var viewModel: BatterySyncViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        runBlocking { hasCapability.emit(false) }

        every {
            watchManager.selectedWatchHasCapability(Capability.SYNC_BATTERY)
        } returns hasCapability
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

    @Test
    fun `canSyncBattery flows true when capability is present`(): Unit = runBlocking {
        // Emulate capability presence
        hasCapability.emit(true)

        // Check canSyncBattery
        viewModel.canSyncBattery.take(1).collect {
            expectThat(it).isTrue()
        }
    }

    @Test
    fun `canSyncBattery flows false when capability is missing`(): Unit = runBlocking {
        // Emulate capability missing
        hasCapability.emit(false)

        // Check canSyncBattery
        viewModel.canSyncBattery.take(1).collect {
            expectThat(it).isFalse()
        }
    }
}
