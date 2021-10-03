package com.boswelja.smartwatchextensions.common

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * An abstract implementation of [TileService] that provides the watch tied to QS Tiles, as well as
 * a coroutine wrapper.
 */
abstract class WatchTileService : TileService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineJob: Job? = null
    internal lateinit var watchId: String
        private set

    /**
     * Called when a tile update is requested.
     * @param watch The [Watch] tied to QS Tiles, or null if none was found.
     */
    abstract suspend fun onTileUpdateRequest(watch: Watch?)

    override fun onTileAdded() {
        coroutineJob = coroutineScope.launch {
            val watch = getWatch()
            onTileUpdateRequest(watch)
        }
    }

    override fun onStartListening() {
        coroutineJob = coroutineScope.launch {
            val watch = getWatch()
            onTileUpdateRequest(watch)
        }
    }

    override fun onTileRemoved() {
        // Cancel any running jobs
        coroutineJob?.cancel()
    }

    /**
     * Try to get the watch attached to QS Tiles.
     * @return The watch attached to QS tiles, or null if not found.
     */
    private suspend fun getWatch(): Watch? {
        val watchId = appSettingsStore.data.map { it.qsTileWatchId }.first()
        this.watchId = watchId
        val watchManager = WatchManager.getInstance(this)
        return if (watchId.isNotBlank()) {
            watchManager.getWatchById(watchId).firstOrNull()
        } else {
            val watch = WatchManager.getInstance(this)
                .registeredWatches
                .first()
                .firstOrNull()

            if (watch != null) {
                appSettingsStore.updateData { it.copy(qsTileWatchId = watch.uid) }
            }
            watch
        }
    }

    /**
     * A convenience function for updating the tile's data.
     */
    fun updateTile(updates: Tile.() -> Unit) {
        qsTile.apply {
            updates()
        }.also { it.updateTile() }
    }
}
