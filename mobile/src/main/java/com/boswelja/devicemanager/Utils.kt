/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.util.TypedValue
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY

object Utils {

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

    /**
     * Set the system's current Interruption Filter state, or set silent mode if
     * Interruption Filter doesn't exist.
     * @param interruptionFilterOn Specify the new Interruption Filter state.
     */
    fun setInterruptionFilter(context: Context, interruptionFilterOn: Boolean) {
        if (interruptionFilterOn != Compat.interruptionFilterEnabled(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (interruptionFilterOn) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    } else {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    prefs.edit().putBoolean(PreferenceKey.DND_SYNC_TO_PHONE_KEY, false).apply()
                }
            } else {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (interruptionFilterOn) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
        }
    }

    fun getAppIcon(context: Context, packageName: String, fallbackIcon: Drawable? = null): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (ignored: Exception) {
            fallbackIcon ?: context.getDrawable(R.drawable.ic_app_icon_unknown)
        }
    }

    fun complexTypeDp(resources: Resources, dp: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
