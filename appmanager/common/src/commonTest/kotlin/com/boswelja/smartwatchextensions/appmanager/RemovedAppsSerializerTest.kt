package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RemovedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val removedApps = createRemovedApps(100)
        val bytes = RemovedAppsSerializer.serialize(removedApps)
        val deserializedList = RemovedAppsSerializer.deserialize(bytes)
        assertEquals(removedApps, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
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
