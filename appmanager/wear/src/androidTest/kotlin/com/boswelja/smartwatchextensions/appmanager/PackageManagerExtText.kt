package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackageManagerExtText {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getAllApps_loadsAllPackages() {
        val truePackages = context.packageManager.getInstalledPackages(0)
        val result = context.packageManager.getAllApps()
        assertTrue {
            truePackages.all { packageInfo -> result.any { it.packageName == packageInfo.packageName } }
        }
    }

    @Test
    fun isPackageInstalled_returnsTrueForInstalledPackage() {
        val packageName = context.packageName

        val result = context.packageManager.isPackageInstalled(packageName)
        assertTrue(result)
    }

    @Test
    fun isPackageInstalled_returnsFalseForMissingPackage() {
        val packageName = "some.package.that.doesnt.exist"

        val result = context.packageManager.isPackageInstalled(packageName)
        assertFalse(result)
    }

    @Test
    fun launchIntent_returnsIntentForPackage() {
        val packageName = "com.android.settings"

        val intent = context.packageManager.launchIntent(packageName)
        assertNotNull(intent)
    }

    @Test
    fun launchIntent_returnsNullForMissingPackage() {
        val packageName = "some.package.that.doesnt.exist"

        val intent = context.packageManager.launchIntent(packageName)
        assertNull(intent)
    }

    @Test
    fun requestUninstallIntent_returnsIntentForPackage() {
        val packageName = context.packageName

        val intent = context.packageManager.requestUninstallIntent(packageName)
        assertNotNull(intent)
    }

    @Test
    fun requestUninstallIntent_returnsNullForMissingPackage() {
        val packageName = "some.package.that.doesnt.exist"

        val intent = context.packageManager.requestUninstallIntent(packageName)
        assertNull(intent)
    }
}
