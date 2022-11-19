package com.boswelja.smartwatchextensions.core.watches.status

import kotlinx.coroutines.flow.Flow

interface WatchStatusRepository {

    /**
     * Flow the [ConnectionMode] the watch with the given ID.
     */
    fun getStatusFor(watchId: String): Flow<ConnectionMode>

}
