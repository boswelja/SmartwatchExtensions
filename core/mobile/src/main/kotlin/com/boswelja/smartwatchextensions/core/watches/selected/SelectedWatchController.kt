package com.boswelja.smartwatchextensions.core.watches.selected

import kotlinx.coroutines.flow.Flow

/**
 * A helper for managing watch selection state.
 */
interface SelectedWatchController {

    /**
     * Flow the currently selected watches ID, or null if no watch is selected.
     */
    val selectedWatch: Flow<String?>

    /**
     * Select a different watch by its ID.
     */
    suspend fun selectWatch(watchId: String)
}
