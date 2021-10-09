package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.watchmanager.repository.WatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * A [WatchRepository] that also manages a selected watch state.
 */
class SelectedWatchHandler(
    context: Context,
    private val watchRepository: WatchRepository
) {

    private val dataStore: DataStore<SelectedWatchState> = context.selectedWatchStateStore
    private val selectedWatchId = dataStore.data.map { it.selectedWatchId }

    /**
     * Flow the currently selected [Watch], or null if no watch is selected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedWatch: Flow<Watch?>
        get() = selectedWatchId.flatMapLatest { id -> watchRepository.getWatchById(id) }

    /**
     * Select a different [Watch].
     */
    suspend fun selectWatch(watch: Watch) {
        dataStore.updateData {
            it.copy(selectedWatchId = watch.uid)
        }
    }
}
