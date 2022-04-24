package com.boswelja.smartwatchextensions.core.settings

import com.boswelja.smartwatchextensions.core.settings.database.WatchSettingsDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WatchSettingsDbRepositoryTest {

    private val watchIds = listOf("id1", "id2", "id3")

    private lateinit var repository: WatchSettingsRepository

    @Before
    fun setUp() {
        val sqlDriver = createTestSqlDriver().apply {
            WatchSettingsDatabase.Schema.create(this)
        }
        val database = WatchSettingsDatabase(sqlDriver)
        repository = WatchSettingsDbRepository(database, Dispatchers.Default)
    }

    @Test
    fun putBooleanPutsBoolean() = runTest {
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
    fun putIntPutsInt() = runTest {
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
    fun getBooleanFlowsDefaultOnNoValue() = runTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"
        val defaultValue = true

        // Make sure data is removed and check value
        repository.deleteForWatch(testId)
        assertEquals(defaultValue, repository.getBoolean(testId, testKey, defaultValue).first())
    }

    @Test
    fun getBooleanFlowsChanges() = runTest {
        val testId = watchIds.first()
        val testKey = "key"

        // Put initial value
        repository.putBoolean(testId, testKey, false)

        repository.getBoolean(testId, testKey).test2(2.seconds) {
            // Check initial value
            assertEquals(false, awaitItem())

            // Update value and check
            repository.putBoolean(testId, testKey, true)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun getIntFlowsDefaultOnNoValue() = runTest {
        // Set up test data
        val testId = watchIds.first()
        val testKey = "key"
        val defaultValue = 100

        // Make sure data is removed and check value
        repository.deleteForWatch(testId)
        assertEquals(defaultValue, repository.getInt(testId, testKey, defaultValue).first())
    }

    @Test
    fun getIntFlowsChanges() = runTest {
        val testId = watchIds.first()
        val testKey = "key"

        // Put initial value
        repository.putInt(testId, testKey, 0)

        repository.getInt(testId, testKey).test2(2.seconds) {
            // Check initial value
            assertEquals(0, awaitItem())

            // Update value and check
            repository.putInt(testId, testKey, 1000)
            assertEquals(1000, awaitItem())
        }
    }

    @Test
    fun getIdsWithBooleanSetFlowsUpdates() = runTest {
        val testKey = "key"
        val testValue = true

        // Clear all data first
        watchIds.forEach { repository.deleteForWatch(it) }

        repository.getIdsWithBooleanSet(testKey, testValue).test2(2.seconds) {
            // Check initial value is empty
            assertTrue(awaitItem().isEmpty())

            // Set IDs and check
            val idsSet = mutableListOf<String>()
            watchIds.forEach { id ->
                idsSet.add(id)
                repository.putBoolean(id, testKey, testValue)
                assertTrue(awaitItem().containsAll(idsSet))
            }
        }
    }

    @Test
    fun getIdsWithIntSetFlowsUpdates() = runTest {
        val testKey = "key"
        val testValue = 1

        // Clear all data first
        watchIds.forEach { repository.deleteForWatch(it) }

        repository.getIdsWithIntSet(testKey, testValue).test2(2.seconds) {
            // Check initial value is empty
            assertTrue(awaitItem().isEmpty())

            // Set IDs and check
            val idsSet = mutableListOf<String>()
            watchIds.forEach { id ->
                idsSet.add(id)
                repository.putInt(id, testKey, testValue)
                assertTrue(awaitItem().containsAll(idsSet))
            }
        }
    }

    @Test
    fun deleteForWatchDeletesForWatch() = runTest {
        val testKey = "key"

        // Put data
        watchIds.forEach { id ->
            repository.putBoolean(id, testKey, true)
            repository.putInt(id, testKey, 1)
        }

        // Delete data
        watchIds.forEach { id ->
            repository.deleteForWatch(id)
            assertFalse(repository.getIdsWithBooleanSet(testKey, true).first().contains(id))
            assertFalse(repository.getIdsWithIntSet(testKey, 1).first().contains(id))
        }
    }
}

fun createTestSqlDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
