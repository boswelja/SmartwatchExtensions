/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.interruptfiltersync.BaseInterruptFilterLocalChangeListener

abstract class BootReceiver : BroadcastReceiver() {

    lateinit var sharedPreferences: SharedPreferences

    abstract fun isInterruptFilterSyncSending(): Boolean

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (isInterruptFilterSyncSending()) {
                val serviceIntent = Intent(context, BaseInterruptFilterLocalChangeListener::class.java)
                Compat.startForegroundService(context!!, serviceIntent)
            }
        }
    }
}
