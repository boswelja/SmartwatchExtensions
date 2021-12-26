package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class AddedOrUpdatedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val appList = createAppList(100)
        val bytes = AddedOrUpdatedAppsSerializer.serialize(appList)
        val deserializedList = AddedOrUpdatedAppsSerializer.deserialize(bytes)
        assertEquals(appList, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
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
