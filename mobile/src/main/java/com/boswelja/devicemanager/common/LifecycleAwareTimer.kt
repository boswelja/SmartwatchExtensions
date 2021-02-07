package com.boswelja.devicemanager.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * A wrapper for [Timer] that automatically starts/stops with [LifecycleOwner] start/stop.
 * Timer lifecycle is handled automatically to simplify setup.
 * @param period The period between function executions.
 * @param unit The [TimeUnit] the period is measured in The default is [TimeUnit.SECONDS].
 * @param callback The function to call when the timer task is run.
 */
class LifecycleAwareTimer(
    period: Long,
    unit: TimeUnit = TimeUnit.SECONDS,
    callback: () -> Unit
) : DefaultLifecycleObserver {

    private val periodMs = TimeUnit.MILLISECONDS.convert(period, unit)
    private lateinit var timer: Timer
    private val task = object : TimerTask() {
        override fun run() {
            callback()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Scheduling new timer with rate $periodMs")
        timer = Timer()
        timer.scheduleAtFixedRate(task, 0, periodMs)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Stopping timer")
        timer.cancel()
    }
}
