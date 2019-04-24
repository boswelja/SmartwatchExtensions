package com.boswelja.devicemanager.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey

class DeviceAdminChangeReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context?, intent: Intent?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(DEVICE_ADMIN_ENABLED_KEY, true)
                .apply()
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(DEVICE_ADMIN_ENABLED_KEY, false)
                .putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)
                .apply()
    }

    companion object {
        const val DEVICE_ADMIN_ENABLED_KEY = "device_admin_enabled"
    }
}