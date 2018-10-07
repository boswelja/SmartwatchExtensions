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
import android.content.IntentFilter
import android.os.BatteryManager
import android.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object Utils {

    fun requestDeviceAdminPerms(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminReceiver().getWho(context))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_desc))
        context.startActivity(intent)
    }

    fun updateBatteryStats(context: Context) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val batteryPct = ((batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! / batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()) * 100).toInt()
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create("/batteryStatus")
        putDataMapReq.dataMap.putInt("com.boswelja.devicemanager.batterypercent", batteryPct)
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }

    fun updateWatchPrefs(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dndSyncEnabled = prefs.getBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
        val dndSyncSend = prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, false)
        val dndSyncReceive = prefs.getBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false)
        val lockPhoneEnabled = prefs.getBoolean(PreferenceKey.LOCK_PHONE_ENABLED, false)

        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create("/preferenceChange")
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_ENABLED_PATH, dndSyncEnabled)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_SEND_PATH, dndSyncSend)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_RECEIVE_PATH, dndSyncReceive)
        putDataMapReq.dataMap.putBoolean(References.LOCK_PHONE_ENABLED_PATH, lockPhoneEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}