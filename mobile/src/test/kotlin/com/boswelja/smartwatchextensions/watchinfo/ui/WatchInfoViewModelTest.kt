package com.boswelja.smartwatchextensions.watchinfo.ui

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WatchInfoViewModelTest {

    private val dummyWatch = Watch("Watch Name", "watch-id", "platform")
    private val registeredWatches = MutableStateFlow(listOf(dummyWatch))

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var viewModel: WatchInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { watchManager.registeredWatches } returns registeredWatches
        every { watchManager.getWatchById(dummyWatch.id) } returns MutableStateFlow(dummyWatch)

        viewModel = WatchInfoViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager
        )
    }

    @Test
    fun `updateWatchName calls watchManager if watch is not null`(): Unit = runBlocking {
        // Check with no / invalid watch selected
        viewModel.updateWatchName("new name")
        coVerify(inverse = true) { watchManager.renameWatch(any(), any()) }

        // Check with valid watch selected
        viewModel.watchId.emit(dummyWatch.id)
        viewModel.updateWatchName("new name")
        coVerify { watchManager.renameWatch(dummyWatch, any()) }
    }

    @Test
    fun `forgetWatch calls watchManager if watch is not null`(): Unit = runBlocking {
        // Check with no / invalid watch selected
        viewModel.forgetWatch()
        coVerify(inverse = true) { watchManager.forgetWatch(any(), any()) }

        // Check with valid watch selected
        viewModel.watchId.emit(dummyWatch.id)
        viewModel.forgetWatch()
        coVerify { watchManager.forgetWatch(any(), dummyWatch) }
    }

    @Test
    fun `resetWatchPreferences calls watchManager if watch is not null`(): Unit = runBlocking {
        // Check with no / invalid watch selected
        viewModel.resetWatchPreferences()
        coVerify(inverse = true) { watchManager.resetWatchPreferences(any(), any()) }

        // Check with valid watch selected
        viewModel.watchId.emit(dummyWatch.id)
        viewModel.resetWatchPreferences()
        coVerify { watchManager.resetWatchPreferences(any(), dummyWatch) }
    }
}
