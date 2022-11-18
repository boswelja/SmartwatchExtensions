package com.boswelja.smartwatchextensions.core.watches.selected

import kotlinx.serialization.Serializable

/**
 * Contains information about the currently selected watch.
 * @param selectedWatchId The UID of the selected watch.
 */
@Serializable
data class SelectedWatchState(
    val selectedWatchId: String
)
