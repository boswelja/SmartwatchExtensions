package com.boswelja.devicemanager

import android.content.ComponentName
import android.preference.PreferenceManager
import android.support.wearable.complications.ProviderUpdateRequester
import android.util.Log
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.complications.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class BatteryUpdateListener: WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        for (event: DataEvent in dataEvents!!) {
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    Log.d("BatteryUpdateListener", event.dataItem.uri.toString())
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val percent = dataMap.getInt("com.boswelja.devicemanager.batterypercent")
                    Log.d("BatteryUpdateListener", percent.toString())
                    preferenceManager.edit().putInt(Config.BATTERY_PERCENT_KEY, percent).apply()
                }
                DataEvent.TYPE_DELETED -> {
                    preferenceManager.edit().remove(Config.BATTERY_PERCENT_KEY).apply()
                }
            }
        }
        val providerUpdateRequester = ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
        providerUpdateRequester.requestUpdateAll()
    }
}