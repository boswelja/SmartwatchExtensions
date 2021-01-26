package com.boswelja.devicemanager.onboarding.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1")
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2")
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3")
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
    fun `isWorking updates correctly when availableWatches is null`(): Unit = runBlocking {
        // By the time watchManager.getAvailableWatches() is called, isWorking should be true.
        coEvery { watchManager.getAvailableWatches() } answers {
            viewModel.isWorking.getOrAwaitValue { assertThat(it).isTrue() }
            null
        }
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
        viewModel.suspendRegisterAvailableWatches()
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
    }

    @Test
    fun `isWorking updates correctly when availableWatches is empty`(): Unit = runBlocking {
        // By the time watchManager.getAvailableWatches() is called, isWorking should be true.
        coEvery { watchManager.getAvailableWatches() } answers {
            viewModel.isWorking.getOrAwaitValue { assertThat(it).isTrue() }
            emptyList()
        }
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
        viewModel.suspendRegisterAvailableWatches()
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
    }

    @Test
    fun `isWorking updates correctly when availableWatches is not empty`(): Unit = runBlocking {
        // By the time watchManager.getAvailableWatches() is called, isWorking should be true.
        coEvery { watchManager.getAvailableWatches() } answers {
            viewModel.isWorking.getOrAwaitValue { assertThat(it).isTrue() }
            dummyWatches
        }
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
        viewModel.suspendRegisterAvailableWatches()
        viewModel.isWorking.getOrAwaitValue { assertThat(it).isFalse() }
    }

    @Test
    fun `registerAvailableWatches registers every watch in availableWatches`(): Unit = runBlocking {
        coEvery { watchManager.getAvailableWatches() } returns dummyWatches
        viewModel.suspendRegisterAvailableWatches()
        dummyWatches.forEach {
            coVerify(exactly = 1) { watchManager.registerWatch(it) }
        }
        coVerify(exactly = 1) { watchManager.getAvailableWatches() }
    }

    @Test
    fun `registerAvailableWatches does nothing when availableWatches is null`(): Unit =
        runBlocking {
            coEvery { watchManager.getAvailableWatches() } returns null
            viewModel.suspendRegisterAvailableWatches()
            coVerify(exactly = 0) { watchManager.registerWatch(any()) }
            coVerify(exactly = 1) { watchManager.getAvailableWatches() }
        }

    @Test
    fun `registerAvailableWatches does nothing when availableWatches is empty`(): Unit =
        runBlocking {
            coEvery { watchManager.getAvailableWatches() } returns emptyList()
            viewModel.suspendRegisterAvailableWatches()
            coVerify(exactly = 0) { watchManager.registerWatch(any()) }
            coVerify(exactly = 1) { watchManager.getAvailableWatches() }
        }
}
