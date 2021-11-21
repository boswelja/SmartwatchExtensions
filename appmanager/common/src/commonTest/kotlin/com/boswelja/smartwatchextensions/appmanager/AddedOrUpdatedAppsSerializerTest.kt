package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AddedOrUpdatedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val appList = createAppList(100)
        val bytes = AddedOrUpdatedAppsSerializer.serialize(appList)
        val deserializedList = AddedOrUpdatedAppsSerializer.deserialize(bytes)
        assertEquals(appList, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            AddedOrUpdatedAppsSerializer.deserialize(bytes)
        }
    }

    private fun createAppList(count: Int): AppList {
        val listOfApps = (0 until count).map { App() }
        return AppList(listOfApps)
    }
}
