/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener

class BootReceiver : BaseBootReceiver() {

    override fun onBootCompleted(context: Context?) {
        if (sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false) ||
                sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false)) {
            Intent(context, DnDLocalChangeListener::class.java).also {
                Compat.startForegroundService(context!!, it)
            }
        }
    }
}
