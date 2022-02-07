package com.boswelja.smartwatchextensions.core.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
        assertFails {
            IntSettingSerializer.deserialize(bytes)
        }
    }
}
