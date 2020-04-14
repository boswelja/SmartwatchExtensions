package com.boswelja.devicemanager.bootorupdate

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.ui.main.MainActivity.Companion.SHOW_CHANGELOG_KEY
import com.boswelja.devicemanager.bootorupdate.updater.Result
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootOrUpdateHandlerService : Service() {

    private lateinit var updater: Updater

    private val watchManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            MainScope().launch(Dispatchers.IO) {
                tryStartBatterySyncWorkers(service)
                tryStartInterruptFilterSyncService(service)
                finish()
            }
        }

        override fun onWatchManagerUnbound() {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updater = Updater(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) updater.createNotificationChannels()
                startForeground(NOTI_ID, createUpdaterNotification())
                if (updater.doUpdate() == Result.COMPLETED) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit {
                        putBoolean(SHOW_CHANGELOG_KEY, true)
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                startForeground(NOTI_ID, createBootNotification())
            }
            else -> return super.onStartCommand(intent, flags, startId)
        }
        WatchConnectionService.bind(this, watchManagerConnection)
        return START_NOT_STICKY
    }

    private fun createBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, BOOT_OR_UPDATE_NOTI_CHANNEL_ID).apply {
            setOngoing(true)
            setShowWhen(false)
            setUsesChronometer(false)
            priority = NotificationCompat.PRIORITY_LOW
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        }
    }

    private fun createUpdaterNotification(): Notification {
        return createBaseNotification().apply {
            setContentTitle(getString(R.string.notification_update_handler_title))
            setSmallIcon(R.drawable.noti_ic_update)
        }.build()
    }

    private fun createBootNotification(): Notification {
        return createBaseNotification().apply {
            setContentTitle(getString(R.string.notification_boot_handler_title))
            setSmallIcon(R.drawable.noti_ic_update)
        }.build()
    }

    private suspend fun tryStartInterruptFilterSyncService(service: WatchConnectionService) {
        val dndSyncToWatchEnabled =
                service.getBoolPrefsForRegisteredWatches(PreferenceKey.DND_SYNC_TO_WATCH_KEY)
                        ?.any { it.value } == true
        if (dndSyncToWatchEnabled) {
            applicationContext.startService(
                    Intent(applicationContext, DnDLocalChangeService::class.java))
        }
    }

    private suspend fun tryStartBatterySyncWorkers(service: WatchConnectionService) {
        val watchBatterySyncInfo =
                service.getBoolPrefsForRegisteredWatches(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
        if (watchBatterySyncInfo != null && watchBatterySyncInfo.isNotEmpty()) {
            for (batterySyncBoolPreference in watchBatterySyncInfo) {
                if (batterySyncBoolPreference.value) {
                    val batterySyncInterval =
                            service.getIntPrefForWatch(batterySyncBoolPreference.watchId, PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
                                    ?.value?.toLong() ?: 15
                    val batterySyncWorkerId = BatterySyncWorker.startWorker(
                            applicationContext, batterySyncBoolPreference.watchId, batterySyncInterval)
                    service.updateBatterySyncWorkerId(batterySyncBoolPreference.watchId, batterySyncWorkerId)
                }
            }
        }
    }

    private fun finish() {
        unbindService(watchManagerConnection)
        stopForeground(true)
        stopSelf()
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}