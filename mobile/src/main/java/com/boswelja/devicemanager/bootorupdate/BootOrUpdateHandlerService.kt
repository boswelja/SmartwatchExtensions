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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.bootorupdate.updater.Result
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.ui.main.MainActivity.Companion.SHOW_CHANGELOG_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class BootOrUpdateHandlerService : Service() {

    private var isUpdating = false

    private val watchManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            Timber.i("Service bound")
            MainScope().launch(Dispatchers.IO) {
                tryStartBatterySyncWorkers(watchManager)
                tryStartInterruptFilterSyncService(watchManager)
                finish()
            }
        }

        override fun onWatchManagerUnbound() {
            Timber.w("Service unbound")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForBootOrUpdate(
                    this, getSystemService(NotificationManager::class.java))
        when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                performUpdates()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.i("Device restarted")
                startForeground(NOTI_ID, createBootNotification())
                bindWatchmanager()
            }
            else -> return super.onStartCommand(intent, flags, startId)
        }
        return START_NOT_STICKY
    }

    /**
     * Initialise an [Updater] instance and perform updates.
     */
    private fun performUpdates() {
        if (!isUpdating) {
            Timber.i("Starting update process")
            isUpdating = true
            val updater = Updater(this)
            startForeground(NOTI_ID, createUpdaterNotification())
            when (updater.doUpdate()) {
                Result.COMPLETED -> {
                    Timber.i("Update completed")
                    PreferenceManager.getDefaultSharedPreferences(this).edit {
                        putBoolean(SHOW_CHANGELOG_KEY, true)
                    }
                }
                Result.NOT_NEEDED -> Timber.i("Update not needed")
            }
            bindWatchmanager()
        }
    }

    /**
     * Create a Common [NotificationCompat.Builder] with all common settings set.
     * @return The [NotificationCompat.Builder] to build notifications from.
     */
    private fun createBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, BOOT_OR_UPDATE_NOTI_CHANNEL_ID).apply {
            setOngoing(true)
            setShowWhen(false)
            setUsesChronometer(false)
            setProgress(0, 0, true)
            priority = NotificationCompat.PRIORITY_LOW
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        }
    }

    /**
     * Create a [Notification] for the Update Handler service.
     * @return The [Notification] ready to send.
     */
    private fun createUpdaterNotification(): Notification {
        return createBaseNotification().apply {
            setContentTitle(getString(R.string.notification_update_handler_title))
            setSmallIcon(R.drawable.noti_ic_update)
        }.build()
    }

    /**
     * Create a [Notification] for the Boot Handler service.
     * @return The [Notification] ready to send.
     */
    private fun createBootNotification(): Notification {
        return createBaseNotification().apply {
            setContentTitle(getString(R.string.notification_boot_handler_title))
            setSmallIcon(R.drawable.noti_ic_update)
        }.build()
    }

    /**
     * Try to start Do not Disturb change listener service if needed.
     * @param service The [WatchManager] to read preferences from.
     */
    private suspend fun tryStartInterruptFilterSyncService(service: WatchManager) {
        val dndSyncToWatchEnabled =
                service.getBoolPrefsForWatches(PreferenceKey.DND_SYNC_TO_WATCH_KEY)
                        ?.any { it.value } == true
        Timber.i("tryStartInterruptFilterSyncService dndSyncToWatchEnabled = $dndSyncToWatchEnabled")
        if (dndSyncToWatchEnabled) {
            Compat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, DnDLocalChangeService::class.java))
        }
    }

    /**
     * Try to start any needed [BatterySyncWorker] instances.
     * @param service The [WatchManager] to read preferences from.
     */
    private suspend fun tryStartBatterySyncWorkers(service: WatchManager) {
        val watchBatterySyncInfo =
                service.getBoolPrefsForWatches(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
        if (watchBatterySyncInfo != null && watchBatterySyncInfo.isNotEmpty()) {
            for (batterySyncBoolPreference in watchBatterySyncInfo) {
                if (batterySyncBoolPreference.value) {
                    Timber.i("tryStartBatterySyncWorkers Starting a Battery Sync Worker")
                    val batterySyncInterval =
                            service.getIntPrefForWatch(batterySyncBoolPreference.watchId,
                                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)?.value?.toLong() ?: 15
                    val batterySyncWorkerId = BatterySyncWorker.startWorker(
                            applicationContext, batterySyncBoolPreference.watchId, batterySyncInterval)
                    service.updateBatterySyncWorkerId(batterySyncBoolPreference.watchId, batterySyncWorkerId)
                }
            }
        } else {
            Timber.w("tryStartBatterySyncWorkers watchBatterySyncInfo possibly null")
        }
    }

    /**
     * Clean up and stop the service.
     */
    private fun finish() {
        Timber.i("Finished")
        unbindService(watchManagerConnection)
        stopForeground(true)
        stopSelf()
    }

    /**
     * Binds to the [WatchManager].
     */
    private fun bindWatchmanager() {
        Timber.d("bindWatchManager() called")
        WatchManager.bind(this, watchManagerConnection)
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}
