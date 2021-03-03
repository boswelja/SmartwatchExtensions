package com.boswelja.devicemanager.watchinfo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchInfoViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private val _watch = MediatorLiveData<Watch>()
    private val watchId = MutableLiveData<String>()

    val watch: LiveData<Watch>
        get() = _watch

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    init {
        _watch.addSource(watchManager.registeredWatches) { watches ->
            Timber.d("registeredWatches updated")
            watches.firstOrNull { it.id == watchId.value }?.let {
                _watch.postValue(it)
            }
        }
        _watch.addSource(watchId) { id ->
            Timber.d("watchId updated")
            watchManager.registeredWatches.value?.firstOrNull { it.id == id }?.let {
                _watch.postValue(it)
            }
        }
        watchManager.refreshData()
    }

    /**
     * Sets the ID of the current watch we're showing info for.
     * @param watchId The [Watch.id] of the current watch.
     */
    fun setWatch(watchId: String) {
        this.watchId.postValue(watchId)
    }

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    fun updateWatchName(name: String) {
        Timber.d("updateWatchName($name) called")
        _watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                Timber.d("Updating watch name")
                watchManager.renameWatch(watch, name)
            }
        }
    }

    fun refreshCapabilities() {
        _watch.value?.let {
            watchManager.requestRefreshCapabilities(it)
        }
    }

    /**
     * Forgets the current watch.
     */
    fun forgetWatch() {
        _watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                watchManager.forgetWatch(getApplication(), watch)
            }
        }
    }

    /**
     * Resets the current watch preferences.
     */
    fun resetWatchPreferences() {
        _watch.value?.let { watch ->
            viewModelScope.launch(dispatcher) {
                watchManager.resetWatchPreferences(getApplication(), watch)
            }
        }
    }
}
