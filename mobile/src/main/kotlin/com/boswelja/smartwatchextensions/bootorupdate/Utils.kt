package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.core.settings.BoolSettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.dndsync.LocalDnDCollectorService
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * A notification channel ID for boot/update worker status notifications.
 */
const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"

internal const val NOTI_ID = 69102

internal suspend fun Context.restartServices(
    settingsRepository: WatchSettingsRepository
) {
    tryStartInterruptFilterSync(settingsRepository)
    tryStartSeparationObserver(settingsRepository)
}

/** Try to start Do not Disturb change listener service if needed. */
private suspend fun Context.tryStartInterruptFilterSync(repository: WatchSettingsRepository) {
    val dndSyncToWatchEnabled = repository
        .getIdsWithBooleanSet(DND_SYNC_TO_WATCH_KEY, true)
        .map { it.isNotEmpty() }
        .first()
    if (dndSyncToWatchEnabled) {
        ContextCompat.startForegroundService(
            applicationContext,
            Intent(applicationContext, LocalDnDCollectorService::class.java)
        )
    }
}

private suspend fun Context.tryStartSeparationObserver(repository: WatchSettingsRepository) {
    val watchSeparationAlertsEnabled = repository
        .getIdsWithBooleanSet(WATCH_SEPARATION_NOTI_KEY, true)
        .map { it.isNotEmpty() }
        .first()
    if (watchSeparationAlertsEnabled) {
        SeparationObserverService.start(this)
    }
}
