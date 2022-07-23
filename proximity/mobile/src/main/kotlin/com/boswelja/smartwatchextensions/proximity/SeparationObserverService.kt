package com.boswelja.smartwatchextensions.proximity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A service for observing the connection status of specified watches.
 */
class SeparationObserverService : LifecycleService() {

    private val watchRepository: WatchRepository by inject()
    private val settingsRepository: WatchSettingsRepository by inject()
    private val discoveryClient: DiscoveryClient by inject()

    private val hasSentNotiMap = hashMapOf<String, Boolean>()
    private var statusCollectorJob: Job? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        createForSeparationObserver(getSystemService(NotificationManager::class.java))
        createForSeparationNotis(getSystemService(NotificationManager::class.java))
        startForeground(FOREGROUND_NOTI_ID, createForegroundNotification())
        lifecycleScope.launch(Dispatchers.IO) {
            startCollectingSettings()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun startCollectingSettings() {
        settingsRepository
            .getIdsWithBooleanSet(WATCH_SEPARATION_NOTI_KEY, true)
            .collect { watchIds ->
                if (watchIds.isEmpty()) {
                    tryStop()
                } else {
                    collectStatusesFor(watchIds)
                }
            }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun collectStatusesFor(watchIds: List<String>) {
        // Map IDs to a list of Flows
        val flows = watchIds.map { watchId ->
            discoveryClient.connectionModeFor(watchId)
                .debounce(SEPARATION_DEBOUNCE_MILLIS)
                .map { status ->
                    // Pair watch ID to status
                    watchId to status
                }
        }.toTypedArray()

        // Cancel any existing jobs
        statusCollectorJob?.cancel()

        // Start collecting new values
        statusCollectorJob = lifecycleScope.launch(Dispatchers.Default) {
            flows.toList().merge().collect { handleStatusChange(it.first, it.second) }
        }
    }

    private suspend fun handleStatusChange(watchUid: String, newStatus: ConnectionMode) {
        val notificationManager = getSystemService(NotificationManager::class.java)!!
        val notiId = watchUid.hashCode()
        if (newStatus == ConnectionMode.Bluetooth) {
            notificationManager.cancel(notiId)
            hasSentNotiMap[watchUid] = false
        } else if (hasSentNotiMap[watchUid] != true) {
            val watch = watchRepository.getWatchById(watchUid).first()!!
            val notification = createSeparationNotification(watch.name)
            notificationManager.notify(notiId, notification)
            hasSentNotiMap[watchUid] = true
        }
    }

    private fun createSeparationNotification(watchName: String): Notification {
        return NotificationCompat.Builder(this, SEPARATION_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.separation_notification_title, watchName))
            .setContentText(getString(R.string.separation_notification_text, watchName))
            .setSmallIcon(R.drawable.noti_ic_watch)
            .setLocalOnly(true)
            .build()
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, OBSERVER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.proximity_observer_title))
            .setContentText(getString(R.string.proximity_observer_summary))
            .setSmallIcon(R.drawable.noti_ic_watch)
            .setOngoing(true)
            .setLocalOnly(true)
            .setUsesChronometer(false)
            .build()
    }

    private fun tryStop() {
        statusCollectorJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Create a notification channel for separation observer status notifications.
     */
    private fun createForSeparationObserver(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(OBSERVER_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                OBSERVER_NOTI_CHANNEL_ID,
                getString(R.string.proximity_observer_noti_channel_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    /**
     * Create a notification channel for Separation Alerts.
     */
    private fun createForSeparationNotis(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(SEPARATION_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                SEPARATION_NOTI_CHANNEL_ID,
                getString(R.string.separation_noti_channel_title),
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    companion object {
        private const val SEPARATION_DEBOUNCE_MILLIS = 100L
        private const val FOREGROUND_NOTI_ID = 51126

        /**
         * A notification channel ID for persistent observer service notifications.
         */
        private const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"

        /**
         * A notification channel ID for posting separation alerts.
         */
        private const val SEPARATION_NOTI_CHANNEL_ID = "watch-separation"

        /**
         * Start the service.
         */
        fun start(context: Context) {
            context.startForegroundService(Intent(context, SeparationObserverService::class.java))
        }
    }
}
