/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import timber.log.Timber

class MainApplicationTest {

    @Test
    fun onCreate() {
        if (BuildConfig.DEBUG) {
            assertWithMessage("Checking Timber tree count correct")
                .that(Timber.treeCount())
                .isEqualTo(1)
        } else {
            assertWithMessage("Checking Timber tree count correct")
                .that(Timber.treeCount())
                .isEqualTo(0)
        }
    }
}
