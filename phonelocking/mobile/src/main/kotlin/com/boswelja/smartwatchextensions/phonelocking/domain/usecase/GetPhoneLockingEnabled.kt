package com.boswelja.smartwatchextensions.phonelocking.domain.usecase

import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.runCatching
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class GetPhoneLockingEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchManager: SelectedWatchManager
) {
    operator fun invoke(watchId: String): Flow<Result<Boolean>> {
        return settingsRepository.getBoolean(watchId, PHONE_LOCKING_ENABLED_KEY)
            .runCatching()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<Boolean>> {
        return selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest { invoke(it.uid) }
    }
}
