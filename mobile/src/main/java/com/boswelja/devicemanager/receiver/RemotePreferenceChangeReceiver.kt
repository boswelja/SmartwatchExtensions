/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Intent
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener

class RemotePreferenceChangeReceiver : BasePreferenceChangeReceiver() {

    override fun handleStartServices(
        interruptFilterSyncToPhone: Boolean,
        interruptFilterSyncToWatch: Boolean,
        interruptFilterSyncWithTheater: Boolean,
        batterySyncEnabled: Boolean
    ) {
        if (interruptFilterSyncToWatch) {
            val intent = Intent(this, InterruptFilterLocalChangeListener::class.java)
            Compat.startForegroundService(this, intent)
        }
        if (batterySyncEnabled) {
            Utils.createBatterySyncJob(this)
        } else {
            Utils.stopBatterySyncJob(this)
        }
    }
}
