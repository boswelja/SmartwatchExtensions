package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
        assertThrows(Exception::class.java) {
            runBlocking { PackageNameSerializer.deserialize(bytes) }
        }
    }
}
