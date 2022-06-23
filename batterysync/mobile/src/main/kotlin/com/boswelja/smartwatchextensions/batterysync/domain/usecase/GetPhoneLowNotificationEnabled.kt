package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Gets the whether battery low notifications are enabled for this device on watch with the given ID.
 */
class GetPhoneLowNotificationEnabled(
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
                            settingsRepository.getBoolean(watchId, BATTERY_PHONE_LOW_NOTI_KEY)
                                .map { phoneLowNotiEnabled ->
                                    FeatureData.Success(phoneLowNotiEnabled)
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
