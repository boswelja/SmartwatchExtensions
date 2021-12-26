package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boswelja.watchconnection.wear.message.MessageClient
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppManagerCacheValidatorReceiverTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory { messageClient }
            }
        )
    }

    private lateinit var messageClient: MessageClient

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        messageClient = mockk()
    }

    @Test
    fun getAddedPackages_returnsAddedApps() {
        // Create data
        val receiver = AppManagerCacheValidatorReceiver()
        val currentPackages = createApps(100)
        val cachedPackages = currentPackages
            .drop(10)
            .map { AppVersion(it.packageName, it.versionCode) }
        val trueAddedApps = currentPackages
            .filterNot { packageInfo -> cachedPackages.any { it.packageName == packageInfo.packageName } }

        // Make the call and check the result
        val addedApps = receiver.getAddedPackages(currentPackages, cachedPackages)
        assertEquals(trueAddedApps.count(), addedApps.apps.count())
        assertTrue { trueAddedApps.containsAll(addedApps.apps) }

        // Check with empty cache
        val addedAppsNoCache = receiver.getAddedPackages(currentPackages, emptyList())
        assertEquals(currentPackages.count(), addedAppsNoCache.apps.count())

        // Check with empty current packages
        val addedAppsNoCurrent = receiver.getAddedPackages(emptyList(), cachedPackages)
        assertEquals(0, addedAppsNoCurrent.apps.count())

        // Check with identical cache and current
        val addedAppsIdentical = receiver.getAddedPackages(
            currentPackages,
            currentPackages.map { AppVersion(it.packageName, it.versionCode) }
        )
        assertEquals(0, addedAppsIdentical.apps.count())
    }

    @Test
    fun getUpdatedPackages_returnsUpdatedApps() {
        // Create data
        val receiver = AppManagerCacheValidatorReceiver()
        val currentPackages = createApps(100)
        val cachedPackages = currentPackages
            .map {
                val shouldDowngrade = Random.nextInt(0, 10) <= 1
                AppVersion(
                    it.packageName,
                    if (shouldDowngrade) it.versionCode - 1 else it.versionCode
                )
            }
        val trueUpdatedApps = currentPackages
            .filter { packageInfo -> cachedPackages.any { it.packageName == packageInfo.packageName && it.versionCode < packageInfo.versionCode } }

        // Make the call and check the result
        val updatedApps = receiver.getUpdatedPackages(currentPackages, cachedPackages)
        assertEquals(trueUpdatedApps.count(), updatedApps.apps.count())
        assertTrue { trueUpdatedApps.containsAll(updatedApps.apps) }

        // Check with empty cache
        val updatedAppsNoCache = receiver.getUpdatedPackages(currentPackages, emptyList())
        assertEquals(0, updatedAppsNoCache.apps.count())

        // Check with empty current packages
        val updatedAppsNoCurrent = receiver.getUpdatedPackages(emptyList(), cachedPackages)
        assertEquals(0, updatedAppsNoCurrent.apps.count())

        // Check with identical cache and current
        val updatedAppsIdentical = receiver.getUpdatedPackages(
            currentPackages,
            currentPackages.map { AppVersion(it.packageName, it.versionCode) }
        )
        assertEquals(0, updatedAppsIdentical.apps.count())
    }

    @Test
    fun getRemovedPackages_returnsRemovedApps() {
        // Create data
        val receiver = AppManagerCacheValidatorReceiver()
        val maxPackages = createApps(100)
        val currentPackages = maxPackages.drop(10)
        val cachedPackages = maxPackages.map { AppVersion(it.packageName, it.versionCode) }
        val trueRemovedApps = cachedPackages
            .filter { packageInfo -> currentPackages.none { it.packageName == packageInfo.packageName } }
            .map { it.packageName }

        // Make the call and check the result
        val removedApps = receiver.getRemovedPackages(currentPackages, cachedPackages)
        assertEquals(trueRemovedApps.count(), removedApps.packages.count())
        assertTrue { trueRemovedApps.containsAll(removedApps.packages) }

        // Check with empty cache
        val removedAppsNoCache = receiver.getRemovedPackages(currentPackages, emptyList())
        assertEquals(0, removedAppsNoCache.packages.count())

        // Check with empty current packages
        val removedAppsNoCurrent = receiver.getRemovedPackages(emptyList(), cachedPackages)
        assertEquals(cachedPackages.count(), removedAppsNoCurrent.packages.count())

        // Check with identical cache and current
        val removedAppsIdentical = receiver.getRemovedPackages(
            currentPackages,
            currentPackages.map { AppVersion(it.packageName, it.versionCode) }
        )
        assertEquals(0, removedAppsIdentical.packages.count())
    }

    private fun createApps(count: Int): List<App> {
        return (0 until count).map {
            App(
                versionName = "2.0",
                versionCode = 2,
                packageName = "com.package.number$it",
                label = "My App $it",
                isSystemApp = false,
                hasLaunchActivity = true,
                isEnabled = true,
                installTime = 1,
                lastUpdateTime = 2,
                requestedPermissions = emptyList()
            )
        }
    }
}
