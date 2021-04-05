/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.bootorupdate

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.bootorupdate.updater.Result
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageHandler
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BootOrUpdateHandlerService : Service() {

    private var isUpdating = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForBootOrUpdate(
                this, getSystemService(NotificationManager::class.java)
            )
        when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                performUpdates()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.i("Device restarted")
                startForeground(NOTI_ID, createBootNotification())
                restartServices()
            }
            else -> return super.onStartCommand(intent, flags, startId)
        }
        return START_NOT_STICKY
    }

    /** Initialise an [Updater] instance and perform updates. */
    private fun performUpdates() {
        if (!isUpdating) {
            Timber.i("Starting update process")
            isUpdating = true
            val updater = Updater(this)
            startForeground(NOTI_ID, createUpdaterNotification())
            when (updater.doUpdate()) {
                Result.COMPLETED -> Timber.i("Updated app and changes were made")
                Result.NOT_NEEDED -> Timber.i("Update not needed")
            }
            restartServices()
            notifyUpdateComplete()
        }
    }

    /**
     * Create a Common [NotificationCompat.Builder] with all common settings set.
     * @return The [NotificationCompat.Builder] to build notifications from.
     */
    private fun createBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, BOOT_OR_UPDATE_NOTI_CHANNEL_ID)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setProgress(0, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
    }

    /**
     * Create a [Notification] for the Update Handler service.
     * @return The [Notification] ready to send.
     */
    private fun createUpdaterNotification(): Notification {
        return createBaseNotification()
            .setContentTitle(getString(R.string.notification_update_handler_title))
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
    }

    /**
     * Create a [Notification] for the Boot Handler service.
     * @return The [Notification] ready to send.
     */
    private fun createBootNotification(): Notification {
        return createBaseNotification()
            .setContentTitle(getString(R.string.notification_boot_handler_title))
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
    }

    /** Try to start Do not Disturb change listener service if needed. */
    private suspend fun tryStartInterruptFilterSyncService(database: WatchDatabase) {
        withContext(Dispatchers.IO) {
            val dndSyncToWatchEnabled =
                database.boolPrefDao().getAllForKey(PreferenceKey.DND_SYNC_TO_WATCH_KEY).any {
                    it.value
                }
            Timber.i(
                "tryStartInterruptFilterSyncService dndSyncToWatchEnabled = $dndSyncToWatchEnabled"
            )
            if (dndSyncToWatchEnabled) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, DnDLocalChangeService::class.java)
                )
            }
        }
    }

    /** Try to start any needed [BatterySyncWorker] instances. */
    private suspend fun tryStartBatterySyncWorkers(database: WatchDatabase) {
        val watchBatterySyncInfo =
            database.boolPrefDao().getAllForKey(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
        if (watchBatterySyncInfo.isNotEmpty()) {
            for (batterySyncBoolPreference in watchBatterySyncInfo) {
                if (batterySyncBoolPreference.value) {
                    Timber.i("tryStartBatterySyncWorkers Starting a Battery Sync Worker")
                    BatterySyncWorker.startWorker(
                        applicationContext, batterySyncBoolPreference.watchId
                    )
                }
            }
        } else {
            Timber.w("tryStartBatterySyncWorkers watchBatterySyncInfo possibly null")
        }
    }

    /** Clean up and stop the service. */
    private fun finish() {
        Timber.i("Finished")
        stopForeground(true)
        stopSelf()
    }

    /** Binds to the [WatchManager]. */
    private fun restartServices() {
        Timber.d("restartServices() called")
        MainScope().launch(Dispatchers.IO) {
            WatchDatabase.getInstance(this@BootOrUpdateHandlerService).also {
                tryStartBatterySyncWorkers(it)
                tryStartInterruptFilterSyncService(it)
                finish()
            }
        }
    }

    /**
     * Sends a message to the user notifying them the update has been completed.
     */
    private fun notifyUpdateComplete() {
        val message = Message(
            Message.Icon.UPDATE,
            getString(R.string.update_completed_title),
            getString(R.string.update_complete_text, BuildConfig.VERSION_NAME),
            Message.Action.LAUNCH_CHANGELOG
        )
        MessageHandler.postMessage(this, message)
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}
