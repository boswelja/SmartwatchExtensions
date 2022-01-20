package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryStatsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val batteryStats = BatteryStats(
            percent = 65,
            charging = true,
            timestamp = 1000
        )
        val bytes = BatteryStatsSerializer.serialize(batteryStats)
        val deserializedList = BatteryStatsSerializer.deserialize(bytes)
        assertEquals(batteryStats, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertFails {
            BatteryStatsSerializer.deserialize(bytes)
        }
    }
}
