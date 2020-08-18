/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phoneconnectionmanager

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class PreferenceChangeReceiver : WearableListenerService() {

  private var sharedPreferences: SharedPreferences? = null

  override fun onDataChanged(dataEvents: DataEventBuffer?) {
    if (dataEvents != null) {
      val currentNodeId = Tasks.await(Wearable.getNodeClient(this).localNode).id
      for (event in dataEvents) {
        if (event.dataItem.uri.toString().endsWith(currentNodeId)) {
          handlePreferenceChange(event)
        }
      }
    }
  }

  private fun handlePreferenceChange(event: DataEvent) {
    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
    if (!dataMap.isEmpty) {
      if (sharedPreferences == null)
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
      sharedPreferences!!.edit {
        for (key in dataMap.keySet()) {
          when (key) {
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
            PreferenceKey.DND_SYNC_TO_WATCH_KEY,
            PreferenceKey.DND_SYNC_TO_PHONE_KEY,
            PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
              val newValue = dataMap.getBoolean(key)
              putBoolean(key, newValue)
              onPreferenceChanged(key, newValue)
            }
            PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
              val newValue = dataMap.getInt(key)
              putInt(key, newValue)
              onPreferenceChanged(key, newValue)
            }
          }
        }
      }
    }
  }

  private fun onPreferenceChanged(key: String, newValue: Any) {
    when (key) {
      PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
        if (newValue == false) {
          sharedPreferences!!.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
          ProviderUpdateRequester(
                  this,
                  ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
              .requestUpdateAll()
        }
      }
      PreferenceKey.DND_SYNC_TO_PHONE_KEY, PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
        if (newValue == true) {
          Intent(this, DnDLocalChangeListener::class.java).also {
            Compat.startForegroundService(this, it)
          }
        }
      }
    }
  }
}
