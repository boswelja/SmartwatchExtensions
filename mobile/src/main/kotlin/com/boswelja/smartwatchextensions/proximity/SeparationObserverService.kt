package com.boswelja.smartwatchextensions.proximity

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.discovery.Status
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber

class SeparationObserverService : LifecycleService() {

    private val watchManager by lazy { WatchManager.getInstance(this) }
    private val settingsDatabase by lazy { WatchSettingsDatabase.getInstance(this) }
    private val hasSentNotiMap = hashMapOf<UUID, Boolean>()
    private var statusCollectorJob: Job? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        NotificationChannelHelper.createForSeparationObserver(this, getSystemService()!!)
        NotificationChannelHelper.createForSeparationNotis(this, getSystemService()!!)
        startForeground(1, createForegroundNotification())
        lifecycleScope.launch(Dispatchers.IO) {
            startCollectingSettings()
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private suspend fun startCollectingSettings() {
        Timber.d("Collecting settings changes")
        settingsDatabase.boolSettings().getByKey(WATCH_SEPARATION_NOTI_KEY).mapLatest {
            it.filter { setting -> setting.value }.map { setting -> setting.watchId }
        }.collect { watchIds ->
            if (watchIds.isEmpty()) {
                tryStop()
            } else {
                collectStatusesFor(watchIds)
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private suspend fun collectStatusesFor(watchIds: List<UUID>) {
        // Map IDs to a list of Flows
        val flows = watchIds.mapNotNull { watchId ->
            // Get the associated watch
            val watch = watchManager.getWatchById(watchId).first()

            // Try get watch status
            watch?.let {
                watchManager.getStatusFor(it)?.debounce(100)?.map { status ->
                    // Pair watch ID to status
                    watch to status
                }
            }
        }.toTypedArray()
        Timber.d("Collecting statuses for %s watches", flows.count())

        // Cancel any existing jobs
        statusCollectorJob?.cancel()

        // Start collecting new values
        statusCollectorJob = lifecycleScope.launch(Dispatchers.Default) {
            merge(*flows).collect { handleStatusChange(it.first, it.second) }
        }
    }

    private fun handleStatusChange(watch: Watch, newStatus: Status) {
        Timber.d("%s status changed to %s", watch.name, newStatus)
        val notificationManager = getSystemService<NotificationManager>()!!
        val notiId = watch.id.hashCode()
        if (newStatus == Status.CONNECTED_NEARBY) {
            notificationManager.cancel(notiId)
            hasSentNotiMap[watch.id] = false
        } else if (hasSentNotiMap[watch.id] != true) {
            val notification = createSeparationNotification(watch.name)
            notificationManager.notify(notiId, notification)
            hasSentNotiMap[watch.id] = true
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
            .setLocalOnly(true)
            .setUsesChronometer(false)
            .build()
    }

    private fun tryStop() {
        Timber.d("Stopping service")
        statusCollectorJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    companion object {
        const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"
        const val SEPARATION_NOTI_CHANNEL_ID = "watch-separation"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, SeparationObserverService::class.java))
        }
    }
}
