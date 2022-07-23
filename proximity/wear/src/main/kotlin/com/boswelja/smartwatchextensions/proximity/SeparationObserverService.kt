package com.boswelja.smartwatchextensions.proximity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.boswelja.smartwatchextensions.proximity.domain.ProximityStateRepository
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A service for observing the connection status for the local device.
 */
class SeparationObserverService : LifecycleService() {

    private val proximityStateRepository: ProximityStateRepository by inject()
    private val discoveryClient: DiscoveryClient by inject()
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private var hasNotifiedThisDisconnect = false

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        createObserverNotiChannel()
        createSeparationNotiChannel()
        startForeground(FOREGROUND_NOTI_ID, createForegroundNotification())
        collectSettingChanges()
        collectPhoneState()
    }

    private fun collectSettingChanges() {
        lifecycleScope.launch(Dispatchers.IO) {
            proximityStateRepository.getPhoneSeparationAlertEnabled().collect {
                if (!it) {
                    tryStop()
                }
            }
        }
    }

    private fun collectPhoneState() {
        lifecycleScope.launch(Dispatchers.Default) {
            discoveryClient.connectionMode().collect { status ->
                val phoneName = phoneStateStore.data.map { it.name }
                if (status == ConnectionMode.Bluetooth) {
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

    private fun createObserverNotiChannel() {
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

    private fun createSeparationNotiChannel() {
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

    private fun tryStop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {

        /**
         * A notification ID for separation alerts.
         */
        const val SEPARATION_NOTI_ID = 11

        /**
         * A notification ID for the local separation observer.
         */
        const val FOREGROUND_NOTI_ID = 51126

        /**
         * The notification channel ID for the local separation observer.
         */
        const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"

        /**
         * The notification channel ID for separation alerts.
         */
        const val SEPARATION_NOTI_CHANNEL_ID = "phone-separation"

        /**
         * Start the [SeparationObserverService].
         */
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, SeparationObserverService::class.java)
            )
        }
    }
}
