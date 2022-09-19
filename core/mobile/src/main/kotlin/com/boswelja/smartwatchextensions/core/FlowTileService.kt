package com.boswelja.smartwatchextensions.core

import android.service.quicksettings.TileService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

/**
 * A [TileService] that takes a [Flow] via [getDataFlow] and automatically manages FLow collection.
 * Override [onTileUpdateRequested] to handle data changes.
 */
abstract class FlowTileService<T> : CoroutineTileService() {

    abstract fun getDataFlow(): Flow<T>

    abstract fun onTileUpdateRequested(data: T)

    final override suspend fun onTileUpdateRequested() {
        val data = getDataFlow().first()
        onTileUpdateRequested(data)
    }

    final override suspend fun onTileStartListening() {
        getDataFlow().collectLatest(::onTileUpdateRequested)
    }
}
