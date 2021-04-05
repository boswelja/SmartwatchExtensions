package com.boswelja.devicemanager.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.boswelja.devicemanager.common.batterysync.BatteryStats
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object Utils {

    /**
     * Get battery stats for this device.
     * @param function The function to be called when we've got the battery stats. This may not be
     * called if there's an issue retrieving battery stats. The function is called with battery
     * percent and a boolean to represent whether the device is charging.
     */
    private suspend fun Context.getBatteryStats(function: suspend (Int, Boolean) -> Unit) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(null, iFilter)?.let {
            val batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val batteryScale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = (batteryLevel * 100) / batteryScale
            val charging = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                BatteryManager.BATTERY_STATUS_CHARGING
            function(percent, charging)
        }
    }

    /**
     * Get up to date battery stats for this device and send it to a specified watch, or all watches
     * with battery sync enabled.
     * @param context [Context].
     * @param watch The [Watch] to send the updated stats to, or null if it should be sent to all
     * possible watches.
     */
    suspend fun updateBatteryStats(context: Context, watch: Watch? = null) {
        withContext(Dispatchers.IO) {
            Timber.i("Updating battery stats for ${watch?.id}")
            val batteryStats = BatteryStats.createForDevice(context)
            if (batteryStats != null) {
                val watchManager = WatchManager.getInstance(context)
                if (watch != null) {
                    watchManager.sendMessage(watch, BATTERY_STATUS_PATH, batteryStats.toByteArray())
                } else {
                    watchManager.registeredWatches.value?.forEach {
                        if (watchManager.getPreference<Boolean>(it, BATTERY_SYNC_ENABLED_KEY)
                            == true
                        ) {
                            watchManager.sendMessage(
                                it, BATTERY_STATUS_PATH, batteryStats.toByteArray()
                            )
                        }
                    }
                }
            } else {
                Timber.w("batteryStats null, skipping...")
            }
        }
    }
}
