package com.boswelja.devicemanager.common

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import java.util.*

object CommonUtils {

    fun getUID(context: Context) : String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        var uuid = sharedPrefs.getString(PreferenceKey.DEVICE_UID, "")!!
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(PreferenceKey.DEVICE_UID, uuid).apply()
        }
        return uuid
    }

    fun getUID(sharedPrefs: SharedPreferences) : String {
        var uuid = sharedPrefs.getString(PreferenceKey.DEVICE_UID, "")!!
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(PreferenceKey.DEVICE_UID, uuid).apply()
        }
        return uuid
    }

    fun updateBatteryStats(context: Context) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val batteryPct = ((batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! / batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()) * 100).toInt()
        val charging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        val message = "$batteryPct|$charging"

        Wearable.getCapabilityClient(context)
                .getCapability(References.CAPABILITY_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener { capabilityInfo ->
                    val nodeId = capabilityInfo.nodes.firstOrNull { it.isNearby }?.id ?: capabilityInfo.nodes.firstOrNull()?.id
                    if (nodeId != null) {
                        val messageClient = Wearable.getMessageClient(context)
                        messageClient.sendMessage(
                                nodeId,
                                References.BATTERY_STATUS_PATH,
                                message.toByteArray(Charsets.UTF_8))
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.d("updateBatteryStats", "success")
                                    } else {
                                        Log.d("updateBatteryStats", "failed")
                                    }
                                }

                        Log.d("updateBatteryStats", message)
                    } else {
                        Log.d("updateBatteryStats", "No available nodes")
                    }
                }

        //val dataClient = Wearable.getDataClient(context)
        //val putDataMapReq = PutDataMapRequest.create(References.BATTERY_STATUS_PATH)
        //putDataMapReq.dataMap.putInt(References.BATTERY_PERCENT_PATH, batteryPct)
        //putDataMapReq.dataMap.putBoolean(References.BATTERY_CHARGING, charging)
        //val putDataReq = putDataMapReq.asPutDataRequest()
        //dataClient.putDataItem(putDataReq)
    }

}