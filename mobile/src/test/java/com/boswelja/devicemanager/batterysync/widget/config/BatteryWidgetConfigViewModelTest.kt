package com.boswelja.devicemanager.batterysync.widget.config

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BatteryWidgetConfigViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1")
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2")
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3")

    private lateinit var watchDatabase: WatchDatabase
    private lateinit var viewModel: BatteryWidgetConfigViewModel

    @Before
    fun setUp() {
        watchDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WatchDatabase::class.java
        ).allowMainThreadQueries().build()

        viewModel = BatteryWidgetConfigViewModel(
            ApplicationProvider.getApplicationContext(),
            watchDatabase
        )
    }

    @After
    fun tearDown() {
        watchDatabase.clearAllTables()
        watchDatabase.close()
    }

    @Test
    fun `allRegisteredWatches updates correctly when watches change`() {
        // Check with multiple watches
        setWatchesInDatabase(listOf(dummyWatch1, dummyWatch2, dummyWatch3))
        viewModel.allRegisteredWatches.getOrAwaitValue {
            assertThat(it).containsExactly(dummyWatch1, dummyWatch2, dummyWatch3)
        }

        // Check with single watch
        setWatchesInDatabase(listOf(dummyWatch1))
        viewModel.allRegisteredWatches.getOrAwaitValue {
            assertThat(it).containsExactly(dummyWatch1)
        }

        // Check with no watches
        setWatchesInDatabase(emptyList())
        viewModel.allRegisteredWatches.getOrAwaitValue { assertThat(it).isEmpty() }
    }

    /**
     * Clear the database and add a list of [Watch] to it.
     */
    private fun setWatchesInDatabase(watches: List<Watch>) {
        watchDatabase.clearAllTables()
        watches.forEach {
            watchDatabase.watchDao().add(it)
        }
    }
}
