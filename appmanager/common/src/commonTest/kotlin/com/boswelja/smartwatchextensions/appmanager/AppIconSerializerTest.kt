package com.boswelja.smartwatchextensions.appmanager

import okio.ByteString.Companion.toByteString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AppIconSerializerTest {

    @Test
    fun serializesCorrectly() = runSuspendingTest {
        val appIcon = AppIcon(
            packageName = "com.my.package",
            iconBytes = byteArrayOf().toByteString()
        )
        val bytes = AppIconSerializer.serialize(appIcon)
        val deserializedIcon = AppIconSerializer.deserialize(bytes)
        assertEquals(appIcon, deserializedIcon)
    }

    @Test
    fun throwsWhenBytesInvalid() = runSuspendingTest {
        val bytes = byteArrayOf(1)
        assertFails {
            AddedOrUpdatedAppsSerializer.deserialize(bytes)
        }
    }

}