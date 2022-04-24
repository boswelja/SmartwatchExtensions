package com.boswelja.smartwatchextensions.core.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoolSettingsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val boolSetting = BoolSetting(key = "key", value = true)
        val bytes = BoolSettingSerializer.serialize(boolSetting)
        val deserializedList = BoolSettingSerializer.deserialize(bytes)
        assertEquals(boolSetting, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertThrows(Exception::class.java) {
            runBlocking { BoolSettingSerializer.deserialize(bytes) }
        }
    }
}
