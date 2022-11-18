package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.runCatching
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

    operator fun invoke(watchId: String): Flow<Result<DeviceBatteryNotificationState>> {
        return combine(
            getPhoneChargeNotificationEnabled(watchId),
            getPhoneLowNotificationEnabled(watchId)
        ) { phoneChargeNotiEnabled, phoneLowNotiEnabled ->
            DeviceBatteryNotificationState(
                chargeNotificationsEnabled = phoneChargeNotiEnabled.getOrThrow(),
                lowNotificationsEnabled = phoneLowNotiEnabled.getOrThrow()
            )
        }.runCatching()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<DeviceBatteryNotificationState>> {
        return selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest {
                invoke(it.uid)
            }
    }
}
