package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.common.R
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class DnDLocalChangeService : LifecycleService() {

    private val dndCollectorJob = Job()

    private val watchManager by lazy { WatchManager.getInstance(this) }

    private val targetWatches = ArrayList<Watch>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate() called")

        lifecycleScope.launch {
            watchManager.settingsRepository
                .getIdsWithBooleanSet(DND_SYNC_TO_WATCH_KEY, true)
                .map { ids ->
                    ids.mapNotNull { id ->
                        watchManager.getWatchById(id).firstOrNull()
                    }
                }.collect {
                    targetWatches.clear()
                    targetWatches.addAll(it)
                    stopIfUnneeded()
                }
        }

        lifecycleScope.launch(dndCollectorJob) {
            dndState().collect { sendNewDnDState(it) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand() called")
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy() called")

        dndCollectorJob.cancel()
    }

    /**
     * Create a [Notification] to show this service is in the foreground.
     * @return The created [Notification].
     */
    private fun createNotification(): Notification {
        NotificationChannelHelper.createForDnDSync(this, getSystemService()!!)
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val notiTapIntent = PendingIntent.getActivity(
            this,
            0,
            launchActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.interrupt_filter_sync_active_noti_title))
            .setContentText(getString(R.string.interrupt_filter_sync_to_phone_noti_desc))
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(notiTapIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Push the new DnD state to the [DataClient] for any watches to retrieve.
     * @param dndEnabled The new state of Do not Disturb.
     */
    private fun sendNewDnDState(dndEnabled: Boolean) {
        Timber.i("sendNewDnDState($dndEnabled) called")
        lifecycleScope.launch {
            targetWatches.forEach { watch ->
                val result = watchManager.sendMessage(
                    watch,
                    Message(
                        DND_STATUS_PATH,
                        dndEnabled
                    )
                )
                if (!result) {
                    Timber.w("Failed to update DnD on ${watch.name}")
                }
            }
        }
    }

    /** Stops the service if it doesn't need to be running any more. */
    private fun stopIfUnneeded() {
        if (targetWatches.isEmpty()) {
            Timber.i("Service unneeded, stopping")
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 52447
    }
}
