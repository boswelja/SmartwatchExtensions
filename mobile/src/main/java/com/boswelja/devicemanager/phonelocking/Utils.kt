package com.boswelja.devicemanager.phonelocking

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService.Companion.ACCESSIBILITY_SERVICE_ENABLED_KEY

object Utils {

    fun launchAccessibilitySettings(context: Context) {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).also {
            context.startActivity(it)
        }
    }

    fun requestDeviceAdminPerms(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminChangeReceiver().getWho(context))
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_desc))
        }
        context.startActivity(intent)
    }

    fun isDeviceAdminEnabled(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(DEVICE_ADMIN_ENABLED_KEY, false)

    @RequiresApi(Build.VERSION_CODES.P)
    fun isAccessibilityServiceEnabled(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)

    fun switchToDeviceAdminMode(context: Context) {
        context.packageManager.apply {
            setComponentEnabledSetting(
                    ComponentName(context, PhoneLockingAccessibilityService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            setComponentEnabledSetting(
                    ComponentName(context, DeviceAdminChangeReceiver::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun switchToAccessibilityServiceMode(context: Context) {
        if (isDeviceAdminEnabled(context)) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.removeActiveAdmin(ComponentName(context, DeviceAdminChangeReceiver::class.java))
        }

        context.packageManager.apply {
            setComponentEnabledSetting(
                    ComponentName(context, PhoneLockingAccessibilityService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            setComponentEnabledSetting(
                    ComponentName(context, DeviceAdminChangeReceiver::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }
}