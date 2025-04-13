package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.RequestBatteryStatus
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryStats
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] to receive [RequestBatteryStatus].
 */
class BatteryUpdateRequestReceiver : WearableListenerService(), KoinComponent {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val messageClient: MessageClient by inject()
    private val getPhoneBatteryStats: GetPhoneBatteryStats by inject()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == RequestBatteryStatus) {
            val batteryStats = getPhoneBatteryStats().getOrNull()
            if (batteryStats != null) {
                messageClient.sendMessage(
                    messageEvent.sourceNodeId,
                    BatteryStatus,
                    BatteryStatsSerializer.serialize(batteryStats)
                )
            }
        }
    }
}
