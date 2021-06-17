package com.boswelja.smartwatchextensions.watchmanager

import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

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

    @RelaxedMockK private lateinit var messageClient: MessageClient
    @RelaxedMockK private lateinit var discoveryClient: DiscoveryClient
    @RelaxedMockK private lateinit var widgetIdStore: DataStore<Preferences>
    @RelaxedMockK private lateinit var analytics: Analytics
    // TODO Don't mock databases, instead create in memory databases so we can validate data too
    @RelaxedMockK private lateinit var batteryStatsDatabase: WatchBatteryStatsDatabase
    @RelaxedMockK private lateinit var watchAppDatabase: WatchAppDatabase
    @RelaxedMockK private lateinit var dataStore: DataStore<AppState>
    @RelaxedMockK private lateinit var settingsDatabase: WatchSettingsDatabase

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var watchManager: WatchManager
    private lateinit var watchDatabase: WatchDatabase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { dataStore.data } returns appState

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(), config
        )

        watchDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WatchDatabase::class.java
        ).allowMainThreadQueries().build()
        coroutineScope = TestCoroutineScope()
    }

    @Test
    fun `selectedWatch is updated correctly on init`(): Unit = runBlocking {
        setRegisteredWatches(dummyWatches)
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        watchManager = getWatchManager()

        expectThat(watchManager.selectedWatch.first()).isEqualTo(dummyWatch1)
    }

    @Test
    fun `selectWatchById updates selectedWatch`(): Unit = runBlocking {
        // Initialise WatchManager
        setRegisteredWatches(dummyWatches)
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        watchManager = getWatchManager()
        watchManager.selectWatchById(dummyWatch2.id)

        // Check the selected watch has been updated
        expectThat(watchManager.selectedWatch.first()).isEqualTo(dummyWatch2)
    }

    @Test
    fun `requestRefreshCapabilities calls connection client`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.getCapabilitiesFor(dummyWatch1)
        coVerify(exactly = 1) { discoveryClient.getCapabilitiesFor(dummyWatch1) }
    }

    @Test
    fun `registerWatch calls connection client and logs analytics event`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.registerWatch(dummyWatch1)
        verify(exactly = 1) { analytics.logWatchRegistered() }
        coVerify(exactly = 1) {
            messageClient.sendMessage(dummyWatch1, Messages.WATCH_REGISTERED_PATH)
        }

        // Verify watch was added to the database
        expectThat(watchDatabase.watchDao().get(dummyWatch1.id).firstOrNull()).isNotNull()
    }

    @Test
    fun `forgetWatch calls connection client, databases and logs analytics event`(): Unit =
        runBlocking {
            watchManager = getWatchManager()
            watchManager.forgetWatch(
                widgetIdStore,
                batteryStatsDatabase,
                watchAppDatabase,
                dummyWatch1
            )
            verify(exactly = 1) { analytics.logWatchRemoved() }
            coVerify(exactly = 1) { messageClient.sendMessage(dummyWatch1, Messages.RESET_APP) }
            verify(exactly = 1) { batteryStatsDatabase.batteryStatsDao() }
            verify(exactly = 1) { watchAppDatabase.apps() }

            // Verify watch isn't in database
            expectThat(watchDatabase.watchDao().get(dummyWatch1.id).firstOrNull()).isNull()
        }

    @Test
    fun `renameWatch updates database and logs analytics event`(): Unit = runBlocking {
        val newName = "dummy name"
        setRegisteredWatches(dummyWatches)
        watchManager = getWatchManager()
        watchManager.renameWatch(dummyWatch1, newName)
        verify(exactly = 1) { analytics.logWatchRenamed() }

        // Verify name was changed in the database
        expectThat(watchDatabase.watchDao().get(dummyWatch1.id).firstOrNull()?.name)
            .isEqualTo(newName)
    }

    @Test
    fun `resetWatchPreferences calls connection client and databases`(): Unit = runBlocking {
        watchManager = getWatchManager()
        watchManager.resetWatchPreferences(
            widgetIdStore,
            batteryStatsDatabase,
            dummyWatch1
        )
        coVerify(exactly = 1) { messageClient.sendMessage(dummyWatch1, CLEAR_PREFERENCES) }
        verify(exactly = 1) { settingsDatabase.intSettings() }
        verify(exactly = 1) { settingsDatabase.boolSettings() }
        verify(exactly = 1) { batteryStatsDatabase.batteryStatsDao() }
    }

    @Test
    fun `selectedWatch is null if there are no registered watches`(): Unit = runBlocking {
        setRegisteredWatches(emptyList())
        watchManager = getWatchManager()
        expectThat(watchManager.selectedWatch.firstOrNull()).isNull()
    }

    @Test
    fun `selectedWatch is not null if there are registered watches and last watch ID is known`():
        Unit = runBlocking {
        appState.emit(AppState(lastSelectedWatchId = dummyWatch1.id.toString()))
        setRegisteredWatches(dummyWatches)
        watchManager = getWatchManager()
        expectThat(watchManager.selectedWatch.firstOrNull()).isNotNull()
    }

    @Test
    fun `selectedWatch is not null if there are registered watches and no last watch ID is known`():
        Unit = runBlocking {
        appState.emit(AppState(lastSelectedWatchId = ""))
        setRegisteredWatches(dummyWatches)
        watchManager = getWatchManager()
        expectThat(watchManager.selectedWatch.firstOrNull()).isNotNull()
    }

    private fun setRegisteredWatches(watches: List<Watch>): Unit = runBlocking {
        watches.forEach { watchDatabase.watchDao().add(it.toDbWatch()) }
    }

    private fun getWatchManager(): WatchManager {
        val watchManager = WatchManager(
            ApplicationProvider.getApplicationContext(),
            settingsDatabase,
            watchDatabase,
            messageClient,
            discoveryClient,
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
