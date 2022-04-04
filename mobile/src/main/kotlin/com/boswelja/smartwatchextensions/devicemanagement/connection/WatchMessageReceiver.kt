package com.boswelja.smartwatchextensions.devicemanagement.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.devicemanagement.CheckWatchRegistered
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestLaunchApp
import com.boswelja.smartwatchextensions.core.devicemanagement.ConfirmWatchRegistered
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to handle messages received with no data.
 */
class WatchMessageReceiver :
    MessageReceiver(),
    KoinComponent {

    private val watchRepository: WatchRepository by inject()
    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        when (message.path) {
            RequestLaunchApp -> launchApp(context)
            CheckWatchRegistered -> sendIsWatchRegistered(message.sourceUid)
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param context [Context].
     */
    private fun launchApp(context: Context) {
        context.startActivity<MainActivity>(flags = Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Tells the source node whether it is registered with Smartwatch Extensions.
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendIsWatchRegistered(watchId: String) {
        withContext(Dispatchers.IO) {
            val watch = watchRepository.getWatchById(watchId).firstOrNull()
            // If watch is found in the database, let it know it's registered
            watch?.let {
                messageClient.sendMessage(watch.uid, Message(ConfirmWatchRegistered, null))
            }
        }
    }
}
