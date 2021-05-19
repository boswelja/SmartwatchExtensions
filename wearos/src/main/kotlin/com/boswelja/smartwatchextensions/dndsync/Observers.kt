package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.app.NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.provider.Settings
import androidx.core.content.getSystemService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

object Observers {
    private const val THEATER_MODE_ON = "theater_mode_on"

    @ExperimentalCoroutinesApi
    fun Context.theaterMode(): Flow<Boolean> = callbackFlow {
        Timber.d("Starting theater_mode_on collector flow")
        val uri = Settings.Global.getUriFor(THEATER_MODE_ON)
        val contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Timber.d("onChange(%s) called", selfChange)
                if (!selfChange) {
                    val isTheaterModeOn = isTheaterModeOn(contentResolver)
                    Timber.d("isTheaterModeOn = %s", isTheaterModeOn)
                    sendBlocking(isTheaterModeOn)
                }
            }
        }
        contentResolver.registerContentObserver(uri, false, contentObserver)
        awaitClose {
            Timber.d("Stopping theater_mode_on collector flow")
            contentResolver.unregisterContentObserver(contentObserver)
        }
    }

    @ExperimentalCoroutinesApi
    fun Context.dndState(): Flow<Boolean> = callbackFlow {
        val notificationManager = getSystemService<NotificationManager>()!!
        val dndChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent?.action == ACTION_INTERRUPTION_FILTER_CHANGED) {
                    sendBlocking(notificationManager.isDndEnabled())
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_INTERRUPTION_FILTER_CHANGED)
        }
        this@dndState.registerReceiver(dndChangeReceiver, filter)

        awaitClose {
            this@dndState.unregisterReceiver(dndChangeReceiver)
        }
    }

    /**
     * Checks whether Do not Disturb is currently active. Will fall back to silent / vibrate on
     * older Android versions
     * @return true if DnD is enabled, false otherwise.
     */
    private fun NotificationManager.isDndEnabled(): Boolean =
        currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

    private fun isTheaterModeOn(contentResolver: ContentResolver): Boolean =
        Settings.Global.getInt(contentResolver, THEATER_MODE_ON, 0) == 1
}
