package com.boswelja.devicemanager.onboarding.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class RegisterWatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionManager.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionManager.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionManager.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    @RelaxedMockK private lateinit var watchManager: WatchManager
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = RegisterWatchViewModel(
            ApplicationProvider.getApplicationContext(),
            false,
            watchManager
        )
    }

    @Test
    fun `isWorking updates correctly when availableWatches is empty`(): Unit = runBlocking {
        // By the time watchManager.getAvailableWatches() is called, isWorking should be true.
        coEvery { watchManager.availableWatches } answers {
            viewModel.isWorking.getOrAwaitValue { assertThat(it).isTrue() }
            MutableLiveData(emptyList())
        }
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
        viewModel.suspendRegisterAvailableWatches()
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
    }

    @Test
    fun `isWorking updates correctly when availableWatches is not empty`(): Unit = runBlocking {
        // By the time watchManager.getAvailableWatches() is called, isWorking should be true.
        coEvery { watchManager.availableWatches } answers {
            viewModel.isWorking.getOrAwaitValue { assertThat(it).isTrue() }
            MutableLiveData(dummyWatches)
        }
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
        viewModel.suspendRegisterAvailableWatches()
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
    }

    @Test
    fun `registerAvailableWatches registers every watch in availableWatches`(): Unit = runBlocking {
        every { watchManager.availableWatches } returns MutableLiveData(dummyWatches)
        viewModel.suspendRegisterAvailableWatches()
        dummyWatches.forEach {
            coVerify(exactly = 1) { watchManager.registerWatch(it) }
        }
        coVerify(exactly = 1) { watchManager.availableWatches }
    }

    @Test
    fun `registerAvailableWatches does nothing when availableWatches is empty`(): Unit =
        runBlocking {
            coEvery { watchManager.availableWatches } returns MutableLiveData(emptyList())
            viewModel.suspendRegisterAvailableWatches()
            coVerify(exactly = 0) { watchManager.registerWatch(any()) }
            coVerify(exactly = 1) { watchManager.availableWatches }
        }
}
