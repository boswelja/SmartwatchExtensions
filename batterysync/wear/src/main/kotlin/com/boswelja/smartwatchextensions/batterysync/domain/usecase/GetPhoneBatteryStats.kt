package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.FeatureData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetPhoneBatteryStats(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val getBatterySyncEnabled: GetBatterySyncEnabled
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FeatureData<BatteryStats>> {
        return getBatterySyncEnabled()
            .flatMapLatest { result ->
                result.fold(
                    onSuccess = { batterySyncEnabled ->
                        if (batterySyncEnabled) {
                            batteryStatsRepository.getPhoneBatteryStats().map { batteryStats ->
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
}
