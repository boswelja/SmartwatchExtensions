package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.batteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.BatterySyncStateRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.serialization.MessageReceiver
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver :
    MessageReceiver<BatteryStats>(BatteryStatsSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()
    private val batteryStatsRepository: BatteryStatsRepository by inject()
    private val batterySyncStateRepository: BatterySyncStateRepository by inject()
    private val batterySyncNotificationHandler: BatterySyncNotificationHandler by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats>
    ) {
        val batterySyncState = batterySyncStateRepository.getBatterySyncState().first()

        if (!batterySyncState.batterySyncEnabled) return

        // Store updated stats
        val batteryStats = message.data
        batteryStatsRepository.updatePhoneBatteryStats(batteryStats)
        sendBatteryStatsUpdate(context, message.sourceUid)
        PhoneBatteryComplicationProvider.updateAll(context)

        batterySyncNotificationHandler.handleNotificationsFor(
            message.sourceUid,
            message.data
        )
    }

    /**
     *  Get an up to date [BatteryStats] and send it to the given target.
     *  @param context [Context].
     *  @param targetUid The target device UID.
     */
    private suspend fun sendBatteryStatsUpdate(context: Context, targetUid: String) {
        val handler = MessageHandler(BatteryStatsSerializer, messageClient)
        val batteryStats = context.batteryStats()
        if (batteryStats != null) {
            handler.sendMessage(
                targetUid,
                Message(BatteryStatus, batteryStats)
            )
        }
    }
}
