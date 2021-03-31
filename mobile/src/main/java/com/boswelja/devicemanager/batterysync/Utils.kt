package com.boswelja.devicemanager.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

object Utils {

    @Deprecated("Send requests through WatchManager instead")
    fun updateBatteryStats(context: Context, watchId: String?) {
        if (!watchId.isNullOrEmpty()) {
            Timber.i("Updating battery stats")
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, iFilter)
            val batteryPct =
                (
                    (
                        batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! /
                            batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()
                        ) * 100
                    )
                    .toInt()
            val charging =
                batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                    BatteryManager.BATTERY_STATUS_CHARGING
            val message = "$batteryPct|$charging"

            Wearable.getMessageClient(context)
                .sendMessage(watchId, BATTERY_STATUS_PATH, message.toByteArray(Charsets.UTF_8))
        } else {
            Timber.w("target null or empty, can't update battery stats")
        }
    }
}
