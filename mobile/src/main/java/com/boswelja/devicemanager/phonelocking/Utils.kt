package com.boswelja.devicemanager.phonelocking

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.boswelja.devicemanager.Utils.isDeviceAdminEnabled

object Utils {

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