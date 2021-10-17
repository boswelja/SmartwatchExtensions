package com.boswelja.smartwatchextensions.appmanager

expect fun <T> runSuspendingTest(block: suspend () -> T)
