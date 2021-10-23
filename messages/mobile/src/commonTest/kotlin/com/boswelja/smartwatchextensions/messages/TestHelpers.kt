package com.boswelja.smartwatchextensions.messages

expect fun <T> runSuspendingTest(block: suspend () -> T)
