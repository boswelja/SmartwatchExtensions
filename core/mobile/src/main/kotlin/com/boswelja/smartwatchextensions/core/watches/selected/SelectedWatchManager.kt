package com.boswelja.smartwatchextensions.core.watches.selected

import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.Flow

/**
 * A helper for managing watch selection state.
 */
interface SelectedWatchManager {

    /**
     * Flow the currently selected [Watch], or null if no watch is selected.
     */
    val selectedWatch: Flow<Watch?>

    /**
     * Select a different [Watch].
     */
    suspend fun selectWatch(watch: Watch)

    /**
     * Select a different [Watch] by it's UID.
     */
    suspend fun selectWatch(watchId: String)
}
