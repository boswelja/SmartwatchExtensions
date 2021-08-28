package com.boswelja.smartwatchextensions

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun runBlockingTimeout(timeout: Long, run: suspend () -> Unit) {
    runBlocking {
        withTimeout(timeout) {
            run()
        }
    }
}
