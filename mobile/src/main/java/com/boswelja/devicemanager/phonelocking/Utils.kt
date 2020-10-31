/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService.Companion.ACCESSIBILITY_SERVICE_ENABLED_KEY
import timber.log.Timber

object Utils {

    /**
     * Launches the device's Accessibility Settings.
     * @param context [Context].
     */
    fun launchAccessibilitySettings(context: Context) {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).also { context.startActivity(it) }
    }

    /**
     * Request Device Administrator permissions for Wearable Extensions.
     * @param context [Context].
     */
    fun requestDeviceAdminPerms(context: Context) {
        val intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    DeviceAdminChangeReceiver().getWho(context)
                )
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.device_admin_desc)
                )
            }
        context.startActivity(intent)
    }

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

    /**
     * Switch Phone Locking mode to Device Administrator. This disables Accessibility Service
     * components.
     * @param context [Context].
     */
    fun switchToDeviceAdminMode(context: Context) {
        Timber.i("Switching to Device Administrator mode")
        context.packageManager.apply {
            setComponentEnabledSetting(
                ComponentName(context, PhoneLockingAccessibilityService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            setComponentEnabledSetting(
                ComponentName(context, DeviceAdminChangeReceiver::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    /**
     * Switch Phone Locking mode to Accessibility Service. This disables Device Administrator
     * components.
     * @param context [Context].
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun switchToAccessibilityServiceMode(context: Context) {
        Timber.i("Switching to Accessibility Service mode")
        if (isDeviceAdminEnabled(context)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putBoolean(DEVICE_ADMIN_ENABLED_KEY, false)
            }
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.removeActiveAdmin(ComponentName(context, DeviceAdminChangeReceiver::class.java))
        }

        context.packageManager.apply {
            setComponentEnabledSetting(
                ComponentName(context, PhoneLockingAccessibilityService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            setComponentEnabledSetting(
                ComponentName(context, DeviceAdminChangeReceiver::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
