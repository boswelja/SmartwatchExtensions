package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.runBlocking

actual fun <T> runSuspendingTest(block: suspend () -> T) {
    runBlocking { block() }
}
