package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Gets a [DeviceBatteryNotificationState] for watch charge notifications on this device for thw watch with the given
 * ID. If no ID is provided, the current selected watch will be used.
 */
class GetWatchBatteryNotificationState(
    private val getWatchLowNotificationEnabled: GetWatchLowNotificationEnabled,
    private val getWatchChargeNotificationEnabled: GetWatchChargeNotificationEnabled,
    private val selectedWatchManager: SelectedWatchManager
) {

    operator fun invoke(watchId: String): Flow<Result<DeviceBatteryNotificationState>> {
        return combine(
            getWatchChargeNotificationEnabled(watchId),
            getWatchLowNotificationEnabled(watchId)
        ) { watchChargeNotiEnabled, watchLowNotiEnabled ->
            Result.success(
                DeviceBatteryNotificationState(
                    watchChargeNotiEnabled.getOrDefault(false),
                    watchLowNotiEnabled.getOrDefault(false)
                )
            )
        }.catch { throwable ->
            emit(Result.failure(throwable))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<DeviceBatteryNotificationState>> {
        return selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest {
                invoke(it.uid)
            }
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
    }
}
