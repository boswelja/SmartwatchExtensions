package com.boswelja.devicemanager.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * A wrapper for [ScheduledThreadPoolExecutor] that automatically starts/stops with [LifecycleOwner]
 * start/stop. Executor lifecycle is handled automatically to simplify setup.
 * @param period The period between function executions.
 * @param unit The [TimeUnit] the period is measured in The default is [TimeUnit.SECONDS].
 * @param callback The function to call when the timer task is run.
 */
class LifecycleAwareTimer(
    private val period: Long,
    private val unit: TimeUnit = TimeUnit.SECONDS,
    private val callback: () -> Unit
) : DefaultLifecycleObserver {

    private val timer = ScheduledThreadPoolExecutor(1)
    private lateinit var task: ScheduledFuture<*>

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Scheduling new timer with rate $period")
        task = timer.scheduleAtFixedRate(
            callback,
            0,
            period,
            unit
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Stopping timer")
        task.cancel(false)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        timer.shutdownNow()
    }

    fun resetTimer() {
        task.cancel(false)
        task = timer.scheduleAtFixedRate(
            callback,
            0,
            period,
            unit
        )
    }
}
