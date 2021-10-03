package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.discoveryClient
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.messageClient
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A [LifecycleService] that listens for changes in the appropriate settings, and sends DnD change
 * requests to the connected phone.
 */
class DnDLocalChangeListener : LifecycleService() {

    private var theaterCollectorJob: Job? = null
    private var dndCollectorJob: Job? = null

    private val discoveryClient by lazy { discoveryClient() }
    private val messageClient by lazy { messageClient(listOf(DnDStatusSerializer)) }

    private var dndSyncToPhone: Boolean = false
    private var dndSyncWithTheater: Boolean = false

    override fun onCreate() {
        super.onCreate()

        Timber.d("Creating notification channel")
        createNotificationChannel()

        startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())

        // Collect DnD Sync to Phone
        lifecycleScope.launch {
            extensionSettingsStore.data.map {
                Pair(it.dndSyncToPhone, it.dndSyncWithTheater)
            }.collect { (dndSyncToPhone, dndSyncWithTheater) ->
                val syncToPhoneChanged = setDnDSyncToPhone(dndSyncToPhone)
                val syncWithTheaterChanged = setDnDSyncWithTheaterMode(dndSyncWithTheater)
                if (syncToPhoneChanged || syncWithTheaterChanged) {
                    if (!tryStop()) {
                        startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())
                    }
                }
            }
        }
    }

    /**
     * Create a foreground notification for this service.
     * @return The notification to post for this service.
     */
    private fun createNotification(): Notification {
        Timber.d("Creating notification")
        NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
            .apply {
                setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                when {
                    dndSyncToPhone && dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_all_noti_desc))
                    dndSyncToPhone && !dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_to_phone_noti_desc))
                    dndSyncWithTheater && !dndSyncToPhone ->
                        setContentText(getString(R.string.dnd_sync_with_theater_noti_desc))
                    else ->
                        setContentText(getString(R.string.getting_ready))
                }
                setSmallIcon(R.drawable.ic_sync)
                setOngoing(true)
                setShowWhen(false)
                setUsesChronometer(false)
                priority = NotificationCompat.PRIORITY_LOW

                val launchIntent =
                    Intent(this@DnDLocalChangeListener, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                PendingIntent.getActivity(
                    this@DnDLocalChangeListener,
                    START_ACTIVITY_FROM_NOTI_ID,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE
                ).also { setContentIntent(it) }
            }
            .also {
                return it.build()
            }
    }

    /**
     * Sets whether DnD Sync with Theater Mode is enabled. This will handle our theater
     * observer, as well as updating our notification.
     * @param isEnabled Whether this sync type should be enabled.
     * @return true if changes were made, false otherwise.
     */
    private fun setDnDSyncWithTheaterMode(isEnabled: Boolean): Boolean {
        Timber.d("setDnDSyncWithTheaterMode(%s) called", isEnabled)
        return if (dndSyncWithTheater != isEnabled) {
            dndSyncWithTheater = isEnabled

            // Cancel any running collector jobs
            theaterCollectorJob?.cancel()

            if (isEnabled) {
                // Launch our theater mode collector
                theaterCollectorJob = lifecycleScope.launch {
                    theaterMode().collect { theaterMode ->
                        updateDnDState(theaterMode)
                    }
                }
            }

            true
        } else {
            Timber.w("Got dndSyncWithTheater update but we were already up to date")
            false
        }
    }

    /**
     * Sets whether DnD Sync to Phone is enabled. This will handle our DnD observer, as well as
     * updating our notification.
     * @param isEnabled Whether this sync type should be enabled.
     * @return true if changes were made, false otherwise.
     */
    private fun setDnDSyncToPhone(isEnabled: Boolean): Boolean {
        Timber.d("setDnDSyncToPhone(%s) called", isEnabled)
        return if (dndSyncToPhone != isEnabled) {
            dndSyncToPhone = isEnabled

            // Cancel any running collector jobs
            dndCollectorJob?.cancel()

            if (isEnabled) {
                // Launch our DnD state collector
                dndCollectorJob = lifecycleScope.launch {
                    dndState().collect { dndEnabled ->
                        updateDnDState(dndEnabled)
                    }
                }
            }

            true
        } else {
            Timber.w("Got dndSyncWithTheater update but we were already up to date")
            false
        }
    }

    /**
     * Sets a new DnD state across devices.
     * @param dndSyncEnabled Whether Interruption Filter should be enabled.
     */
    private suspend fun updateDnDState(dndSyncEnabled: Boolean) {
        Timber.d("DnD set to %s", dndSyncEnabled)
        messageClient.sendMessage(
            discoveryClient.pairedPhone()!!,
            Message(DND_STATUS_PATH, dndSyncEnabled)
        )
    }

    /**
     * Creates a [NotificationChannel] for [DND_SYNC_NOTI_CHANNEL_ID].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val notificationManager = getSystemService<NotificationManager>()!!
        if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                DND_SYNC_NOTI_CHANNEL_ID,
                getString(R.string.noti_channel_dnd_sync_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * Stop the service if all sync features are disabled.
     * @return true if we stopped the service, false otherwise.
     */
    private fun tryStop(): Boolean {
        if (!dndSyncToPhone && !dndSyncWithTheater) {
            stopForeground(true)
            stopSelf()
            return true
        }
        return false
    }
}
