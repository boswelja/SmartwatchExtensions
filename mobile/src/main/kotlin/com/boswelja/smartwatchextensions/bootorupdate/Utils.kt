package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.WATCH_SEPARATION_NOTI_KEY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * A notification channel ID for boot/update worker status notifications.
 */
const val BootUpdateNotiChannelId = "boot_or_update_noti_channel"

internal const val NotiId = 69102

internal suspend fun Context.restartServices(
    settingsRepository: WatchSettingsRepository
) {
    tryStartSeparationObserver(settingsRepository)
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
