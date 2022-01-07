package com.boswelja.smartwatchextensions.batterysync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class BatteryStatsSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
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
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            BatteryStatsSerializer.deserialize(bytes)
        }
    }
}
