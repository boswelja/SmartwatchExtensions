package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Retrieves a [BatteryStats] for the watch with the given ID, if battery sync is enabled.
 */
class GetBatteryStatsForWatch(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val settingsRepository: WatchSettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(watchId: String): Flow<FeatureData<BatteryStats>> {
        return settingsRepository.getBoolean(watchId, BATTERY_SYNC_ENABLED_KEY)
            .flatMapLatest { batterySyncEnabled ->
                if (batterySyncEnabled) {
                    batteryStatsRepository.batteryStatsFor(watchId).map { batteryStats ->
                        FeatureData.Success(batteryStats)
                    }
                } else {
                    flowOf(FeatureData.Disabled())
                }
            }
            .catch { throwable ->
                emit(FeatureData.Error(throwable))
            }
    }
}
