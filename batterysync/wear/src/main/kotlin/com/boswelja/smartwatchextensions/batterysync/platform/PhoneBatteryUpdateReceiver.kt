package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.batteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SendBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneBatteryStats
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver :
    MessageReceiver<BatteryStats>(BatteryStatsSerializer),
    KoinComponent {

    private val batterySyncNotificationHandler: BatterySyncNotificationHandler by inject()
    private val setPhoneBatteryStats: SetPhoneBatteryStats by inject()
    private val sendBatteryStats: SendBatteryStats by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats>
    ) {
        // Store updated stats
        val batteryStats = message.data
        setPhoneBatteryStats(batteryStats)
        sendBatteryStatsUpdate(context)
        PhoneBatteryComplicationProvider.updateAll(context)

        batterySyncNotificationHandler.handleNotificationsFor(
            message.sourceUid,
            message.data
        )
    }

    /**
     *  Get an up to date [BatteryStats] and send it to the given target.
     *  @param context [Context].
     */
    private suspend fun sendBatteryStatsUpdate(context: Context) {
        context.batteryStats()?.let { sendBatteryStats(it) }
    }
}
