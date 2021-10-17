package com.boswelja.smartwatchextensions.main.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class MainViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

    val selectedWatch = watchManager.selectedWatch
    val registeredWatches = watchManager.registeredWatches

    val needsSetup = registeredWatches.map { it.isNullOrEmpty() }

    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            watchManager.selectWatchById(watchId)
        }
    }
}
