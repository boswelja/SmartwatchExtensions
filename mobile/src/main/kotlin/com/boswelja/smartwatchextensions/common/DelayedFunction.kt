package com.boswelja.smartwatchextensions.common

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * A wrapper for [ScheduledThreadPoolExecutor] that handles delaying execution of a function.
 * @param delay The delay before the function executes.
 * @param unit The [TimeUnit] the period is measured in The default is [TimeUnit.SECONDS].
 * @param function The function to call when the timer task is run.
 */
class DelayedFunction(
    private val delay: Long,
    private val unit: TimeUnit = TimeUnit.SECONDS,
    private val function: () -> Unit
) {

    private val timer = ScheduledThreadPoolExecutor(1)
    private var task: ScheduledFuture<*>

    init {
        task = timer.schedule(
            function,
            delay,
            unit
        )
    }

    fun reset() {
        task.cancel(false)
        task = timer.schedule(
            function,
            delay,
            unit
        )
    }
}
