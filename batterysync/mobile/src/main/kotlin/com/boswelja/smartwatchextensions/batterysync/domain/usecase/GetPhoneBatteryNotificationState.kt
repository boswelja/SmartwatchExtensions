package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Gets a [DeviceBatteryNotificationState] for phone charge notifications on the watch with the given ID. If no ID is
 * provided, the current selected watch will be used.
 */
class GetPhoneBatteryNotificationState(
    private val getPhoneLowNotificationEnabled: GetPhoneLowNotificationEnabled,
    private val getPhoneChargeNotificationEnabled: GetPhoneChargeNotificationEnabled,
    private val selectedWatchManager: SelectedWatchManager
) {

    operator fun invoke(watchId: String): Flow<FeatureData<DeviceBatteryNotificationState>> {
        return combine(
            getPhoneChargeNotificationEnabled(watchId),
            getPhoneLowNotificationEnabled(watchId)
        ) { phoneChargeNotiEnabled, phoneLowNotiEnabled ->
            if (phoneChargeNotiEnabled is FeatureData.Disabled || phoneLowNotiEnabled is FeatureData.Disabled) {
                FeatureData.Disabled()
            } else {
                FeatureData.Success(
                    DeviceBatteryNotificationState(
                        phoneChargeNotiEnabled.data == true,
                        phoneLowNotiEnabled.data == true
                    )
                )
            }
        }.catch { throwable ->
            emit(FeatureData.Error(throwable))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FeatureData<DeviceBatteryNotificationState>> {
        return selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest {
                invoke(it.uid)
            }
            .catch { throwable ->
                emit(FeatureData.Error(throwable))
            }
    }
}
