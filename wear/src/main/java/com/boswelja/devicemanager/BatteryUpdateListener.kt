/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.content.ComponentName
import android.preference.PreferenceManager
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.complications.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class BatteryUpdateListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        for (event: DataEvent in dataEvents!!) {
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val percent = dataMap.getInt("com.boswelja.devicemanager.batterypercent")
                    preferenceManager.edit().putInt(References.BATTERY_PERCENT_KEY, percent).apply()
                }
                DataEvent.TYPE_DELETED -> {
                    preferenceManager.edit().remove(References.BATTERY_PERCENT_KEY).apply()
                }
            }
        }
        val providerUpdateRequester = ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
        providerUpdateRequester.requestUpdateAll()
    }
}