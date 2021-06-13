package com.boswelja.smartwatchextensions.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
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

    fun selectWatchById(watchId: UUID) = watchManager.selectWatchById(watchId)
}
