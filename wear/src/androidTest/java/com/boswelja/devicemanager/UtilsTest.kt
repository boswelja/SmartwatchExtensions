/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilsTest {

    @Test
    fun isAppInstalled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(Utils.isAppInstalled(context.packageManager, context.packageName))
                .isTrue()
    }
}
