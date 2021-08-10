package com.boswelja.smartwatchextensions.watchmanager.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class WatchInfoViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    val watchId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val watch: Flow<Watch?> = watchId.flatMapLatest { id ->
        id?.let {
            watchManager.getWatchById(id)
        } ?: flow { emit(null) }
    }

    val watchName = watch.map { it?.name }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    suspend fun updateWatchName(name: String) {
        Timber.d("updateWatchName($name) called")
        watch.first()?.let {
            Timber.d("Updating watch name")
            watchManager.renameWatch(it, name)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCapabilities(): Flow<List<Capability>> = watch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getCapabilitiesFor(it)
        } ?: flow { emit(emptyList<Capability>()) }
    }

    /**
     * Forgets the current watch.
     */
    suspend fun forgetWatch() {
        watch.first()?.let {
            watchManager.forgetWatch(getApplication(), it)
        }
    }

    /**
     * Resets the current watch preferences.
     */
    suspend fun resetWatchPreferences() {
        watch.first()?.let {
            watchManager.resetWatchPreferences(getApplication(), it)
        }
    }
}
