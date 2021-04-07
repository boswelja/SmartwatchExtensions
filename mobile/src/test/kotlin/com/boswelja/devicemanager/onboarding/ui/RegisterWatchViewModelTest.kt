package com.boswelja.devicemanager.onboarding.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
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
class RegisterWatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionInterface.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)
    private val availableWatches = MutableLiveData(dummyWatches)
    private val registeredWatches = MutableLiveData(emptyList<Watch>())

    @RelaxedMockK private lateinit var watchManager: WatchManager
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        availableWatches.postValue(dummyWatches)
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
    fun `refreshData calls watchManager`() {
        viewModel.refreshData()
        verify { watchManager.refreshData() }
    }

    @Test
    fun `addWatch calls watchManager and updates LiveData`() {
        availableWatches.postValue(listOf(dummyWatch1))
        registeredWatches.postValue(emptyList())

        viewModel.addWatch(dummyWatch1)
        coVerify { watchManager.registerWatch(dummyWatch1) }
        assertThat(viewModel.registeredWatches.getOrAwaitValue()).contains(dummyWatch1)
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
    fun `watchesToRegister contains all available watches`() {
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsExactlyElementsIn(dummyWatches)

        availableWatches.postValue(listOf(dummyWatch1))
        assertThat(viewModel.watchesToAdd.getOrAwaitValue())
            .containsExactlyElementsIn(listOf(dummyWatch1))

        availableWatches.postValue(listOf(dummyWatch1, dummyWatch3))
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
