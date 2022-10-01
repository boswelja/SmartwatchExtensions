package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.watchmanager.domain.WatchVersionRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WatchManagerViewModel(
    private val watchRepository: WatchRepository,
    private val watchVersionRepository: WatchVersionRepository
) : ViewModel() {

    val registeredWatches: Flow<List<Watch>> = watchRepository.registeredWatches
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

    suspend fun renameWatch(watch: Watch, newName: String): Result<Unit> {
        return runCatching {
            watchRepository.renameWatch(watch, newName)
        }
    }

    suspend fun forgetWatch(watch: Watch): Result<Unit> {
        return runCatching {
            watchRepository.deregisterWatch(watch)
        }
    }
}
