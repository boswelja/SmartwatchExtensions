package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.runCatching
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Gets whether battery sync is enabled for the watch with the given ID.
 */
class GetBatterySyncEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchManager: SelectedWatchManager
) {
    operator fun invoke(watchId: String): Flow<Result<Boolean>> {
        return settingsRepository.getBoolean(watchId, BATTERY_SYNC_ENABLED_KEY)
            .runCatching()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<Boolean>> {
        return selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest { watch ->
                invoke(watch.uid)
            }
    }
}
