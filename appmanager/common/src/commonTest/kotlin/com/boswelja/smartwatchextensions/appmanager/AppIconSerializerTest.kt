package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.toByteString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class AppIconSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val appIcon = AppIcon(
            packageName = "com.my.package",
            iconBytes = byteArrayOf().toByteString()
        )
        val bytes = AppIconSerializer.serialize(appIcon)
        val deserializedIcon = AppIconSerializer.deserialize(bytes)
        assertEquals(appIcon, deserializedIcon)
    }

    @Test
    fun throwsWhenBytesInvalid() = runTest {
        val bytes = byteArrayOf(1)
        assertFails {
            AddedOrUpdatedAppsSerializer.deserialize(bytes)
        }
    }
}
