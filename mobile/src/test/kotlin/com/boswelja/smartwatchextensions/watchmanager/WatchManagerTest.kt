package com.boswelja.smartwatchextensions.watchmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.liveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.WatchConnectionClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
@ExperimentalCoroutinesApi
class WatchManagerTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val platformIdentifier = "dummy"

    private val dummyWatch1 = Watch("Watch 1", "id1", platformIdentifier)
    private val dummyWatch2 = Watch("Watch 2", "id2", platformIdentifier)
    private val dummyWatch3 = Watch("Watch 3", "id3", platformIdentifier)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)
    private val appState = MutableStateFlow(AppState())

    @RelaxedMockK private lateinit var watchDatabase: WatchDatabase
    @RelaxedMockK private lateinit var connectionClient: WatchConnectionClient
    @RelaxedMockK private lateinit var widgetIdStore: DataStore<Preferences>
    @RelaxedMockK private lateinit var analytics: Analytics
    @RelaxedMockK private lateinit var batteryStatsDatabase: WatchBatteryStatsDatabase
    @RelaxedMockK private lateinit var dataStore: DataStore<AppState>
    @RelaxedMockK private lateinit var settingsDatabase: WatchSettingsDatabase

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { dataStore.data } returns appState

        coroutineScope = TestCoroutineScope()
    }

    @Test
    fun `selectedWatch is updated correctly on init`(): Unit = runBlocking {
        every { watchDatabase.watchDao().getAllObservable() } returns liveData {
            emit(dummyWatches.map { it.toDbWatch() })
        }
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        watchManager = getWatchManager()

        assertThat(watchManager.selectedWatch.getOrAwaitValue()).isEqualTo(dummyWatch1)
    }

    @Test
    fun `selectWatchById updates selectedWatch`(): Unit = runBlocking {
        // Initialise WatchManager
        every { watchManager.registeredWatches } returns liveData { emit(dummyWatches) }
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        watchManager = getWatchManager()
        watchManager.selectWatchById(dummyWatch2.id)

        // Check the selected watch has been updated
        assertThat(watchManager.selectedWatch.getOrAwaitValue()).isEqualTo(dummyWatch2)
    }

    @Test
    fun `requestRefreshCapabilities calls connection client`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.getCapabilitiesFor(dummyWatch1)
        coVerify(exactly = 1) { connectionClient.getCapabilitiesFor(dummyWatch1) }
    }

    @Test
    fun `registerWatch calls connection client and logs analytics event`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.registerWatch(dummyWatch1)
        verify(exactly = 1) { analytics.logWatchRegistered() }
        coVerify(exactly = 1) { connectionClient.sendMessage(dummyWatch1, Messages.WATCH_REGISTERED_PATH) }
        coVerify(exactly = 1) { watchDatabase.watchDao().add(dummyWatch1.toDbWatch()) }
    }

    @Test
    fun `forgetWatch calls connection client, databases and logs analytics event`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.forgetWatch(
            widgetIdStore,
            batteryStatsDatabase,
            dummyWatch1
        )
        verify(exactly = 1) { analytics.logWatchRemoved() }
        coVerify(exactly = 1) { watchDatabase.watchDao().remove(dummyWatch1.id) }
        coVerify(exactly = 1) { connectionClient.sendMessage(dummyWatch1, Messages.RESET_APP) }
        verify(exactly = 1) { batteryStatsDatabase.batteryStatsDao() }
    }

    @Test
    fun `renameWatch calls repository and logs analytics event`(): Unit = runBlocking {
        val newName = "dummy name"
        watchManager = getWatchManager()
        watchManager.renameWatch(dummyWatch1, newName)
        verify(exactly = 1) { analytics.logWatchRenamed() }
        coVerify(exactly = 1) { watchDatabase.watchDao().setName(dummyWatch1.id, newName) }
    }

    @Test
    fun `resetWatchPreferences calls connection client and databases`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.resetWatchPreferences(
            widgetIdStore,
            batteryStatsDatabase,
            dummyWatch1
        )
        coVerify(exactly = 1) { connectionClient.sendMessage(dummyWatch1, CLEAR_PREFERENCES) }
        coVerify(exactly = 1) { settingsDatabase.clearWatchPreferences(dummyWatch1) }
        verify(exactly = 1) { batteryStatsDatabase.batteryStatsDao() }
    }

    @Test
    fun `selectedWatch is null if there are no registered watches`() {
        every { watchDatabase.watchDao().getAllObservable() } returns liveData {
            emit(dummyWatches.map { it.toDbWatch() })
        }
        watchManager = getWatchManager()
        try {
            // We expect this to throw an exception, so use a shorter timeout
            assertThat(
                watchManager.selectedWatch.getOrAwaitValue(
                    time = 500, timeUnit = TimeUnit.MILLISECONDS
                )
            ).isNull()
        } catch (e: Exception) {
            assertThat(watchManager.selectedWatch.value).isNull()
        }
    }

    @Test
    fun `selectedWatch is not null if there are registered watches and last watch ID is known`():
        Unit = runBlocking {
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        every { watchDatabase.watchDao().getAllObservable() } returns liveData {
            emit(dummyWatches.map { it.toDbWatch() })
        }
        watchManager = getWatchManager()
        try {
            assertThat(
                watchManager.selectedWatch.getOrAwaitValue()
            ).isNotNull()
        } catch (e: Exception) {
            assertThat(watchManager.selectedWatch.value).isNotNull()
        }
    }

    @Test
    fun `selectedWatch is not null if there are registered watches and no last watch ID is known`():
        Unit = runBlocking {
        appState.emit(AppState(lastSelectedWatchId = ""))
        every { watchDatabase.watchDao().getAllObservable() } returns liveData {
            emit(dummyWatches.map { it.toDbWatch() })
        }
        watchManager = getWatchManager()
        try {
            assertThat(
                watchManager.selectedWatch.getOrAwaitValue()
            ).isNotNull()
        } catch (e: Exception) {
            assertThat(watchManager.selectedWatch.value).isNotNull()
        }
    }

    private fun getWatchManager(): WatchManager {
        val watchManager = WatchManager(
            settingsDatabase,
            watchDatabase,
            connectionClient,
            analytics,
            dataStore,
            coroutineScope
        )
        clearAllMocks(
            answers = false,
            recordedCalls = true,
            childMocks = false,
            regularMocks = true,
            objectMocks = false,
            staticMocks = false,
            constructorMocks = false
        )
        return watchManager
    }
}
