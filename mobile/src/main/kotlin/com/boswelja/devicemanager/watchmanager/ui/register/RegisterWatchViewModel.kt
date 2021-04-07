package com.boswelja.devicemanager.watchmanager.ui.register

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
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

    private val _registeredWatches = MutableLiveData<List<Watch>>()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    val registeredWatches: LiveData<List<Watch>>
        get() = _registeredWatches

    val availableWatches = watchManager.availableWatches.map { watches ->
        watches.filterNot { watchManager.registeredWatches.value?.contains(it) == true }
    }

    fun registerWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        viewModelScope.launch(dispatcher) {
            try {
                watchManager.registerWatch(watch)
                _registeredWatches.postValue(
                    _registeredWatches.value?.plus(watch) ?: listOf(watch)
                )
            } catch (e: SQLiteException) {
                Timber.w("Tried to register a watch that's already registered")
            }
        }
    }

    fun refreshData() {
        watchManager.refreshData()
    }
}
