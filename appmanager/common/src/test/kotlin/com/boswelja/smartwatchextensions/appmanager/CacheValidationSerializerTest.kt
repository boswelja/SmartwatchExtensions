package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CacheValidationSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val data = createAppVersions(100)
        val bytes = CacheValidationSerializer.serialize(data)
        val deserializedData = CacheValidationSerializer.deserialize(bytes)
        assertEquals(data, deserializedData)
    }

    private fun createAppVersions(count: Int): AppVersions {
        val versions = (0 until count).map {
            AppVersion(it.toString(), it.toLong())
        }
        return AppVersions(versions)
    }
}
