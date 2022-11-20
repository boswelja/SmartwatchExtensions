package com.boswelja.smartwatchextensions.core.watches.selected

import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * A [SelectedWatchController] backed by a [DataStore].
 */
class SelectedWatchControllerImpl(
    private val dataStore: DataStore<SelectedWatchState>,
    private val registeredWatchRepository: RegisteredWatchRepository
) : SelectedWatchController {

    private val selectedWatchId = dataStore.data.map { it.selectedWatchId }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedWatch: Flow<Watch?>
        get() = selectedWatchId
            .flatMapLatest { id -> registeredWatchRepository.getWatchById(id) }
            .mapLatest { it?.let { Watch(it.uid, it.name) } }

    override suspend fun selectWatch(watch: Watch) {
        selectWatch(watch.uid)
    }

    override suspend fun selectWatch(watchId: String) {
        dataStore.updateData {
            it.copy(selectedWatchId = watchId)
        }
    }
}
