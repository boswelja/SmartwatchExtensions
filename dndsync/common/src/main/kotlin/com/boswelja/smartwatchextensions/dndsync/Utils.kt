package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gets a [Flow] of this watches DnD state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Context.dndState(): Flow<Boolean> = callbackFlow {
    val notificationManager = getSystemService(NotificationManager::class.java)
    val dndChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                trySend(notificationManager.isDndEnabled)
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
    }
    this@dndState.registerReceiver(dndChangeReceiver, filter)

    send(notificationManager.isDndEnabled)

    awaitClose {
        this@dndState.unregisterReceiver(dndChangeReceiver)
    }
}

/**
 * Checks whether DnD is enabled for this watch.
 * @return true if DnD is enabled, false otherwise.
 */
private val NotificationManager.isDndEnabled: Boolean
    get() = this.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

/**
 * Try to set the system DnD status. This will fail if permission is not granted.
 * @param isEnabled Whether DnD should be enabled.
 * @return true if setting DnD succeeds, false otherwise.
 */
fun NotificationManager.setDnD(isEnabled: Boolean): Boolean {
    return if (isNotificationPolicyAccessGranted) {
        val newFilter = if (isEnabled)
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        else
            NotificationManager.INTERRUPTION_FILTER_ALL
        setInterruptionFilter(newFilter)
        true
    } else {
        false
    }
}
