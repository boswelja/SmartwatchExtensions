package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class PackageNameSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val data = "com.package.name"
        val bytes = PackageNameSerializer.serialize(data)
        val deserializedData = PackageNameSerializer.deserialize(bytes)
        assertEquals(data, deserializedData)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertFails {
            PackageNameSerializer.deserialize(bytes)
        }
    }
}
