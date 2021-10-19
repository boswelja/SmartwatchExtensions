package com.boswelja.smartwatchextensions.batterysync

expect fun <T> runSuspendingTest(block: suspend () -> T)
