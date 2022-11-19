package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Retrieves a [BatteryStats] for the watch with the given ID, if battery sync is enabled.
 */
class GetBatteryStats(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val getBatterySyncEnabled: GetBatterySyncEnabled,
    private val selectedWatchController: SelectedWatchController
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(watchId: String): Flow<FeatureData<BatteryStats>> {
        return getBatterySyncEnabled(watchId)
            .flatMapLatest { result ->
                result.fold(
                    onSuccess = { batterySyncEnabled ->
                        if (batterySyncEnabled) {
                            batteryStatsRepository.getBatteryStatsForWatch(watchId)
                                .filterNotNull()
                                .map { batteryStats ->
                                    FeatureData.Success(batteryStats)
                                }
                        } else {
                            flowOf(FeatureData.Disabled())
                        }
                    },
                    onFailure = { throwable ->
                        flowOf(FeatureData.Error(throwable))
                    }
                )

            }
            .catch { throwable ->
                emit(FeatureData.Error(throwable))
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FeatureData<BatteryStats>> {
        return selectedWatchController.selectedWatch
            .filterNotNull()
            .flatMapLatest { watch ->
                invoke(watch.uid)
            }
            .catch { throwable ->
                emit(FeatureData.Error(throwable))
            }
    }
}
