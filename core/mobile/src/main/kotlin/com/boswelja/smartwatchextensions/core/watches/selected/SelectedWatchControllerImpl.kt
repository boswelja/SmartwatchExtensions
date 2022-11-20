package com.boswelja.smartwatchextensions.core.watches.selected

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A [SelectedWatchController] backed by a [DataStore].
 */
class SelectedWatchControllerImpl(
    private val dataStore: DataStore<SelectedWatchState>
) : SelectedWatchController {

    override val selectedWatch: Flow<String?>
        get() = dataStore.data.map { it.selectedWatchId }

    override suspend fun selectWatch(watchId: String) {
        dataStore.updateData {
            it.copy(selectedWatchId = watchId)
        }
    }
}
