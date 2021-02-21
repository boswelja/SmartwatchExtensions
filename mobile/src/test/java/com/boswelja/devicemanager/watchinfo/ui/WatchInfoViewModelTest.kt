package com.boswelja.devicemanager.watchinfo.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlin.experimental.or
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
class WatchInfoViewModelTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("watch-id", "Watch Name", "platform")
    private val registeredWatches = MutableLiveData(listOf(dummyWatch))
    private val dispatcher = TestCoroutineDispatcher()

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var viewModel: WatchInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { watchManager.registeredWatches } returns registeredWatches

        viewModel = WatchInfoViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager,
            dispatcher
        )
    }

    @Test
    fun `setWatch correctly picks a watch from registeredWatches`() {
        viewModel.setWatch(dummyWatch.id)
        assertThat(viewModel.watch.getOrAwaitValue()).isEqualTo(dummyWatch)
    }

    @Test
    fun `capability changes update selected watch`() {
        viewModel.setWatch(dummyWatch.id)

        // Swap watch out
        val dummyWatch2 = dummyWatch
        dummyWatch2.capabilities = Capability.SYNC_BATTERY.id or Capability.SEND_DND.id
        registeredWatches.value = listOf(dummyWatch2)

        assertThat(viewModel.watch.getOrAwaitValue().capabilities)
            .isEqualTo(dummyWatch2.capabilities)
    }

    @Test
    fun `updateWatchName calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.updateWatchName("new name")
        coVerify(inverse = true) { watchManager.renameWatch(any(), any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.updateWatchName("new name")
        coVerify { watchManager.renameWatch(dummyWatch, any()) }
    }

    @Test
    fun `refreshCapabilities calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.refreshCapabilities()
        coVerify(inverse = true) { watchManager.requestRefreshCapabilities(any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.refreshCapabilities()
        coVerify { watchManager.requestRefreshCapabilities(dummyWatch) }
    }

    @Test
    fun `forgetWatch calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.forgetWatch()
        coVerify(inverse = true) { watchManager.forgetWatch(any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.forgetWatch()
        coVerify { watchManager.forgetWatch(dummyWatch) }
    }

    @Test
    fun `resetWatchPreferences calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.resetWatchPreferences()
        coVerify(inverse = true) { watchManager.resetWatchPreferences(any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.resetWatchPreferences()
        coVerify { watchManager.resetWatchPreferences(dummyWatch) }
    }
}
