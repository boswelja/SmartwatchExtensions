package com.boswelja.devicemanager.tasks

import android.app.job.JobParameters
import android.app.job.JobService
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class BatteryInfoUpdate: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, ifilter)
        val batteryPct = ((batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat())) * 100).toInt()
        Log.d("BatteryInfoUpdate", batteryPct.toString())

        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create("/batteryPercent")
        putDataMapReq.dataMap.putInt("com.boswelja.devicemanager.batterypercent", batteryPct)
        val putDataReq = putDataMapReq.asPutDataRequest()
        Log.d("BatteryInfoUpdate", putDataReq.uri.toString())
        dataClient.putDataItem(putDataReq)
        jobFinished(params, true)
        return false
    }
}