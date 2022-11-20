package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.watchmanager.domain.WatchVersionRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class WatchManagerViewModel(
    registeredWatchRepository: RegisteredWatchRepository,
    private val watchVersionRepository: WatchVersionRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val registeredWatches: Flow<List<Watch>> = registeredWatchRepository.registeredWatches
        .mapLatest {
            it.map {
                Watch(it.uid, it.name)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    suspend fun loadWatchVersion(watchId: String): Result<String> {
        return runCatching {
            watchVersionRepository.getWatchVersion(watchId).versionName
        }
    }
}
