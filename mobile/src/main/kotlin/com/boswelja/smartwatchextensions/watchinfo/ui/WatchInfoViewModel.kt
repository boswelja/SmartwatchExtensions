package com.boswelja.smartwatchextensions.watchinfo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class WatchInfoViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private val watchId = MutableLiveData<UUID>()

    val watch: LiveData<Watch?> =
        watchId.switchMap { id -> watchManager.observeWatchById(id).map { it } }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    /**
     * Sets the ID of the current watch we're showing info for.
     * @param watchId The [Watch.id] of the current watch.
     */
    fun setWatch(watchId: UUID) {
        this.watchId.postValue(watchId)
    }

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    fun updateWatchName(name: String) {
        Timber.d("updateWatchName($name) called")
        watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                Timber.d("Updating watch name")
                watchManager.renameWatch(watch, name)
            }
        }
    }

    fun getCapabilities(): LiveData<Array<Capability>> {
        return watch.switchMap { watch ->
            watch?.let {
                watchManager.getCapabilitiesFor(it)
                    ?.mapNotNull { capabilities ->
                        capabilities.map { capability ->
                            Capability.valueOf(capability)
                        }.toTypedArray()
                    }?.asLiveData()
            } ?: liveData { }
        }
    }

    /**
     * Forgets the current watch.
     */
    fun forgetWatch() {
        watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                watchManager.forgetWatch(getApplication<Application>(), watch)
            }
        }
    }

    /**
     * Resets the current watch preferences.
     */
    fun resetWatchPreferences() {
        watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                watchManager.resetWatchPreferences(getApplication<Application>(), watch)
            }
        }
    }
}
