/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.os.Build
import android.preference.PreferenceManager
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.complications.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class BatteryUpdateListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        for (event: DataEvent in dataEvents!!) {
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val percent = dataMap.getInt(References.BATTERY_PERCENT_PATH)
                    val charging = dataMap.getBoolean(References.BATTERY_CHARGING)
                    preferenceManager.edit().putInt(References.BATTERY_PERCENT_KEY, percent).apply()

                    if (preferenceManager.getBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, false) && percent > 90 && charging) {
                        sendChargedNoti()
                    }
                }
                DataEvent.TYPE_DELETED -> {
                    preferenceManager.edit().remove(References.BATTERY_PERCENT_KEY).apply()
                }
            }
        }
        val providerUpdateRequester = ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
        providerUpdateRequester.requestUpdateAll()
    }

    private fun sendChargedNoti() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val noti = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("phone_charged", "Phone Fully Charged", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, "phone_charged")
        } else {
            Notification.Builder(this)
        }
        noti.setSmallIcon(R.drawable.ic_battery_full)
        noti.setContentTitle("Phone fully charged")
        noti.setContentText("Your phone is fully charged")
        notificationManager.notify(123, noti.build())
    }
}