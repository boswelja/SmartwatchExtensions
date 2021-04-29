package com.boswelja.smartwatchextensions.dndsync

import android.content.Context
import androidx.lifecycle.asFlow
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_STATUS_PATH
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.UUID

object Utils {

    suspend fun handleDnDStateChange(
        context: Context,
        sourceWatchId: UUID,
        isDnDEnabled: Boolean,
        watchManager: WatchManager = WatchManager.getInstance(context)
    ) {
        // Set the new DnD state
        val success = Compat.setInterruptionFilter(context, isDnDEnabled)

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
}
