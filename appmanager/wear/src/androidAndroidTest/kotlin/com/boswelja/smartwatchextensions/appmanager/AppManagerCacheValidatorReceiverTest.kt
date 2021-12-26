package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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
        val currentPackages = createPackageInfos(100)
        val cachedPackages = currentPackages
            .drop(10)
            .map { AppVersion(it.packageName, it.longVersionCode) }
        val trueAddedApps = currentPackages
            .filterNot { packageInfo -> cachedPackages.any { it.packageName == packageInfo.packageName } }
            .map { it.toApp(context.packageManager) }

        // Make the call and check the result
        val addedApps = receiver.getAddedPackages(context, currentPackages, cachedPackages)
        assertEquals(trueAddedApps.count(), addedApps.apps.count())
        assertTrue { trueAddedApps.containsAll(addedApps.apps) }

        // Check with empty cache
        val addedAppsNoCache = receiver.getAddedPackages(context, currentPackages, emptyList())
        assertEquals(currentPackages.count(), addedAppsNoCache.apps.count())

        // Check with empty current packages
        val addedAppsNoCurrent = receiver.getAddedPackages(context, emptyList(), cachedPackages)
        assertEquals(0, addedAppsNoCurrent.apps.count())

        // Check with identical cache and current
        val addedAppsIdentical = receiver.getAddedPackages(
            context,
            currentPackages,
            currentPackages.map { AppVersion(it.packageName, it.longVersionCode) }
        )
        assertEquals(0, addedAppsIdentical.apps.count())
    }

    @Test
    fun getUpdatedPackages_returnsUpdatedApps() {
        // Create data
        val receiver = AppManagerCacheValidatorReceiver()
        val currentPackages = createPackageInfos(100)
        val cachedPackages = currentPackages
            .map {
                val shouldDowngrade = Random.nextInt(0, 10) <= 1
                AppVersion(
                    it.packageName,
                    if (shouldDowngrade) it.longVersionCode - 1 else it.longVersionCode
                )
            }
        val trueUpdatedApps = currentPackages
            .filter { packageInfo -> cachedPackages.any { it.packageName == packageInfo.packageName && it.versionCode < packageInfo.longVersionCode } }
            .map { it.toApp(context.packageManager) }

        // Make the call and check the result
        val updatedApps = receiver.getUpdatedPackages(context, currentPackages, cachedPackages)
        assertEquals(trueUpdatedApps.count(), updatedApps.apps.count())
        assertTrue { trueUpdatedApps.containsAll(updatedApps.apps) }

        // Check with empty cache
        val updatedAppsNoCache = receiver.getUpdatedPackages(context, currentPackages, emptyList())
        assertEquals(0, updatedAppsNoCache.apps.count())

        // Check with empty current packages
        val updatedAppsNoCurrent = receiver.getUpdatedPackages(context, emptyList(), cachedPackages)
        assertEquals(0, updatedAppsNoCurrent.apps.count())

        // Check with identical cache and current
        val updatedAppsIdentical = receiver.getUpdatedPackages(
            context,
            currentPackages,
            currentPackages.map { AppVersion(it.packageName, it.longVersionCode) }
        )
        assertEquals(0, updatedAppsIdentical.apps.count())
    }

    @Test
    fun getRemovedPackages_returnsRemovedApps() {
        // Create data
        val receiver = AppManagerCacheValidatorReceiver()
        val maxPackages = createPackageInfos(100)
        val currentPackages = maxPackages.drop(10)
        val cachedPackages = maxPackages.map { AppVersion(it.packageName, it.longVersionCode) }
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
            currentPackages.map { AppVersion(it.packageName, it.longVersionCode) }
        )
        assertEquals(0, removedAppsIdentical.packages.count())
    }

    private fun createPackageInfos(count: Int): List<PackageInfo> {
        return (0 until count).map {
            PackageInfo().apply {
                versionName = "2.0"
                longVersionCode = 2
                packageName = "com.package.number$it"
                lastUpdateTime = 2
                firstInstallTime = 1
                permissions = emptyArray()
                applicationInfo = ApplicationInfo().apply {
                    enabled = true
                    flags = 0
                    nonLocalizedLabel = "My App $it"
                }
            }
        }
    }
}
