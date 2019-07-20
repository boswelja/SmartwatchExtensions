/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.Compat

abstract class InterruptFilterChangeReceiver : BroadcastReceiver() {

    abstract fun onInterruptFilterChanged(context: Context, interruptFilterEnabled: Boolean)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
            val interruptionFilterEnabled = Compat.interruptionFilterEnabled(context)
            onInterruptFilterChanged(context, interruptionFilterEnabled)
        }
    }
}
