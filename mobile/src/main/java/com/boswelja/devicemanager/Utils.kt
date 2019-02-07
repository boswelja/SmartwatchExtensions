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

    fun updateWatchPrefs(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dndSyncEnabled = prefs.getBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
        val dndSyncSend = prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, false)
        val dndSyncReceive = prefs.getBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false)
        val phoneBatteryChargedNoti = prefs.getBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, false)
        val lockPhoneEnabled = prefs.getBoolean(PreferenceKey.LOCK_PHONE_ENABLED, false)
        val batterySyncEnabled = prefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)

        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(References.PREFERENCE_CHANGE_PATH)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_ENABLED_PATH, dndSyncEnabled)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_SEND_PATH, dndSyncSend)
        putDataMapReq.dataMap.putBoolean(References.DND_SYNC_RECEIVE_PATH, dndSyncReceive)
        putDataMapReq.dataMap.putBoolean(References.BATTERY_PHONE_FULL_CHARGE_NOTI_PATH, phoneBatteryChargedNoti)
        putDataMapReq.dataMap.putBoolean(References.LOCK_PHONE_ENABLED_PATH, lockPhoneEnabled)
        putDataMapReq.dataMap.putBoolean(References.BATTERY_SYNC_ENABLED_PATH, batterySyncEnabled )
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}