package com.boswelja.devicemanager.setup.ui

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
@Config(sdk = [Build.VERSION_CODES.Q])
class RegisterWatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("an-id-1234", "Watch 1")

    @RelaxedMockK private lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `refreshAvailableWatches is called on ViewModel init`(): Unit = runBlocking {
        coEvery { watchManager.getAvailableWatches() } returns null
        val viewModel = getViewModel()
        coVerify(exactly = 1) { watchManager.getAvailableWatches() }
        viewModel.availableWatches.getOrAwaitValue {
            assertThat(it).isNull()
        }
        viewModel.isLoading.getOrAwaitValue {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `refreshAvailableWatches updates the corresponding LiveData`(): Unit = runBlocking {
        coEvery { watchManager.getAvailableWatches() } returns listOf(dummyWatch)
        val viewModel = getViewModel()
        coVerify(exactly = 1) { watchManager.getAvailableWatches() }
        viewModel.availableWatches.getOrAwaitValue {
            assertThat(it).containsExactly(dummyWatch)
        }
        viewModel.isLoading.getOrAwaitValue {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `registerWatch fires the corresponding event`() {
        coEvery { watchManager.getAvailableWatches() } returns listOf(dummyWatch)
        val viewModel = getViewModel()
        viewModel.registerWatch(dummyWatch)
        coVerify(exactly = 1) { watchManager.registerWatch(dummyWatch) }
        viewModel.newWatchRegistered.getOrAwaitValue {
            assertThat(it).isTrue()
        }
    }

    private fun getViewModel(): RegisterWatchViewModel =
        RegisterWatchViewModel(ApplicationProvider.getApplicationContext(), watchManager)
}
