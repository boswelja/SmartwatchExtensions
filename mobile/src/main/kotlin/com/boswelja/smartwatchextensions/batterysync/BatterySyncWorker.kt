package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [BaseBatterySyncWorker] that sends battery stats to a specified watch.
 */
class BatterySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : BaseBatterySyncWorker(appContext, workerParams), KoinComponent {

    private val watchManager: WatchManager by inject()

    override suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean {
        return watchManager.getWatchById(targetUid).firstOrNull()?.let { watch ->
            watchManager.sendMessage(watch, Message(BATTERY_STATUS_PATH, batteryStats))
        } ?: false
    }
}
