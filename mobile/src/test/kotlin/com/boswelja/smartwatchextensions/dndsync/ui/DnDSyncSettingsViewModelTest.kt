package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.NotificationManager
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class DnDSyncSettingsViewModelTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch = Watch("Name", "id", "platform")
    private val dummyCapabilities = MutableStateFlow<List<Capability>>(emptyList())
    private val dummyWatchLive = MutableStateFlow(dummyWatch)

    @RelaxedMockK
    private lateinit var watchManager: WatchManager
    @RelaxedMockK
    private lateinit var notificationManager: NotificationManager

    private lateinit var viewModel: DnDSyncSettingsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        runBlocking { dummyCapabilities.emit(emptyList()) }

        every { watchManager.selectedWatchCapabilities() } returns dummyCapabilities
        every { watchManager.selectedWatch } returns dummyWatchLive

        viewModel = DnDSyncSettingsViewModel(
            ApplicationProvider.getApplicationContext(),
            watchManager,
            notificationManager
        )
    }

    @Test
    fun `canSendDnD flows true when capability is present`(): Unit = runBlocking {
        // Emulate capability presence
        dummyCapabilities.emit(listOf(Capability.SEND_DND))

        // Check canSyncBattery
        viewModel.canSendDnD.take(1).collect {
            expectThat(it).isTrue()
        }
    }

    @Test
    fun `canSendDnD flows false when capability is missing`(): Unit = runBlocking {
        // Emulate capability presence
        dummyCapabilities.emit(emptyList())

        // Check canSyncBattery
        viewModel.canSendDnD.take(1).collect {
            expectThat(it).isFalse()
        }
    }

    @Test
    fun `canReceiveDnD flows true when capability is present`(): Unit = runBlocking {
        // Emulate capability presence
        dummyCapabilities.emit(listOf(Capability.RECEIVE_DND))

        // Check canSyncBattery
        viewModel.canReceiveDnD.take(1).collect {
            expectThat(it).isTrue()
        }
    }

    @Test
    fun `canReceiveDnD flows false when capability is missing`(): Unit = runBlocking {
        // Emulate capability presence
        dummyCapabilities.emit(emptyList())

        // Check canSyncBattery
        viewModel.canReceiveDnD.take(1).collect {
            expectThat(it).isFalse()
        }
    }
}
