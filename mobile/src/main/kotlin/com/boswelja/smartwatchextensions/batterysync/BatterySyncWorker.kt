package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.flow.firstOrNull
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class BatterySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : BaseBatterySyncWorker(appContext, workerParams), DIAware {

    override val di: DI by closestDI(applicationContext)

    private val watchManager: WatchManager by instance()

    override suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean {
        return watchManager.getWatchById(targetUid).firstOrNull()?.let { watch ->
            watchManager.sendMessage(watch, Message(BATTERY_STATUS_PATH, batteryStats))
        } ?: false
    }
}
