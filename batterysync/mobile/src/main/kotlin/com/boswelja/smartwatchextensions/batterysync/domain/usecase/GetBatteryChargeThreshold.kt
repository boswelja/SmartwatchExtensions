package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.runCatching
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Gets the battery charge threshold for the watch with the given ID.
 */
class GetBatteryChargeThreshold(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchController: SelectedWatchController
) {
    operator fun invoke(watchId: String): Flow<Result<Int>> {
        return settingsRepository.getInt(watchId, BATTERY_CHARGE_THRESHOLD_KEY, DefaultValues.CHARGE_THRESHOLD)
            .runCatching()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<Int>> {
        return selectedWatchController.selectedWatch
            .filterNotNull()
            .flatMapLatest { watch ->
                invoke(watch.uid)
            }
    }
}
