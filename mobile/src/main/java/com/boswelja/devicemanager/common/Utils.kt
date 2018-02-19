package com.boswelja.devicemanager.common

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.boswelja.devicemanager.R
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
        val batteryPct = ((batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat())) * 100).toInt()
        Log.d("BatteryInfoUpdate", batteryPct.toString())

        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create("/batteryPercent")
        putDataMapReq.dataMap.putInt("com.boswelja.devicemanager.batterypercent", batteryPct)
        val putDataReq = putDataMapReq.asPutDataRequest()
        Log.d("BatteryInfoUpdate", putDataReq.uri.toString())
        dataClient.putDataItem(putDataReq)
    }

}