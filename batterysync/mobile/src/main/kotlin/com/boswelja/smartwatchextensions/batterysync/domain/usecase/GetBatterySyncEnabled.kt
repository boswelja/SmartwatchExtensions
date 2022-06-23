package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Gets whether battery sync is enabled for the watch with the given ID.
 */
class GetBatterySyncEnabled(
    private val settingsRepository: WatchSettingsRepository
) {
    operator fun invoke(watchId: String): Flow<Result<Boolean>> {
        return settingsRepository.getBoolean(watchId, BATTERY_SYNC_ENABLED_KEY)
            .map { Result.success(it) }
            .catch { throwable ->
                Result.failure<Boolean>(throwable)
            }
    }
}
