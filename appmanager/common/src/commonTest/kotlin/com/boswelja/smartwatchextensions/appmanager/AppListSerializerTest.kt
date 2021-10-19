package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AppListSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val appList = createAppList(100)
        val bytes = AppListSerializer.serialize(appList)
        val deserializedList = AppListSerializer.deserialize(bytes)
        assertEquals(appList, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            AppListSerializer.deserialize(bytes)
        }
    }

    private fun createAppList(count: Int): AppList {
        val listOfApps = (0 until count).map { App() }
        return AppList(listOfApps)
    }
}
