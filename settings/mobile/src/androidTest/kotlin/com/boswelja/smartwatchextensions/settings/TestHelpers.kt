package com.boswelja.smartwatchextensions.settings

import kotlinx.coroutines.runBlocking

actual fun <T> runSuspendingTest(block: suspend () -> T) {
    runBlocking { block() }
}
