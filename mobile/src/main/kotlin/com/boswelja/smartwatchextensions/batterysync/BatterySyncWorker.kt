package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.flow.firstOrNull

class BatterySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : BaseBatterySyncWorker(appContext, workerParams) {
    override suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean {
        val watchManager = WatchManager.getInstance(applicationContext)
        return watchManager.getWatchById(targetUid).firstOrNull()?.let { watch ->
            watchManager.sendMessage(watch, Message(BATTERY_STATUS_PATH, batteryStats))
        } ?: false
    }
}
