package com.boswelja.smartwatchextensions.watchmanager.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.batterysync.Utils
import com.boswelja.smartwatchextensions.batterysync.Utils.handleBatteryStats
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages.ALL_APPS
import com.boswelja.smartwatchextensions.common.appmanager.Messages.START_SENDING_APPS
import com.boswelja.smartwatchextensions.common.appmanager.decompress
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LAUNCH_APP
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.boswelja.smartwatchextensions.dndsync.Utils.handleDnDStateChange
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.watchconnection.core.MessageReceiver
import com.boswelja.watchconnection.core.WatchPlatformManager
import com.boswelja.watchconnection.wearos.WearOSPlatform
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber

@ExperimentalCoroutinesApi
class WatchMessageReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(
        context: Context,
        sourceWatchId: UUID,
        message: String,
        data: ByteArray?
    ) {
        Timber.d("Received %s", message)
        when (message) {
            START_SENDING_APPS -> {
                clearAppsForWatch(context, sourceWatchId)
            }
            ALL_APPS -> {
                data?.let {
                    Timber.d("Received %s bytes", data.size)
                    val decompressedBytes = data.decompress()
                    val app = App.fromByteArray(decompressedBytes)
                    storeWatchApp(context, sourceWatchId, app)
                }
            }
            LAUNCH_APP -> launchApp(context)
            REQUEST_BATTERY_UPDATE_PATH -> Utils.updateBatteryStats(context)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(context, sourceWatchId)
            BATTERY_STATUS_PATH -> data?.let {
                handleBatteryStats(context, sourceWatchId, BatteryStats.fromByteArray(data))
            }
            DND_STATUS_PATH -> data?.let {
                handleDnDStateChange(context, sourceWatchId, Boolean.fromByteArray(data))
            }
        }
        Timber.d("Finished handling message")
    }

    private suspend fun clearAppsForWatch(
        context: Context,
        sourceWatchId: UUID
    ) {
        val database = WatchAppDatabase.getInstance(context)
        database.apps().removeForWatch(sourceWatchId)
    }

    private suspend fun storeWatchApp(
        context: Context,
        sourceWatchId: UUID,
        app: App
    ) {
        val database = WatchAppDatabase.getInstance(context)
        database.apps().add(
            com.boswelja.smartwatchextensions.appmanager.App(
                sourceWatchId,
                app
            )
        )
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param context [Context].
     */
    private fun launchApp(context: Context) {
        Timber.i("launchApp() called")
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            context.startActivity(it)
        }
    }

    /**
     * Tells the source node whether it is registered with Smartwatch Extensions.
     * @param context [Context].
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendIsWatchRegistered(context: Context, watchId: UUID) {
        Timber.i("sendIsWatchRegistered() called")
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            val watch = database.watchDao().get(watchId).firstOrNull()
            // If watch is found in the database, let it know it's registered
            watch?.let {
                WatchPlatformManager(
                    WearOSPlatform(
                        context,
                        WatchManager.CAPABILITY_WATCH_APP,
                        Capability.values().map { it.name }
                    )
                ).sendMessage(watch, WATCH_REGISTERED_PATH)
            }
        }
    }
}
