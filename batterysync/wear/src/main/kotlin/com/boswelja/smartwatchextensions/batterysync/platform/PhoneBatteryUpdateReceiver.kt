package com.boswelja.smartwatchextensions.batterysync.platform

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.batteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SendBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneBatteryStats
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver : WearableListenerService() {

    private val batterySyncNotificationHandler: BatterySyncNotificationHandler by inject()
    private val setPhoneBatteryStats: SetPhoneBatteryStats by inject()
    private val sendBatteryStats: SendBatteryStats by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == BatteryStatus) {
            if (message.data.isEmpty()) return
            val batteryStats = BatteryStatsSerializer.deserialize(message.data)
            runBlocking {
                setPhoneBatteryStats(batteryStats)
                sendBatteryStatsUpdate()
                PhoneBatteryComplicationProvider.updateAll(this@PhoneBatteryUpdateReceiver)

                batterySyncNotificationHandler.handleNotificationsFor(
                    message.sourceNodeId,
                    batteryStats
                )
            }
        }
    }

    /**
     *  Get an up to date [BatteryStats] and send it to the given target.
     */
    private suspend fun sendBatteryStatsUpdate() {
        batteryStats()?.let { sendBatteryStats(it) }
    }
}
