package com.boswelja.devicemanager.phonelocking

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService.Companion.ACCESSIBILITY_SERVICE_ENABLED_KEY

object Utils {

    /**
     * Checks whether Device Administrator mode is enabled.
     * @param context [Context].
     * @return true if Wearable Extensions is a Device Administrator, false otherwise.
     */
    fun isDeviceAdminEnabled(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(DEVICE_ADMIN_ENABLED_KEY, false)

    /**
     * Checks whether Phone Locking accessibility service is enabled.
     * @param context [Context].
     * @return true if Accessibility Service is enabled, false otherwise.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun isAccessibilityServiceEnabled(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)
}
