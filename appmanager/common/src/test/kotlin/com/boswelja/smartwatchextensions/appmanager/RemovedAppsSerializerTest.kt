package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemovedAppsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val removedApps = createRemovedApps(100)
        val bytes = RemovedAppsSerializer.serialize(removedApps)
        val deserializedList = RemovedAppsSerializer.deserialize(bytes)
        assertEquals(removedApps, deserializedList)
    }

    private fun createRemovedApps(count: Int): RemovedApps {
        val packages = (0 until count).map { it.toString() }
        return RemovedApps(packages)
    }
}
