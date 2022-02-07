package com.boswelja.smartwatchextensions.core.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
        assertFails {
            BoolSettingSerializer.deserialize(bytes)
        }
    }
}
