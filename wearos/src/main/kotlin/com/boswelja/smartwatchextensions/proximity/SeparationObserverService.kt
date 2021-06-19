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
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SeparationObserverService : LifecycleService() {

    private val notificationManager by lazy { getSystemService<NotificationManager>()!! }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createForegroundNotification())
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
        val capabilityClient = Wearable.getCapabilityClient(this)
        val phoneStateStore = phoneStateStore
        lifecycleScope.launch(Dispatchers.Default) {
            val phoneName = phoneStateStore.data.map { it.name }
            while (true) {
                val capabilityInfo = capabilityClient
                    .getCapability("extensions_phone_app", CapabilityClient.FILTER_ALL)
                    .await()

                // We can assume the phone is the only one on the list
                val phoneNode = capabilityInfo.nodes.firstOrNull()

                if (phoneNode?.isNearby == true) {
                    notificationManager.cancel(SEPARATION_NOTI_ID)
                } else {
                    notificationManager.notify(
                        SEPARATION_NOTI_ID,
                        createSeparationNotification(phoneName.first())
                    )
                }

                // Wait for a specified interval before repeating
                delay(STATUS_CHECK_INTERVAL)
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
        return NotificationCompat.Builder(this, OBSERVER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.separation_notification_title, phoneName))
            .setContentText(getString(R.string.separation_notification_text, phoneName))
            .setSmallIcon(R.drawable.noti_ic_phone_lost)
            .setLocalOnly(true)
            .build()
    }

    private fun createNotificationChannel() {
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

    private fun tryStop() {
        stopForeground(true)
        stopSelf()
    }

    companion object {
        private const val SEPARATION_NOTI_ID = 11
        private const val STATUS_CHECK_INTERVAL = 5000L

        const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, SeparationObserverService::class.java)
            )
        }
    }
}
