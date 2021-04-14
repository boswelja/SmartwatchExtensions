package com.boswelja.smartwatchextensions

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
