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
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("Intent received")
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.d("Handling ${intent.action}")
                CapabilityUpdater(context!!).updateCapabilities()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                if (sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false) ||
                    sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false)
                ) {
                    Intent(context, DnDLocalChangeListener::class.java).also {
                        ContextCompat.startForegroundService(context, it)
                    }
                }
            }
        }
    }
}
