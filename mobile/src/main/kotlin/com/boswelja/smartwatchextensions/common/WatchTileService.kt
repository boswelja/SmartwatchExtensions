package com.boswelja.smartwatchextensions.common

import android.service.quicksettings.TileService
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.settings.appSettingsStore
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.Exception

/**
 * An abstract implementation of [TileService] that provides the watch tied to QS Tiles, as well as
 * a coroutine wrapper.
 */
abstract class WatchTileService : TileService() {

    private val watchRepository: WatchRepository by inject()

    internal val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineJob: Job? = null

    /**
     * Flows the current watch to display QS tiles for.
     */
    internal val watch: StateFlow<Watch?> = appSettingsStore.data
        .map {
            if (it.qsTileWatchId.isNotBlank()) {
                watchRepository.getWatchById(it.qsTileWatchId).firstOrNull()
            } else {
                val watch = watchRepository
                    .registeredWatches
                    .first()
                    .firstOrNull()

                if (watch != null) {
                    appSettingsStore.updateData { it.copy(qsTileWatchId = watch.uid) }
                }
                watch
            }
        }
        .stateIn(
            coroutineScope,
            SharingStarted.Lazily,
            null
        )

    /**
     * Called when a tile update is requested.
     */
    abstract suspend fun onTileUpdateRequest()

    override fun onTileAdded() {
        coroutineJob?.cancel()
        coroutineJob = coroutineScope.launch {
            onTileUpdateRequest()
        }
    }

    override fun onStartListening() {
        coroutineJob?.cancel()
        coroutineJob = coroutineScope.launch {
            onTileUpdateRequest()
        }
    }

    override fun onTileRemoved() {
        // Cancel any running jobs
        try {
            coroutineScope.cancel()
        } catch(ignored: Exception) { }
    }

}
