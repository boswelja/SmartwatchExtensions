package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.smartwatchextensions.settingssync.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
object Utils {

    const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"

    /**
     * Get up to date battery stats for this device and send it to a specified watch, or all watches
     * with battery sync enabled.
     * @param context [Context].
     * @param watch The [Watch] to send the updated stats to, or null if it should be sent to all
     * possible watches.
     */
    suspend fun updateBatteryStats(context: Context, watch: Watch? = null) {
        withContext(Dispatchers.IO) {
            Timber.i("Updating battery stats for ${watch?.uid}")
            val batteryStats = context.batteryStats()
            Timber.d(
                "percent = %s, isCharging = %s",
                batteryStats?.percent,
                batteryStats?.charging
            )
            if (batteryStats != null) {
                val watchManager = WatchManager.getInstance(context)
                if (watch != null) {
                    watchManager.sendMessage(watch, BATTERY_STATUS_PATH, batteryStats.encode())
                } else {
                    watchManager.registeredWatches.first()
                        .filter {
                            watchManager.getBoolSetting(
                                BATTERY_SYNC_ENABLED_KEY, it
                            ).first()
                        }.forEach {
                            watchManager.sendMessage(
                                it, BATTERY_STATUS_PATH, batteryStats.encode()
                            )
                        }
                }
            } else {
                Timber.w("batteryStats null, skipping...")
            }
        }
    }



}
