package com.boswelja.smartwatchextensions.settings

expect fun <T> runSuspendingTest(block: suspend () -> T)
