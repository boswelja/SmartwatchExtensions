package com.boswelja.devicemanager.watchmanager

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.connection.DummyConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WatchRepositoryTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val platformIdentifier1 = "dummy1"
    private val platformIdentifier2 = "dummy2"

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", platformIdentifier1)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", platformIdentifier1)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", platformIdentifier2)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private lateinit var repository: WatchRepository
    private lateinit var database: WatchDatabase
    private lateinit var connectionInterface1: DummyConnectionInterface
    private lateinit var connectionInterface2: DummyConnectionInterface

    @Before
    fun setUp() {
        connectionInterface1 = spyk(DummyConnectionInterface(platformIdentifier1))
        connectionInterface2 = spyk(DummyConnectionInterface(platformIdentifier2))
        database = spyk(
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WatchDatabase::class.java
            ).allowMainThreadQueries().build()
        )
        repository = WatchRepository(database, connectionInterface1, connectionInterface2)
        clearAllMocks(
            answers = false,
            recordedCalls = true,
            childMocks = false,
            regularMocks = true,
            objectMocks = false,
            staticMocks = false,
            constructorMocks = false
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `refreshData calls correct function on all provided WatchConnectionInterface`() {
        repository.refreshData()
        verify(exactly = 1) { connectionInterface1.refreshData() }
        verify(exactly = 1) { connectionInterface2.refreshData() }
    }

    @Test
    fun `renameWatch renames the watch in the database`(): Unit = runBlocking {
        val newName = "new name"
        repository.renameWatch(dummyWatch1, newName)
        verify(exactly = 1) { database.renameWatch(dummyWatch1, newName) }
    }

    @Test
    fun `refreshData calls refreshData on all provided connection interfaces`() {
        repository.refreshData()
        verify(exactly = 1) { connectionInterface1.refreshData() }
        verify(exactly = 1) { connectionInterface2.refreshData() }
    }

    @Test
    fun `availableWatches updates correctly when connection interface availableWatches changes`() {
        // Test adding a list from one connection interface
        connectionInterface1.mutableAvailableWatches.value = listOf(dummyWatch1, dummyWatch2)
        assertThat(repository.availableWatches.getOrAwaitValue())
            .containsExactly(dummyWatch1, dummyWatch2)

        // Test adding a list from a second connection interface
        connectionInterface2.mutableAvailableWatches.value = listOf(dummyWatch3)
        assertThat(repository.availableWatches.getOrAwaitValue())
            .containsExactly(dummyWatch1, dummyWatch2, dummyWatch3)
    }

    @Test
    fun `registeredWatches updates status for watches from a platform that has a data change`() {
        val dummyObserver = Observer<List<Watch>> {
            Timber.d("Observed $it")
        }
        repository.registeredWatches.observeForever(dummyObserver)
        dummyWatches.forEach { database.addWatch(it) }

        connectionInterface1.dataChanged.fire()

        dummyWatches.filter { it.platform == connectionInterface1.platformIdentifier }.forEach {
            verify { connectionInterface1.getWatchStatus(it.id, true) }
        }

        connectionInterface2.dataChanged.fire()

        dummyWatches.filter { it.platform == connectionInterface2.platformIdentifier }.forEach {
            verify { connectionInterface2.getWatchStatus(it.id, true) }
        }

        repository.registeredWatches.removeObserver(dummyObserver)
    }
}
