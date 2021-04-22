package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchViewModel
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.wearos.WearOSConnectionHandler
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
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
class RegisterWatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val dummyWatch1 = Watch("Watch 1", "id1", WearOSConnectionHandler.PLATFORM)
    private val dummyWatch2 = Watch("Watch 2", "id2", WearOSConnectionHandler.PLATFORM)
    private val dummyWatch3 = Watch("Watch 3", "id3", WearOSConnectionHandler.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)
    private val availableWatches = MutableStateFlow(dummyWatch1)
    private val registeredWatches = MutableLiveData(emptyList<Watch>())

    @RelaxedMockK private lateinit var watchManager: WatchManager
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp(): Unit = runBlocking {
        MockKAnnotations.init(this)

        availableWatches.resetReplayCache()
        dummyWatches.forEach { availableWatches.emit(it) }
        registeredWatches.postValue(emptyList())

        every { watchManager.availableWatches } returns availableWatches
        every { watchManager.registeredWatches } returns registeredWatches

        viewModel = RegisterWatchViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager,
            testDispatcher
        )
    }

    @Test
    fun `addWatch calls watchManager and updates LiveData`(): Unit = runBlocking {
        availableWatches.resetReplayCache()
        availableWatches.emit(dummyWatch1)
        registeredWatches.postValue(emptyList())

        viewModel.addWatch(dummyWatch1)
        coVerify { watchManager.registerWatch(dummyWatch1) }
        assertThat(viewModel.registeredWatches.toList()).contains(dummyWatch1)
    }

    @Test
    fun `addWatch does nothing if a watch is already registered`() {
        // We need to register a watch first
        viewModel.addWatch(dummyWatch1)
        viewModel.addWatch(dummyWatch1)
        // Verify watch was only added once
        coVerify(exactly = 1) { watchManager.registerWatch(dummyWatch1) }
    }

    @Test
    fun `watchesToRegister contains all available watches`(): Unit = runBlocking {
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsExactlyElementsIn(dummyWatches)

        availableWatches.resetReplayCache()
        availableWatches.emit(dummyWatch1)
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsExactlyElementsIn(listOf(dummyWatch1))

        availableWatches.resetReplayCache()
        availableWatches.emit(dummyWatch1)
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsExactlyElementsIn(listOf(dummyWatch1, dummyWatch3))
    }

    @Test
    fun `watchesToRegister doesn't contain any registered watches`() {
        registeredWatches.postValue(dummyWatches)
        assertThat(viewModel.watchesToAdd.getOrAwaitValue()).isEmpty()

        registeredWatches.postValue(listOf(dummyWatch1))
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsNoneIn(listOf(dummyWatch1))

        registeredWatches.postValue(listOf(dummyWatch1, dummyWatch2))
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsNoneIn(listOf(dummyWatch1, dummyWatch2))
    }
}
