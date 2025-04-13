package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.StoreBatteryStatsForWatch
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] to receive [BatteryStats] and store it.
 */
class BatteryStatsReceiver :
    WearableListenerService(),
    KoinComponent {

    private val storeBatteryStatsForWatch: StoreBatteryStatsForWatch by inject()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == BatteryStatus) {
            val batteryStats = BatteryStatsSerializer.deserialize(messageEvent.data)
            runBlocking {
                storeBatteryStatsForWatch(messageEvent.sourceNodeId, batteryStats)
            }
        }
    }
}
