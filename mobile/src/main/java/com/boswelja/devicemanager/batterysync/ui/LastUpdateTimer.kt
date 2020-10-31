/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import timber.log.Timber

class LastUpdateTimer(lifecycle: Lifecycle) : DefaultLifecycleObserver {

    private val _totalMinutes = MutableLiveData(0)
    val totalMinutes: LiveData<Int>
        get() = _totalMinutes

    private val executor = ScheduledThreadPoolExecutor(1).apply { removeOnCancelPolicy = true }
    private var scheduledTask: ScheduledFuture<*>? = null
    private val runnable =
        Runnable {
            Timber.i("Incrementing timer")
            _totalMinutes.postValue(_totalMinutes.value!! + 1)
        }

    init {
        lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stopTimer()
    }

    fun resetTimer(timerStart: Int = 0) {
        stopTimer()
        _totalMinutes.postValue(timerStart)
        scheduledTask = executor.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.MINUTES)
        Timber.i("Timer reset, start point = $timerStart")
    }

    fun stopTimer() {
        Timber.i("Stopping timer")
        scheduledTask?.cancel(false)
    }
}
