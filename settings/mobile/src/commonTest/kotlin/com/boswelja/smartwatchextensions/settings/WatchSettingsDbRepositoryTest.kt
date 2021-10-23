package com.boswelja.smartwatchextensions.settings

import app.cash.turbine.test
import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabase
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

class WatchSettingsDbRepositoryTest {

    private val watchIds = listOf("id1", "id2", "id3")

    private lateinit var repository: WatchSettingsRepository

    @BeforeTest
    fun setUp() {
        val sqlDriver = createTestSqlDriver().apply {
            WatchSettingsDatabase.Schema.create(this)
        }
        val database = WatchSettingsDatabase(sqlDriver)
        repository = WatchSettingsDbRepository(database, Dispatchers.Default)
    }

    @Test
    fun putBooleanPutsBoolean() = runSuspendingTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"

        // Set boolean to false and check value
        repository.putBoolean(testId, testKey, false)
        assertEquals(false, repository.getBoolean(testId, testKey).first())

        // Set boolean to true and check value
        repository.putBoolean(testId, testKey, true)
        assertEquals(true, repository.getBoolean(testId, testKey).first())
    }

    @Test
    fun putIntPutsInt() = runSuspendingTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"

        // Put 0 and check value
        repository.putInt(testId, testKey, 0)
        assertEquals(0, repository.getInt(testId, testKey).first())

        // Update data and check value
        repository.putInt(testId, testKey, 3255)
        assertEquals(3255, repository.getInt(testId, testKey).first())
    }

    @Test
    fun getBooleanFlowsDefaultOnNoValue() = runSuspendingTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"
        val defaultValue = true

        // Make sure data is removed and check value
        repository.deleteForWatch(testId)
        assertEquals(defaultValue, repository.getBoolean(testId, testKey, defaultValue).first())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getBooleanFlowsChanges() = runSuspendingTest {
        val testId = watchIds.first()
        val testKey = "key"

        // Put initial value
        repository.putBoolean(testId, testKey, false)

        repository.getBoolean(testId, testKey).test {
            // Check initial value
            assertEquals(false, awaitItem())

            // Update value and check
            repository.putBoolean(testId, testKey, true)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun getIntFlowsDefaultOnNoValue() = runSuspendingTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"
        val defaultValue = 100

        // Make sure data is removed and check value
        repository.deleteForWatch(testId)
        assertEquals(defaultValue, repository.getInt(testId, testKey, defaultValue).first())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getIntFlowsChanges() = runSuspendingTest {
        val testId = watchIds.first()
        val testKey = "key"

        // Put initial value
        repository.putInt(testId, testKey, 0)

        repository.getInt(testId, testKey).test {
            // Check initial value
            assertEquals(0, awaitItem())

            // Update value and check
            repository.putInt(testId, testKey, 1000)
            assertEquals(1000, awaitItem())
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getIdsWithBooleanSetFlowsUpdates() = runSuspendingTest {
        val testKey = "key"
        val testValue = true

        // Clear all data first
        watchIds.forEach { repository.deleteForWatch(it) }

        repository.getIdsWithBooleanSet(testKey, testValue).test {
            // Check initial value is empty
            assertTrue { awaitItem().isEmpty() }

            // Set IDs and check
            val idsSet = mutableListOf<String>()
            watchIds.forEach { id ->
                idsSet.add(id)
                repository.putBoolean(id, testKey, testValue)
                assertTrue { awaitItem().containsAll(idsSet) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getIdsWithIntSetFlowsUpdates() = runSuspendingTest {
        val testKey = "key"
        val testValue = 1

        // Clear all data first
        watchIds.forEach { repository.deleteForWatch(it) }

        repository.getIdsWithIntSet(testKey, testValue).test {
            // Check initial value is empty
            assertTrue { awaitItem().isEmpty() }

            // Set IDs and check
            val idsSet = mutableListOf<String>()
            watchIds.forEach { id ->
                idsSet.add(id)
                repository.putInt(id, testKey, testValue)
                assertTrue { awaitItem().containsAll(idsSet) }
            }
        }
    }

    @Test
    fun deleteForWatchDeletesForWatch() = runSuspendingTest {
        val testKey = "key"

        // Put data
        watchIds.forEach { id ->
            repository.putBoolean(id, testKey, true)
            repository.putInt(id, testKey, 1)
        }

        // Delete data
        watchIds.forEach { id ->
            repository.deleteForWatch(id)
            assertFalse { repository.getIdsWithBooleanSet(testKey, true).first().contains(id) }
            assertFalse { repository.getIdsWithIntSet(testKey, 1).first().contains(id) }
        }
    }
}

expect fun createTestSqlDriver(): SqlDriver
