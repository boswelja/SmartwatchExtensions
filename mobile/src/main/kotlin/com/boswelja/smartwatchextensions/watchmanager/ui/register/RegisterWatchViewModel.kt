package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    private val _addedWatches = MutableLiveData<List<Watch>>()
    private val _watchesToAdd = MediatorLiveData<List<Watch>>()

    val registeredWatches: LiveData<List<Watch>>
        get() = _addedWatches

    val watchesToAdd: LiveData<List<Watch>>
        get() = _watchesToAdd

    init {
        _watchesToAdd.addSource(watchManager.availableWatches) {
            val watchesToRegister = it.filterNot { watch ->
                watchManager.registeredWatches.value?.contains(watch) == true
            }
            _watchesToAdd.postValue(watchesToRegister)
        }
        _watchesToAdd.addSource(watchManager.registeredWatches) { registeredWatches ->
            val watchesToRegister = _watchesToAdd.value?.let {
                it.filterNot { watch -> registeredWatches.contains(watch) }
            } ?: emptyList()
            _watchesToAdd.postValue(watchesToRegister)
        }
    }

    fun addWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        val shouldRegisterWatch = (_addedWatches.value ?: emptyList())
            .none { it.id == watch.id }
        if (shouldRegisterWatch) {
            _addedWatches.postValue(
                _addedWatches.value?.plus(watch) ?: listOf(watch)
            )
            viewModelScope.launch(dispatcher) {
                watchManager.registerWatch(watch)
            }
        } else {
            Timber.w("Tried registering a watch that isn't available to register")
        }
    }

    fun refreshData() {
        watchManager.refreshData()
    }
}
