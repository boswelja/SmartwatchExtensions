package com.boswelja.smartwatchextensions.messages

import kotlinx.coroutines.runBlocking

actual fun <T> runSuspendingTest(block: suspend () -> T) {
    runBlocking { block() }
}
