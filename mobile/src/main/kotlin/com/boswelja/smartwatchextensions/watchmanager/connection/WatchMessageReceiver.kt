package com.boswelja.smartwatchextensions.watchmanager.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_START
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.batterysync.BATTERY_STATUS_PATH
import com.boswelja.smartwatchextensions.batterysync.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.batterysync.Utils
import com.boswelja.smartwatchextensions.common.EmptySerializer
import com.boswelja.smartwatchextensions.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LAUNCH_APP
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.dndsync.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.message.MessageClient
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
            REQUEST_BATTERY_UPDATE_PATH,
            BATTERY_STATUS_PATH,
            DND_STATUS_PATH
        )
    )
) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Nothing?>) {
        when (message.path) {
            APP_SENDING_START -> {
                clearAppsForWatch(context, message.sourceUid)
            }
            LAUNCH_APP -> launchApp(context)
            REQUEST_BATTERY_UPDATE_PATH -> Utils.updateBatteryStats(context)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(context, message.sourceUid)
        }
    }

    private suspend fun clearAppsForWatch(
        context: Context,
        sourceWatchId: String
    ) {
        val database = WatchAppDatabase.getInstance(context)
        database.apps().removeForWatch(sourceWatchId)
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
            val database = WatchDatabase.getInstance(context)
            val watch = database.getById(watchId).firstOrNull()
            // If watch is found in the database, let it know it's registered
            watch?.let {
                MessageClient(
                    platforms = listOf(WearOSMessagePlatform(context))
                ).sendMessage(watch, Message(WATCH_REGISTERED_PATH, null))
            }
        }
    }
}
