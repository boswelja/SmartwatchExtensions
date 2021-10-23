package com.boswelja.smartwatchextensions.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class IntSettingsSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val intSetting = IntSetting(key = "key", value = 65)
        val bytes = IntSettingSerializer.serialize(intSetting)
        val deserializedList = IntSettingSerializer.deserialize(bytes)
        assertEquals(intSetting, deserializedList)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            IntSettingSerializer.deserialize(bytes)
        }
    }
}
