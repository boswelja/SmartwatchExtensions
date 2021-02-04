package com.boswelja.devicemanager.watchmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.connection.DummyConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WatchRepositoryTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", DummyConnectionInterface.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", DummyConnectionInterface.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", DummyConnectionInterface.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private lateinit var repository: WatchRepository
    private lateinit var database: WatchDatabase
    private lateinit var connectionInterface: DummyConnectionInterface

    @Before
    fun setUp() {
        connectionInterface = spyk(DummyConnectionInterface())
        database = spyk(
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WatchDatabase::class.java
            ).build()
        )
        repository = WatchRepository(database, connectionInterface)
        verifyAll {
            connectionInterface.availableWatches
            connectionInterface.dataChanged
            connectionInterface.platformIdentifier
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replaceForPlatform returns only newWatches if empty existingWatches`() {
        val result = repository.replaceForPlatform(emptyList(), dummyWatches)
        assertThat(result).containsExactlyElementsIn(dummyWatches)
    }

    @Test
    fun `replaceForPlatform returns existingWatches if newWatches is empty`() {
        var result = repository.replaceForPlatform(emptyList(), emptyList())
        assertThat(result).isEmpty()

        result = repository.replaceForPlatform(dummyWatches, emptyList())
        assertThat(result).containsExactlyElementsIn(dummyWatches)
    }

    @Test
    fun `replaceForPlatform correctly removes all watches for a platform and replaces them`() {
        val newWatches = dummyWatches.drop(1)
        val result = repository.replaceForPlatform(dummyWatches, newWatches)
        assertThat(result).containsExactlyElementsIn(newWatches)
    }

    @Test
    fun `updateStatusForPlatform calls getStatus on the correct watches`() {
        // Change the returning status
        every { connectionInterface.getWatchStatus(any(), any()) } returns Watch.Status.CONNECTED

        val result =
            repository.updateStatusForPlatform(dummyWatches, DummyConnectionInterface.PLATFORM)
        dummyWatches.forEach {
            verify(exactly = 1) { connectionInterface.getWatchStatus(it, any()) }
        }
        // Ensure no extra calls to getWatchStatus were made
        confirmVerified(connectionInterface)
        result.filter { it.platform == DummyConnectionInterface.PLATFORM }.forEach {
            assertThat(it.status).isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)
        }
    }

    @Test
    fun `refreshData calls correct function on all provided WatchConnectionInterface`() {
        repository.refreshData()
        verify(exactly = 1) { connectionInterface.refreshData() }
    }

    @Test
    fun `updatePreference does nothing with invalid keys`(): Unit = runBlocking {
        val key = "dummy-key"
        repository.updatePreference(dummyWatch1, key, 0)
        verify(exactly = 0) { database.updatePrefInDatabase(any(), any(), any()) }
        verify(exactly = 0) { connectionInterface.updatePreferenceOnWatch(any(), any(), any()) }
    }

    @Test
    fun `updatePreference correctly updates preference if contained in SyncPreferences`(): Unit =
        runBlocking {
            val key = SyncPreferences.INT_PREFS.first()
            repository.updatePreference(dummyWatch1, key, 0)
            verify(exactly = 1) { database.updatePrefInDatabase(dummyWatch1.id, key, 0) }
            verify(exactly = 1) { connectionInterface.updatePreferenceOnWatch(dummyWatch1, key, 0) }
        }

    @Test
    fun `forgetWatch removes the watch from the database and calls resetWatch`(): Unit =
        runBlocking {
            repository.forgetWatch(dummyWatch1)
            verify(exactly = 1) { database.forgetWatch(dummyWatch1) }
            verify(exactly = 1) { connectionInterface.resetWatchApp(dummyWatch1) }
        }

    @Test
    fun `renameWatch renames the watch in the database`(): Unit = runBlocking {
        val newName = "new name"
        repository.renameWatch(dummyWatch1, newName)
        verify(exactly = 1) { database.renameWatch(dummyWatch1, newName) }
    }

    @Test
    fun `resetWatch calls the correct connectionInterface`() {
        repository.resetWatch(dummyWatch1)
        verify(exactly = 1) { connectionInterface.resetWatchApp(dummyWatch1) }
    }

    @Test
    fun `resetWatchPreferences calls the correct connectionInterface`() {
        repository.resetWatchPreferences(dummyWatch1)
        verify(exactly = 1) { connectionInterface.resetWatchPreferences(dummyWatch1) }
    }

    @Test
    fun `refreshData calls refreshData on all provided connection interfaces`() {
        repository.refreshData()
        verify(exactly = 1) { connectionInterface.refreshData() }
    }
}
