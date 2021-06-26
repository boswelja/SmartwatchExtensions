package com.boswelja.smartwatchextensions.bootorupdate

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.bootorupdate.updater.Updater
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeService
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.sendMessage
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BootOrUpdateHandlerService : LifecycleService() {

    private var isUpdating = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand called")
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
                lifecycleScope.launch { restartServices() }
            }
            else -> return super.onStartCommand(intent, flags, startId)
        }
        return START_NOT_STICKY
    }

    /** Initialise an [Updater] instance and perform updates. */
    private fun performUpdates() {
        if (!isUpdating) {
            isUpdating = true
            Timber.i("Starting update process")
            startForeground(NOTI_ID, createUpdaterNotification())
            lifecycleScope.launch(Dispatchers.IO) {
                if (appStateStore.data.map { it.lastAppVersion }.first() <= 0) {
                    Timber.d("Updating 'lastAppVersion'")
                    appStateStore.updateData {
                        it.copy(lastAppVersion = 1)
                    }
                }
                val updater = Updater(this@BootOrUpdateHandlerService)
                if (updater.migrate()) {
                    // Update completed, notify user
                    notifyUpdateComplete()
                } else {
                    Timber.w("Failed to update")
                }
            }
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
    private suspend fun tryStartInterruptFilterSyncService(database: WatchSettingsDatabase) {
        withContext(Dispatchers.IO) {
            val dndSyncToWatchEnabled =
                database.boolSettings().getByKey(PreferenceKey.DND_SYNC_TO_WATCH_KEY).first().any {
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

    private suspend fun tryStartSeparationObserverService(database: WatchSettingsDatabase) {
        val watchSeparationAlertsEnabled =
            database.boolSettings().getByKey(WATCH_SEPARATION_NOTI_KEY).first().any { it.value }
        if (watchSeparationAlertsEnabled) {
            SeparationObserverService.start(this)
        }
    }

    /** Try to start any needed [BatterySyncWorker] instances. */
    private suspend fun tryStartBatterySyncWorkers(database: WatchSettingsDatabase) {
        withContext(Dispatchers.IO) {
            val watchBatterySyncInfo =
                database.boolSettings().getByKey(PreferenceKey.BATTERY_SYNC_ENABLED_KEY).first()
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
    }

    /** Clean up and stop the service. */
    private fun finish() {
        Timber.i("Finished")
        stopForeground(true)
        stopSelf()
    }

    /** Binds to the [WatchManager]. */
    private suspend fun restartServices() {
        Timber.d("restartServices() called")
        WatchSettingsDatabase.getInstance(this).also {
            tryStartBatterySyncWorkers(it)
            tryStartInterruptFilterSyncService(it)
            tryStartSeparationObserverService(it)
            finish()
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
        lifecycleScope.launch {
            sendMessage(message)
        }
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}
