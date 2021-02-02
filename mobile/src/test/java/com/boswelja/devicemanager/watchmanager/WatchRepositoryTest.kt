package com.boswelja.devicemanager.watchmanager

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.watchmanager.connection.DummyConnectionInterface
import com.boswelja.devicemanager.watchmanager.connection.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WatchRepositoryTest {

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionInterface.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private lateinit var repository: WatchRepository
    private lateinit var database: WatchDatabase
    private lateinit var connectionInterface: DummyConnectionInterface

    @Before
    fun setUp() {
        connectionInterface = spyk(DummyConnectionInterface())
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WatchDatabase::class.java
        ).build()
        repository = WatchRepository(database, connectionInterface)
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
}
