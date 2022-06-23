package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Gets the battery charge threshold for the watch with the given ID.
 */
class GetBatteryChargeThreshold(
    private val settingsRepository: WatchSettingsRepository
) {
    operator fun invoke(watchId: String): Flow<Result<Int>> {
        return settingsRepository.getInt(watchId, BATTERY_CHARGE_THRESHOLD_KEY, DefaultValues.CHARGE_THRESHOLD)
            .map { chargeThreshold ->
                Result.success(chargeThreshold)
            }
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
    }
}
