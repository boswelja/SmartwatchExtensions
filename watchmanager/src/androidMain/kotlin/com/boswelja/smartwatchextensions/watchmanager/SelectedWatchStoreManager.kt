package com.boswelja.smartwatchextensions.watchmanager

import androidx.datastore.core.DataStore
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SelectedWatchStoreManager(
    private val dataStore: DataStore<SelectedWatchState>,
    private val watchRepository: WatchRepository
) : SelectedWatchManager {

    private val selectedWatchId = dataStore.data.map { it.selectedWatchId }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedWatch: Flow<Watch?>
        get() = selectedWatchId.flatMapLatest { id -> watchRepository.getWatchById(id) }

    override suspend fun selectWatch(watch: Watch) {
        selectWatch(watch.uid)
    }

    override suspend fun selectWatch(watchId: String) {
        dataStore.updateData {
            it.copy(selectedWatchId = watchId)
        }
    }
}