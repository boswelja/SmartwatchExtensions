/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import com.boswelja.devicemanager.common.Compat
import timber.log.Timber

abstract class DnDLocalChangeReceiver : BroadcastReceiver() {

    /**
     * Called when the state of Do not Disturb (or ringer mode state on pre-M devices) is changed.
     * @param dndEnabled The new state of Do not Disturb.
     */
    abstract fun onDnDChanged(dndEnabled: Boolean)

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("Received broadcast")
        when (intent?.action) {
            NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED,
            AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                Timber.i("DnD mode changed")
                onDnDChanged(Compat.isDndEnabled(context!!))
            }
        }
    }

    fun register(context: Context) {
        IntentFilter()
            .apply {
                addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            }
            .also {
                Timber.i("Registering a receiver")
                context.registerReceiver(this, it)
            }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}
