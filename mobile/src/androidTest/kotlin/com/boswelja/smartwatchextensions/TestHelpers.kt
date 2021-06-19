package com.boswelja.smartwatchextensions

import java.util.concurrent.TimeoutException

fun expectTrueWithinTimeout(timeout: Long, assertion: () -> Boolean) {
    val delayBetweenChecks = 50L
    var elapsedTime = 0L
    var wasTrue = assertion()
    while (!wasTrue && elapsedTime < timeout) {
        Thread.sleep(delayBetweenChecks)
        elapsedTime += delayBetweenChecks
        wasTrue = assertion()
    }
    if (!wasTrue)
        throw TimeoutException("Timed out waiting for assertion")
}
