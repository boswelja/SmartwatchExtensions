package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import androidx.lifecycle.asFlow
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber

object Utils {

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

    suspend fun handleDnDStateChange(
        context: Context,
        sourceWatchId: UUID,
        isDnDEnabled: Boolean,
        watchManager: WatchManager = WatchManager.getInstance(context)
    ) {
        // Set the new DnD state
        val success = context.getSystemService<NotificationManager>()!!.setDnD(isDnDEnabled)

        if (success) {
            // Let other watches know DnD state changed
            Timber.d("Successfully set DnD state")
            watchManager.registeredWatches.asFlow().first()
                .filterNot { it.id == sourceWatchId }.forEach { watch ->
                    watchManager.sendMessage(watch, DND_STATUS_PATH, isDnDEnabled.toByteArray())
                }
        } else {
            // Setting DnD failed, likely due to no permission. Disable extensions.
            Timber.w("Failed to set DnD state")
            watchManager.updatePreference(DND_SYNC_TO_PHONE_KEY, false)
            watchManager.updatePreference(DND_SYNC_WITH_THEATER_KEY, false)
            // TODO Send a message to let the user know
        }
    }

    /**
     * Try to set the system DnD status. This will fail if permission is not granted.
     * @param isEnabled Whether DnD should be enabled.
     * @return true if setting DnD succeeds, false otherwise.
     */
    private fun NotificationManager.setDnD(isEnabled: Boolean): Boolean {
        return if (isNotificationPolicyAccessGranted) {
            val newFilter = if (isEnabled)
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else
                NotificationManager.INTERRUPTION_FILTER_ALL
            setInterruptionFilter(newFilter)
            true
        } else {
            Timber.w("No permission to set DnD state")
            false
        }
    }
}
