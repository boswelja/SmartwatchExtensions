/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

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
