package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class RemovedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val removedApps = createRemovedApps(100)
        val bytes = RemovedAppsSerializer.serialize(removedApps)
        val deserializedList = RemovedAppsSerializer.deserialize(bytes)
        assertEquals(removedApps, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertFails {
            RemovedAppsSerializer.deserialize(bytes)
        }
    }

    private fun createRemovedApps(count: Int): RemovedApps {
        val packages = (0 until count).map { it.toString() }
        return RemovedApps(packages)
    }
}
