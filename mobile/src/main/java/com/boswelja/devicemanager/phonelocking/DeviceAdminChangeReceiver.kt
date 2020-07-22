/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import timber.log.Timber

class DeviceAdminChangeReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Timber.i("onEnabled() called")
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(DEVICE_ADMIN_ENABLED_KEY, true)
            .apply()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Timber.i("onDisabled() called")
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(DEVICE_ADMIN_ENABLED_KEY, false)
            .putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)
            .apply()
        // TODO Tell registered watches Phone Locking was disabled.
    }

    companion object {
        const val DEVICE_ADMIN_ENABLED_KEY = "device_admin_enabled"
    }
}
