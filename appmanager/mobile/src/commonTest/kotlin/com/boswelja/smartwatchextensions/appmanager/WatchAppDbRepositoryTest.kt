package com.boswelja.smartwatchextensions.appmanager

import app.cash.turbine.test
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.appmanager.database.watchAppDbAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class WatchAppDbRepositoryTest {

    private val watchIds = listOf("id1", "id2", "id3")

    private lateinit var repository: WatchAppRepository

    @BeforeTest
    fun setUp() {
        val sqlDriver = createTestSqlDriver().apply {
            WatchAppDatabase.Schema.create(this)
        }
        repository = WatchAppDbRepository(
            WatchAppDatabase(
                sqlDriver,
                watchAppDbAdapter
            ),
            Dispatchers.Default
        )
    }

    @Test
    fun updateAllAddsAll() = runSuspendingTest {
        // Create app details and put them in the repository
        val countPerWatch = 100
        val allAppDetails = createAppDetailsForWatches(countPerWatch)
        repository.updateAll(allAppDetails)

        // Check counts
        watchIds.forEach { watchId ->
            assertEquals(countPerWatch.toLong(), repository.countFor(watchId).first())
        }
    }

    @Test
    fun updateAllAddsNoDuplicates() = runSuspendingTest {
        // Create app details and put them in the repository
        val countPerWatch = 100
        val allAppDetails = createAppDetailsForWatches(countPerWatch)
        repository.updateAll(allAppDetails)

        // Add all again
        repository.updateAll(allAppDetails)

        // Check counts
        watchIds.forEach { watchId ->
            assertEquals(countPerWatch.toLong(), repository.countFor(watchId).first())
        }
    }

    @Test
    fun deleteForDeletesForWatchId() = runSuspendingTest {
        val countPerWatch = 100
        val allAppDetails = createAppDetailsForWatches(countPerWatch)
        repository.updateAll(allAppDetails)

        val idToClear = watchIds.first()
        repository.deleteFor(idToClear)

        // Check counts
        watchIds.forEach { watchId ->
            val count = repository.countFor(watchId).first()
            if (watchId == idToClear) {
                assertEquals(0, count)
            } else {
                assertEquals(countPerWatch.toLong(), count)
            }
        }
    }

    @Test
    fun deleteDeletesItem() = runSuspendingTest {
        val countPerWatch = 1
        val allAppDetails = createAppDetailsForWatches(countPerWatch)
        repository.updateAll(allAppDetails)

        // Delete an item
        val itemToDelete = allAppDetails.first()
        repository.delete(itemToDelete.watchId, itemToDelete.packageName)

        // Check apps
        val apps = repository.getAppsFor(itemToDelete.watchId).first()
        assertTrue { apps.none { it.packageName == itemToDelete.packageName } }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getDetailsForUpdatesWithSource() = runSuspendingTest {
        val initialAppDetails = WatchAppDetails(
            watchId = watchIds.first(),
            iconPath = null,
            installTime = 0,
            updateTime = 0,
            isEnabled = true,
            isLaunchable = true,
            isSystemApp = false,
            label = "App",
            packageName = "com.package.name",
            permissions = listOf("permission"),
            versionCode = 0,
            versionName = "0"
        )
        val updatedAppDetails = initialAppDetails.copy(
            versionCode = 1,
            versionName = "1",
            updateTime = 1
        )
        repository.updateAll(listOf(initialAppDetails))

        repository.getDetailsFor(
            initialAppDetails.watchId,
            initialAppDetails.packageName
        ).test(timeout = Duration.seconds(2)) {
            // Check first item is emitted correctly
            assertEquals(initialAppDetails, awaitItem())

            // Check updated item is emitted correctly
            repository.updateAll(listOf(updatedAppDetails))
            assertEquals(updatedAppDetails, awaitItem())

            cancel()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getAppsForUpdatesWithSource() = runSuspendingTest {
        val countPerWatch = 100
        val allAppDetails = createAppDetailsForWatches(countPerWatch)
        val watchId = watchIds.first()

        repository.getAppsFor(watchId).test {
            // Check list is empty
            assertTrue { awaitItem().isEmpty() }

            // Update the sources and check again
            repository.updateAll(allAppDetails)
            assertTrue { awaitItem().count() == countPerWatch }

            // Remove items and check again
            repository.deleteFor(watchId)
            assertTrue { awaitItem().isEmpty() }

            cancel()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun countForUpdatesWithSource() = runSuspendingTest {
        val initialCount = 50
        val initialApps = createAppDetailsForWatches(initialCount)
        val endCount = 100
        val endApps = createAppDetailsForWatches(endCount)
        val watchId = watchIds.first()

        repository.countFor(watchId).test {
            assertTrue { awaitItem() == 0L }

            // Check initial count
            repository.updateAll(initialApps)
            assertTrue { awaitItem() == initialCount.toLong() }

            // Check updated count
            repository.updateAll(endApps)
            assertTrue { awaitItem() == endCount.toLong() }

            // Remove all and check again
            repository.deleteFor(watchId)
            assertTrue { awaitItem() == 0L }

            cancel()
        }
    }

    private fun createAppDetailsForWatches(perWatchCount: Int): List<WatchAppDetails> {
        val allList = mutableListOf<WatchAppDetails>()
        watchIds.forEach { watchId ->
            (0 until perWatchCount).forEach {
                allList.add(
                    WatchAppDetails(
                        watchId = watchId,
                        iconPath = null,
                        installTime = 0,
                        updateTime = 0,
                        isEnabled = true,
                        isLaunchable = true,
                        isSystemApp = false,
                        label = "App $it",
                        packageName = it.toString(),
                        permissions = listOf(),
                        versionCode = it.toLong(),
                        versionName = it.toString()
                    )
                )
            }
        }
        return allList
    }
}

expect fun createTestSqlDriver(): SqlDriver
