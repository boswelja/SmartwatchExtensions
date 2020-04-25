package com.boswelja.devicemanager.common

import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.Extensions.toByteArray
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage

class ExtensionsTest {

    @Test
    fun toByteArray() {
        val trueByteArray = byteArrayOf(1)
        val falseByteArray = byteArrayOf(0)

        assertWithMessage("Checking true bool toByteArray() works")
                .that(true.toByteArray())
                .isEqualTo(trueByteArray)
        assertWithMessage("Checking false bool toByteArray() works")
                .that(false.toByteArray())
                .isEqualTo(falseByteArray)
    }

    @Test
    fun fromByteArray() {
        val trueByteArray = byteArrayOf(1)
        val falseByteArray = byteArrayOf(0)

        assertWithMessage("Checking true bool fromByteArray() works")
                .that(Boolean.fromByteArray(trueByteArray))
                .isTrue()
        assertWithMessage("Checking false bool fromByteArray() works")
                .that(Boolean.fromByteArray(falseByteArray))
                .isFalse()
    }
}