package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddedOrUpdatedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val appList = createAppList(100)
        val bytes = AddedOrUpdatedAppsSerializer.serialize(appList)
        val deserializedList = AddedOrUpdatedAppsSerializer.deserialize(bytes)
        assertEquals(appList, deserializedList)
    }

    private fun createAppList(count: Int): AppList {
        val listOfApps = (0 until count).map {
            App(
                versionName = "2.0",
                versionCode = 2,
                packageName = "com.package.number$it",
                label = "My App $it",
                isSystemApp = false,
                hasLaunchActivity = true,
                isEnabled = true,
                installTime = 1,
                updateTime = 2
            )
        }
        return AppList(listOfApps)
    }
}
