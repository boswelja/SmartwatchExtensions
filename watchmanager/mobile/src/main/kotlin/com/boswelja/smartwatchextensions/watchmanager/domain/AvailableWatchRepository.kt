package com.boswelja.smartwatchextensions.watchmanager.domain

import kotlinx.coroutines.flow.Flow

interface AvailableWatchRepository {

    fun getAvailableWatches(): Flow<List<AvailableWatch>>
}
