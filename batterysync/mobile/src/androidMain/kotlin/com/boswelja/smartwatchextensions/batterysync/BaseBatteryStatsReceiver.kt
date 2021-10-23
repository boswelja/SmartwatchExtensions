package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [BatteryStats] and update [BatteryStatsDbRepository] with the new
 * data.
 */
abstract class BaseBatteryStatsReceiver :
    MessageReceiver<BatteryStats?>(BatteryStatsSerializer),
    KoinComponent {

    private val batteryStatsRepository: BatteryStatsRepository by inject()

    /**
     * Called after battery stats have been saved into the repository.
     * @param context [Context].
     * @param sourceUid The source UID that sent the battery stats.
     * @param batteryStats The [BatteryStats] object.
     */
    abstract suspend fun onBatteryStatsReceived(
        context: Context,
        sourceUid: String,
        batteryStats: BatteryStats
    )

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats?>
    ) {
        message.data?.let { batteryStats ->
            batteryStatsRepository.updateStatsFor(
                message.sourceUid,
                batteryStats
            )
            onBatteryStatsReceived(context, message.sourceUid, batteryStats)
        }
    }
}
