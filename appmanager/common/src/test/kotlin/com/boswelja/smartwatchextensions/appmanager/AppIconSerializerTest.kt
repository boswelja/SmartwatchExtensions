package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppIconSerializerTest {

    @Test
    fun serializesCorrectly() = runTest {
        val appIcon = AppIcon(
            packageName = "com.my.package",
            iconBytes = byteArrayOf()
        )
        val bytes = AppIconSerializer.serialize(appIcon)
        val deserializedIcon = AppIconSerializer.deserialize(bytes)
        assertEquals(appIcon, deserializedIcon)
    }
}
