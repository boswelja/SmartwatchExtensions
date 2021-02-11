/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener

class BootReceiver : BroadcastReceiver() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            onBootCompleted(context)
        }
    }

    private fun onBootCompleted(context: Context?) {
        CapabilityUpdater(context!!).updateCapabilities()
        if (sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false) ||
            sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false)
        ) {
            Intent(context, DnDLocalChangeListener::class.java).also {
                Compat.startForegroundService(context, it)
            }
        }
    }
}
