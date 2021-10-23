package com.boswelja.smartwatchextensions.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class BoolSettingsSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val boolSetting = BoolSetting(key = "key", value = true)
        val bytes = BoolSettingSerializer.serialize(boolSetting)
        val deserializedList = BoolSettingSerializer.deserialize(bytes)
        assertEquals(boolSetting, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            BoolSettingSerializer.deserialize(bytes)
        }
    }
}
