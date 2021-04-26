package com.boswelja.smartwatchextensions.watchinfo.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
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

    private val dummyWatch = Watch("Watch Name", "watch-id", "platform")
    private val registeredWatches = MutableLiveData(listOf(dummyWatch))
    private val dispatcher = TestCoroutineDispatcher()

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var viewModel: WatchInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { watchManager.registeredWatches } returns registeredWatches
        every { watchManager.observeWatchById(dummyWatch.id) } returns MutableLiveData(dummyWatch)

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
    fun `getCapabilities calls watchManager if watch is not null`(): Unit = runBlocking {
        // Check with no / invalid watch selected
        viewModel.getCapabilities()?.collect()
        coVerify(inverse = true) { watchManager.getCapabilitiesFor(any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.getCapabilities()?.collect()
        coVerify { watchManager.getCapabilitiesFor(dummyWatch) }
    }

    @Test
    fun `forgetWatch calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.forgetWatch()
        coVerify(inverse = true) { watchManager.forgetWatch(any(), any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.forgetWatch()
        coVerify { watchManager.forgetWatch(any(), dummyWatch) }
    }

    @Test
    fun `resetWatchPreferences calls watchManager if watch is not null`() {
        // Check with no / invalid watch selected
        viewModel.resetWatchPreferences()
        coVerify(inverse = true) { watchManager.resetWatchPreferences(any(), any()) }

        // Check with valid watch selected
        viewModel.setWatch(dummyWatch.id)
        viewModel.watch.getOrAwaitValue()
        viewModel.resetWatchPreferences()
        coVerify { watchManager.resetWatchPreferences(any(), dummyWatch) }
    }
}