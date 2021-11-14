package com.boswelja.smartwatchextensions.devicemanagement.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.devicemanagement.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.devicemanagement.LAUNCH_APP
import com.boswelja.smartwatchextensions.devicemanagement.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
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
import timber.log.Timber

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
            LAUNCH_APP -> launchApp(context)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(message.sourceUid)
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param context [Context].
     */
    private fun launchApp(context: Context) {
        Timber.i("launchApp() called")
        context.startActivity<MainActivity>(flags = Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Tells the source node whether it is registered with Smartwatch Extensions.
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendIsWatchRegistered(watchId: String) {
        Timber.i("sendIsWatchRegistered() called")
        withContext(Dispatchers.IO) {
            val watch = watchRepository.getWatchById(watchId).firstOrNull()
            // If watch is found in the database, let it know it's registered
            watch?.let {
                messageClient.sendMessage(watch.uid, Message(WATCH_REGISTERED_PATH, null))
            }
        }
    }
}
