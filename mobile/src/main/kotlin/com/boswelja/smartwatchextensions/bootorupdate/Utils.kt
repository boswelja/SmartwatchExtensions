package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeService
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.settingssync.BoolSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.settingssync.BoolSettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
internal const val NOTI_ID = 69102

internal suspend fun Context.restartServices() {
    Timber.d("restartServices() called")
    WatchSettingsDatabase.getInstance(this).also {
        tryStartInterruptFilterSyncService(it)
        tryStartSeparationObserverService(it)
    }
}

/** Try to start Do not Disturb change listener service if needed. */
private suspend fun Context.tryStartInterruptFilterSyncService(database: WatchSettingsDatabase) {
    withContext(Dispatchers.IO) {
        val dndSyncToWatchEnabled =
            database.boolSettings().getByKey(DND_SYNC_TO_WATCH_KEY).first().any {
                it.value
            }
        Timber.i(
            "tryStartInterruptFilterSyncService dndSyncToWatchEnabled = $dndSyncToWatchEnabled"
        )
        if (dndSyncToWatchEnabled) {
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, DnDLocalChangeService::class.java)
            )
        }
    }
}

private suspend fun Context.tryStartSeparationObserverService(database: WatchSettingsDatabase) {
    val watchSeparationAlertsEnabled =
        database.boolSettings().getByKey(WATCH_SEPARATION_NOTI_KEY).first().any { it.value }
    if (watchSeparationAlertsEnabled) {
        SeparationObserverService.start(this)
    }
}
