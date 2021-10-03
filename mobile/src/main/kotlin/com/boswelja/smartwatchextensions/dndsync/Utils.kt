package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.sendMessage
import com.boswelja.smartwatchextensions.settingssync.BoolSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.settingssync.BoolSettingKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.flow.first
import timber.log.Timber

object Utils {

    suspend fun handleDnDStateChange(
        context: Context,
        sourceWatchId: String,
        isDnDEnabled: Boolean,
        watchManager: WatchManager = WatchManager.getInstance(context)
    ) {
        // Set the new DnD state
        val success = context.getSystemService<NotificationManager>()!!.setDnD(isDnDEnabled)

        if (success) {
            // Let other watches know DnD state changed
            Timber.d("Successfully set DnD state")
            // TODO we need to check whether watches have DnD Sync on
            watchManager.registeredWatches.first()
                .filterNot { it.uid == sourceWatchId }.forEach { watch ->
                    watchManager.sendMessage(watch, DND_STATUS_PATH, isDnDEnabled.toByteArray())
                }
        } else {
            // Setting DnD failed, likely due to no permission. Disable extensions.
            Timber.w("Failed to set DnD state")
            watchManager.updatePreference(DND_SYNC_TO_PHONE_KEY, false)
            watchManager.updatePreference(DND_SYNC_WITH_THEATER_KEY, false)
            context.sendMessage(
                Message(
                    Message.Icon.ERROR,
                    context.getString(R.string.dnd_sync_error_title),
                    context.getString(R.string.dnd_sync_error_summary)
                )
            )
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
