package com.boswelja.smartwatchextensions.core.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntSettingsSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val intSetting = IntSetting(key = "key", value = 65)
        val bytes = IntSettingSerializer.serialize(intSetting)
        val deserializedList = IntSettingSerializer.deserialize(bytes)
        assertEquals(intSetting, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertThrows(Exception::class.java) {
            runBlocking { IntSettingSerializer.deserialize(bytes) }
        }
    }
}
