package com.boswelja.smartwatchextensions.main.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    val selectedWatch = watchManager.selectedWatch
    val registeredWatches = watchManager.registeredWatches

    val needsSetup = registeredWatches.map { it.isNullOrEmpty() }

    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            watchManager.selectWatchById(watchId)
        }
    }
}
