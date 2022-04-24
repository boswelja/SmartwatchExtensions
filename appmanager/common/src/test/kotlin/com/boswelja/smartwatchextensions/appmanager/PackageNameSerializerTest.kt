package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
}
