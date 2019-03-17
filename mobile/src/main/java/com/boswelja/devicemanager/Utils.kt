/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.ui.MainActivity
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object Utils {

    fun requestDeviceAdminPerms(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminReceiver().getWho(context))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_desc))
        context.startActivity(intent)
    }

    fun updateWatchPrefs(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dndSyncSend = prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, false)
        val dndSyncReceive = prefs.getBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false)
        val phoneBatteryChargedNoti = prefs.getBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, false)
        val lockPhoneEnabled = prefs.getBoolean(PreferenceKey.LOCK_PHONE_ENABLED, false)
        val batterySyncEnabled = prefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)

        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(References.PREFERENCE_CHANGE_PATH)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_SEND_KEY, dndSyncSend)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_RECEIVE_KEY, dndSyncReceive)
        putDataMapReq.dataMap.putBoolean(References.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
        putDataMapReq.dataMap.putBoolean(References.LOCK_PHONE_ENABLED_KEY, lockPhoneEnabled)
        putDataMapReq.dataMap.putBoolean(References.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(intent)
    }

    fun switchDayNightMode(activity: MainActivity) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val currentNightMode = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d("switchDayNightMode", "Night mode off, switching on")
                prefs.edit().putInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_YES).apply()
            }
            else -> {
                prefs.edit().putInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_NO).apply()
            }
        }
        activity.recreate()
    }
}