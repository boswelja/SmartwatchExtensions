package com.boswelja.smartwatchextensions.watchmanager.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_START
import com.boswelja.smartwatchextensions.batterysync.BATTERY_STATUS_PATH
import com.boswelja.smartwatchextensions.common.EmptySerializer
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.devicemanagement.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.devicemanagement.LAUNCH_APP
import com.boswelja.smartwatchextensions.devicemanagement.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.dndsync.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.WatchDbRepository
import com.boswelja.smartwatchextensions.watchmanager.database.RegisteredWatchDatabaseLoader
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform
import com.boswelja.watchconnection.wearos.message.WearOSMessagePlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchMessageReceiver : MessageReceiver<Nothing?>(
    EmptySerializer(
        messagePaths = setOf(
            APP_SENDING_START,
            APP_SENDING_COMPLETE,
            BATTERY_STATUS_PATH,
            DND_STATUS_PATH
        )
    )
) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Nothing?>) {
        when (message.path) {
            LAUNCH_APP -> launchApp(context)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(context, message.sourceUid)
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
     * @param context [Context].
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendIsWatchRegistered(context: Context, watchId: String) {
        Timber.i("sendIsWatchRegistered() called")
        withContext(Dispatchers.IO) {
            val repository = WatchDbRepository(
                DiscoveryClient(
                    listOf(
                        WearOSDiscoveryPlatform(context)
                    )
                ),
                RegisteredWatchDatabaseLoader(context).createDatabase()
            )
            val watch = repository.getWatchById(watchId).firstOrNull()
            // If watch is found in the database, let it know it's registered
            watch?.let {
                MessageClient(
                    platforms = listOf(WearOSMessagePlatform(context))
                ).sendMessage(watch, Message(WATCH_REGISTERED_PATH, null))
            }
        }
    }
}
