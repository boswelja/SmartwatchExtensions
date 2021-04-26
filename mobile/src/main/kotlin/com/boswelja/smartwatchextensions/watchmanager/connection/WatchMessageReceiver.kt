package com.boswelja.smartwatchextensions.watchmanager.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.batterysync.Utils
import com.boswelja.smartwatchextensions.batterysync.Utils.handleBatteryStats
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LAUNCH_APP
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.watchconnection.core.MessageReceiver
import com.boswelja.watchconnection.core.WatchPlatformManager
import com.boswelja.watchconnection.wearos.WearOSPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

class WatchMessageReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(
        context: Context,
        sourceWatchId: UUID,
        message: String,
        data: ByteArray?
    ) {
        when (message) {
            LAUNCH_APP -> launchApp(context)
            REQUEST_BATTERY_UPDATE_PATH -> Utils.updateBatteryStats(context)
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH ->
                sendInterruptFilterAccess(context, sourceWatchId)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(context, sourceWatchId)
            BATTERY_STATUS_PATH -> data?.let {
                handleBatteryStats(context, sourceWatchId, BatteryStats.fromByteArray(data))
            }
        }
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
     * Tells the source node whether we are allowed to set the state of Do not Disturb.
     * @param context [Context].
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendInterruptFilterAccess(context: Context, watchId: UUID) {
        Timber.i("sendInterruptFilterAccess() called")
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            val watch = database.watchDao().get(watchId)
            watch?.let {
                val hasDnDAccess = Compat.canSetDnD(context)
                WatchPlatformManager(
                    WearOSPlatform(
                        context,
                        WatchManager.CAPABILITY_WATCH_APP,
                        Capability.values().map { it.name }
                    )
                ).sendMessage(
                    watch,
                    REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                    hasDnDAccess.toByteArray()
                )
            }
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
            val watch = database.watchDao().get(watchId)
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
