package com.boswelja.smartwatchextensions.core

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * A [TileService] that provides suspending functions.
 */
abstract class CoroutineTileService : TileService() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var coroutineJob: Job? = null

    /**
     * Called when a tile update is requested.
     */
    abstract suspend fun onTileUpdateRequested()

    /**
     * Called when the tile should start listening for its own data changes. When this is called, the tile should be
     * responsible for keeping its own UI up to date.
     */
    abstract suspend fun onTileStartListening()

    final override fun onTileAdded() {
        coroutineJob?.cancel()
        coroutineJob = coroutineScope.launch {
            onTileUpdateRequested()
        }
    }

    final override fun onStartListening() {
        coroutineJob?.cancel()
        coroutineJob = coroutineScope.launch {
            onTileStartListening()
        }
    }

    final override fun onStopListening() {
        coroutineJob?.cancel()
    }

    final override fun onTileRemoved() {
        // Cancel any running jobs
        try {
            coroutineScope.cancel()
        } catch(ignored: Exception) { }
    }

    fun updateTile(updates: Tile.() -> Unit) {
        qsTile.apply {
            updates()
        }.also { it.updateTile() }
    }
}
