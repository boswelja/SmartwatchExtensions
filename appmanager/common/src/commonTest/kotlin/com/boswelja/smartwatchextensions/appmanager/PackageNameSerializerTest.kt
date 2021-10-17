package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PackageNameSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val data = "com.package.name"
        val bytes = PackageNameSerializer.serialize(data)
        val deserializedData = PackageNameSerializer.deserialize(bytes)
        assertEquals(data, deserializedData)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            PackageNameSerializer.deserialize(bytes)
        }
    }
}
