package com.boswelja.smartwatchextensions.core.watches.available

import kotlinx.coroutines.flow.Flow

interface AvailableWatchRepository {

    fun getAvailableWatches(): Flow<List<AvailableWatch>>
}
