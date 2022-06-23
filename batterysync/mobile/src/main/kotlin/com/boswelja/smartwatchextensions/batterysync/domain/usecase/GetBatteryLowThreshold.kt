package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Gets the battery low threshold for the watch with the given ID.
 */
class GetBatteryLowThreshold(
    private val settingsRepository: WatchSettingsRepository
) {
    operator fun invoke(watchId: String): Flow<Result<Int>> {
        return settingsRepository.getInt(watchId, BATTERY_LOW_THRESHOLD_KEY, DefaultValues.CHARGE_THRESHOLD)
            .map { lowThreshold ->
                Result.success(lowThreshold)
            }
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
    }
}
