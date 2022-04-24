package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [BatteryStats] and update [BatteryStatsDbRepository] with the new
 * data.
 */
class BatteryStatsReceiver :
    MessageReceiver<BatteryStats>(BatteryStatsSerializer),
    KoinComponent {

    private val batteryStatsRepository: BatteryStatsRepository by inject()
    private val batterySyncNotificationHandler: BatterySyncNotificationHandler by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats>
    ) {
        batteryStatsRepository.updateStatsFor(
            message.sourceUid,
            message.data
        )
        batterySyncNotificationHandler.handleNotificationsFor(
            message.sourceUid,
            message.data
        )
    }
}
