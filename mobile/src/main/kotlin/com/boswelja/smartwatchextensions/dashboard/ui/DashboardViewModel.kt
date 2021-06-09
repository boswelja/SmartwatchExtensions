package com.boswelja.smartwatchextensions.dashboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val watchManager = WatchManager.getInstance(application)

    val status = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flow {
            emit(Status.ERROR)
        }
    }
}
