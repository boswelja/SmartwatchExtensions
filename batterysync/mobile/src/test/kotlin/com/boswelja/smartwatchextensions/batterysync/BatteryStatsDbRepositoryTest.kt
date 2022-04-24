package com.boswelja.smartwatchextensions.batterysync

import app.cash.turbine.test
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryStatsDbRepositoryTest {

    private val watchIds = listOf("id1", "id2", "id3")

    lateinit var repository: BatteryStatsRepository

    @Before
    fun setUp() {
        val sqlDriver = createTestSqlDriver().apply {
            BatteryStatsDatabase.Schema.create(this)
        }
        repository = BatteryStatsDbRepository(
            BatteryStatsDatabase(sqlDriver),
            Dispatchers.Default
        )
    }

    @Test
    fun removeStatsForRemovesStats() = runTest {
        // Populate the repository
        createStatsForWatches().forEach { (id, stats) ->
            repository.updateStatsFor(id, stats)
        }

        // Remove stats for an ID
        val idToRemove = watchIds.first()
        repository.removeStatsFor(idToRemove)

        // Check result
        withContext(Dispatchers.Default) {
            repository.batteryStatsFor(idToRemove).test {
                assertNull(awaitItem())
            }
        }
    }

    @Test
    fun getStatsForFlowsUpdates() = runTest {
        // Set up values
        val idToTest = watchIds.first()
        val initialStats = BatteryStats(
            percent = 10,
            charging = true,
            timestamp = 0
        )
        val updatedStats = BatteryStats(
            percent = 90,
            charging = false,
            timestamp = 1000
        )

        // Load the first value into the repository
        repository.updateStatsFor(idToTest, initialStats)

        // Test the Flow
        withContext(Dispatchers.Default) {
            repository.batteryStatsFor(idToTest).test {
                // Check the first value matches
                assertEquals(initialStats, awaitItem())

                // Check the second value is updated
                repository.updateStatsFor(idToTest, updatedStats)
                assertEquals(updatedStats, awaitItem())
            }
        }
    }

    private fun createStatsForWatches(
        percent: Int = 65,
        charging: Boolean = false,
        timestamp: Long = 0
    ): Map<String, BatteryStats> {
        return watchIds.associateWith { BatteryStats(percent, charging, timestamp) }
    }
}

fun createTestSqlDriver(): SqlDriver {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}
