package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
        assertThrows(Exception::class.java) {
            runBlocking {
                BatteryStatsSerializer.deserialize(bytes)
            }
        }
    }
}
