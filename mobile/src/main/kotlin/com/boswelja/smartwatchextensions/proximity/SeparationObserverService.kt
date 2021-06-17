package com.boswelja.smartwatchextensions.proximity

import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class SeparationObserverService : LifecycleService() {

    private val settingsDatabase by lazy { WatchSettingsDatabase.getInstance(this) }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        NotificationChannelHelper.createForSeparationObserver(this, getSystemService()!!)
        startForeground(1, createNotification())
        lifecycleScope.launch(Dispatchers.IO) {
            startCollectingSettings()
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun startCollectingSettings() {
        settingsDatabase.boolSettings().getByKey(WATCH_SEPARATION_NOTI_KEY).mapLatest {
            it.filter { setting -> setting.value }.map { setting -> setting.watchId }
        }.collect { watchIds ->
            if (watchIds.isEmpty()) {
                tryStop()
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, OBSERVER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.proximity_observer_title))
            .setContentText(getString(R.string.proximity_observer_summary))
            .setSmallIcon(R.drawable.noti_ic_watch)
            .build()
    }

    private fun tryStop() {
        stopForeground(true)
        stopSelf()
    }

    companion object {
        const val OBSERVER_NOTI_CHANNEL_ID = "proximity-observer"
        fun start(context: Context) {
            context.startForegroundService(Intent(context, SeparationObserverService::class.java))
        }
    }
}
