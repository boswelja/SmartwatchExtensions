/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer

class DeviceAdminChangeReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context?, intent: Intent?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(DEVICE_ADMIN_ENABLED_KEY, true)
                .apply()
        PreferenceSyncLayer(context!!).pushNewData(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(DEVICE_ADMIN_ENABLED_KEY, false)
                .putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)
                .apply()
        PreferenceSyncLayer(context!!).pushNewData(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
    }

    companion object {
        const val DEVICE_ADMIN_ENABLED_KEY = "device_admin_enabled"
    }
}
