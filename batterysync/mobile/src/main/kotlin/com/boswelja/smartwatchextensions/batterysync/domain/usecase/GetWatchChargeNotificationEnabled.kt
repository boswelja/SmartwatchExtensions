package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Gets the whether battery charge notifications are enabled for the watch with the given ID.
 */
class GetWatchChargeNotificationEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val getBatterySyncEnabled: GetBatterySyncEnabled
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(watchId: String): Flow<FeatureData<Boolean>> {
        return getBatterySyncEnabled(watchId)
            .flatMapLatest { result ->
                result.fold(
                    onSuccess = { batterySyncEnabled ->
                        if (batterySyncEnabled) {
                            settingsRepository.getBoolean(watchId, BATTERY_WATCH_CHARGE_NOTI_KEY)
                                .map { watchChargeNotiEnabled ->
                                    FeatureData.Success(watchChargeNotiEnabled)
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
    }
}
