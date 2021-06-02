package com.boswelja.smartwatchextensions.dashboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Status
import kotlinx.coroutines.Dispatchers

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val watchManager = WatchManager.getInstance(application)

    val status = watchManager.selectedWatchLiveData.switchMap { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)?.asLiveData(Dispatchers.IO)
        } ?: liveData {
            emit(Status.ERROR)
        }
    }
}
