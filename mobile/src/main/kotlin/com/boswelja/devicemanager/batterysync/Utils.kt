package com.boswelja.devicemanager.batterysync

import android.content.Context
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
     * Get up to date battery stats for this device and send it to a specified watch, or all watches
     * with battery sync enabled.
     * @param context [Context].
     * @param watch The [Watch] to send the updated stats to, or null if it should be sent to all
     * possible watches.
     */
    suspend fun updateBatteryStats(context: Context, watch: Watch? = null) {
        withContext(Dispatchers.getIO()) {
            Timber.i("Updating battery stats for ${watch?.id}")
            val batteryStats = BatteryStats.createForDevice(context)
            if (batteryStats != null) {
                val watchManager = WatchManager.getInstance(context)
                if (watch != null) {
                    watchManager.sendMessage(watch, BATTERY_STATUS_PATH, batteryStats.toByteArray())
                } else {
                    watchManager.registeredWatches.value?.forEach {
                        if (watchManager.getPreference<Boolean>(it.id, BATTERY_SYNC_ENABLED_KEY)
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
