package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
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
    private val messageClient: MessageClient by inject()

    override suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean {
        val handler = MessageHandler(BatteryStatsSerializer, messageClient)
        return watchManager.getWatchById(targetUid).firstOrNull()?.let { watch ->
            handler.sendMessage(watch.uid, Message(BATTERY_STATUS_PATH, batteryStats))
        } ?: false
    }
}
