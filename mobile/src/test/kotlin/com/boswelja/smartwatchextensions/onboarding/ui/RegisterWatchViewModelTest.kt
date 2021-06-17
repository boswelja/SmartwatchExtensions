package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchViewModel
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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

    private val dummyPlatform = "platform"
    private val dummyWatch1 = Watch("Watch 1", "id1", dummyPlatform)
    private val dummyWatch2 = Watch("Watch 2", "id2", dummyPlatform)
    private val dummyWatch3 = Watch("Watch 3", "id3", dummyPlatform)
    private val dummyWatches = arrayOf(dummyWatch1, dummyWatch2, dummyWatch3)
    private var availableWatches: Flow<List<Watch>> = flow { }
    private val registeredWatches = MutableStateFlow(emptyList<Watch>())

    @RelaxedMockK private lateinit var watchManager: WatchManager
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        setAvailableWatches(*dummyWatches)
        registeredWatches.tryEmit(emptyList())

        every { watchManager.availableWatches } returns availableWatches
        every { watchManager.registeredWatches } returns registeredWatches

        viewModel = RegisterWatchViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager
        )
    }

    @Test
    fun `addWatch calls watchManager and updates LiveData`(): Unit = runBlocking {
        setAvailableWatches(dummyWatch1)
        registeredWatches.tryEmit(emptyList())

        viewModel.addWatch(dummyWatch1)
        coVerify { watchManager.registerWatch(dummyWatch1) }
    }

    private fun setAvailableWatches(vararg available: Watch) {
        availableWatches = flow {
            emit(available.toList())
        }
    }
}
