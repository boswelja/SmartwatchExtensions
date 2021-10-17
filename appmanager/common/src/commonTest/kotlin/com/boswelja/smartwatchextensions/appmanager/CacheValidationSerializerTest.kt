package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CacheValidationSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val data = 3214
        val bytes = CacheValidationSerializer.serialize(data)
        val deserializedData = CacheValidationSerializer.deserialize(bytes)
        assertEquals(data, deserializedData)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            CacheValidationSerializer.deserialize(bytes)
        }
    }
}
