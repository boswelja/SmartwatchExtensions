package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.runBlocking

actual fun <T> runSuspendingTest(block: suspend () -> T) {
    runBlocking { block() }
}
