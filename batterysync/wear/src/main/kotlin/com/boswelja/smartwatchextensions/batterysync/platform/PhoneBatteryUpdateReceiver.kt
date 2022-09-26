package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.batteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SendBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneBatteryStats
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver :
    MessageReceiver(),
    KoinComponent {

    private val batterySyncNotificationHandler: BatterySyncNotificationHandler by inject()
    private val setPhoneBatteryStats: SetPhoneBatteryStats by inject()
    private val sendBatteryStats: SendBatteryStats by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == BatteryStatus) {
            val batteryStats = message.data?.let { BatteryStatsSerializer.deserialize(it) } ?: return
            setPhoneBatteryStats(batteryStats)
            sendBatteryStatsUpdate(context)
            PhoneBatteryComplicationProvider.updateAll(context)

            batterySyncNotificationHandler.handleNotificationsFor(
                message.sourceUid,
                batteryStats
            )
        }
    }

    /**
     *  Get an up to date [BatteryStats] and send it to the given target.
     *  @param context [Context].
     */
    private suspend fun sendBatteryStatsUpdate(context: Context) {
        context.batteryStats()?.let { sendBatteryStats(it) }
    }
}
