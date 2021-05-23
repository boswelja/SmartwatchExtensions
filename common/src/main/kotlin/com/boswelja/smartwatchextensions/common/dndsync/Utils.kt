package com.boswelja.smartwatchextensions.common.dndsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gets a [Flow] of this watches DnD state.
 */
@ExperimentalCoroutinesApi
fun Context.dndState(): Flow<Boolean> = callbackFlow {
    val notificationManager = getSystemService<NotificationManager>()!!
    val dndChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                sendBlocking(notificationManager.isDndEnabled)
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
