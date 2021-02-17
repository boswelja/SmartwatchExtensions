/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phoneconnectionmanager

import android.content.Context
import android.content.SharedPreferences
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncHelper(
    context: Context,
    private val sharedPreferences: SharedPreferences,
    private val phoneId: String
) {

    private val dataClient = Wearable.getDataClient(context)

    fun pushNewData(key: String): Task<DataItem>? {
        if (phoneId.isNotEmpty()) {
            val syncedPrefUpdateReq = PutDataMapRequest.create(PREFERENCE_CHANGE_PATH)
            when (key) {
                in SyncPreferences.BOOL_PREFS -> {
                    val newValue = sharedPreferences.getBoolean(key, false)
                    syncedPrefUpdateReq.dataMap.putBoolean(key, newValue)
                }
                in SyncPreferences.INT_PREFS -> {
                    val newValue = sharedPreferences.getInt(key, 90)
                    syncedPrefUpdateReq.dataMap.putInt(key, newValue)
                }
            }
            if (!syncedPrefUpdateReq.dataMap.isEmpty) {
                syncedPrefUpdateReq.setUrgent()
                return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
            }
        }
        return null
    }

    companion object {
        private const val PREFERENCE_CHANGE_PATH = "/preference_change"
    }
}
