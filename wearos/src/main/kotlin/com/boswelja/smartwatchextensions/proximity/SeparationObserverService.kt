package com.boswelja.smartwatchextensions.proximity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.phoneStateStore
import com.boswelja.smartwatchextensions.phoneconnectionmanager.ConnectionHelper
import com.boswelja.smartwatchextensions.phoneconnectionmanager.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SeparationObserverService : LifecycleService() {

    private val connectionHelper by lazy { ConnectionHelper(this) }
    private val notificationManager by lazy { getSystemService<NotificationManager>()!! }
    private var hasNotifiedThisDisconnect = false

    override fun onCreate() {
        super.onCreate()
        createObserverNotificationChannel()
        createSeparationNotificationChannel()
        startForeground(FOREGROUND_NOTI_ID, createForegroundNotification())
        collectSettingChanges()
        collectPhoneState()
    }

    private fun collectSettingChanges() {
        lifecycleScope.launch(Dispatchers.IO) {
            extensionSettingsStore.data.map { it.phoneSeparationNotis }.collect {
                if (!it) {
                    tryStop()
                }
            }
        }
    }

    private fun collectPhoneState() {
        val phoneStateStore = phoneStateStore
        lifecycleScope.launch(Dispatchers.Default) {
            connectionHelper.phoneStatus().collect { status ->
                val phoneName = phoneStateStore.data.map { it.name }
                if (status != Status.CONNECTED_NEARBY) {
                    notificationManager.cancel(SEPARATION_NOTI_ID)
                    hasNotifiedThisDisconnect = false
                } else if (!hasNotifiedThisDisconnect) {
                    notificationManager.notify(
                        SEPARATION_NOTI_ID,
                        createSeparationNotification(phoneName.first())
                    )
                    hasNotifiedThisDisconnect = true
                }
            }
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, OBSERVER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.proximity_observer_title))
            .setContentText(getString(R.string.proximity_observer_summary))
            .setSmallIcon(R.drawable.noti_ic_phone)
            .setLocalOnly(true)
            .setUsesChronometer(false)
            .build()
    }

    private fun createSeparationNotification(phoneName: String): Notification {
        return NotificationCompat.Builder(this, SEPARATION_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.separation_notification_title, phoneName))
            .setContentText(getString(R.string.separation_notification_text, phoneName))
            .setSmallIcon(R.drawable.noti_ic_phone_lost)
            .setLocalOnly(true)
            .build()
    }

    private fun createObserverNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    private fun createSeparationNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    private fun tryStop() {
        stopForeground(true)
        stopSelf()
    }

    companion object {
        private const val SEPARATION_NOTI_ID = 11

        const val FOREGROUND_NOTI_ID = 51126
        const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"
        const val SEPARATION_NOTI_CHANNEL_ID = "phone-separation"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, SeparationObserverService::class.java)
            )
        }
    }
}
