package com.boswelja.smartwatchextensions.managespace.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.AppSettingsSerializer
import com.boswelja.smartwatchextensions.settings.Settings
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

class ManageSpaceViewModel(
    application: Application,
    private val analytics: Analytics,
    private val watchManager: WatchManager,
    private val appSettingsDataStore: DataStore<Settings>,
    private val coroutineDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private var registeredWatches: List<Watch>? = null

    init {
        viewModelScope.launch {
            watchManager.registeredWatches.collect {
                registeredWatches = it
            }
        }
    }

    /**
     * Reset analytics storage. See [Analytics.resetAnalytics].
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetAnalytics(
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            analytics.resetAnalytics()
            onCompleteFunction(true)
        }
    }

    /**
     * Reset extension settings for all registered watches.
     * @param onProgressChanged The function to call when clear progress is changed.
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetExtensionSettings(
        onProgressChanged: (progress: Int) -> Unit,
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val registeredWatches = registeredWatches
            if (!registeredWatches.isNullOrEmpty()) {
                var progress: Int
                val watchCount = registeredWatches.count()
                val progressMultiplier =
                    floor(MAX_PROGRESS / watchCount).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.resetWatchPreferences(watch)
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
            }
            onCompleteFunction(true)
        }
    }

    /**
     * Reset app settings for Smartwatch Extensions. This does not include extension settings.
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetAppSettings(
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            appSettingsDataStore.updateData {
                AppSettingsSerializer().defaultValue
            }
            withContext(Dispatchers.Main) { onCompleteFunction(true) }
        }
    }

    /**
     * Requests all registered watches be reset, and removes them from the database. This should be
     * followed by a call to [android.app.ActivityManager.clearApplicationUserData] to clean up
     * everything else.
     * @param onProgressChanged The function to call when clear progress is changed.
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetApp(
        onProgressChanged: (progress: Int) -> Unit,
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val registeredWatches = registeredWatches
            if (!registeredWatches.isNullOrEmpty()) {
                val watchCount = registeredWatches.count()
                var progress: Int
                val progressMultiplier = floor(MAX_PROGRESS / (watchCount + 1)).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.forgetWatch(watch)
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
                analytics.resetAnalytics()
                progress = (watchCount + 1) * progressMultiplier
            }
            onCompleteFunction(true)
        }
    }

    /**
     * Clears the app cache.
     * @param onProgressChanged The function to call when clear progress is changed.
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun clearCache(
        onProgressChanged: (progress: Int) -> Unit,
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val context = getApplication<Application>()
            val cacheFiles = (context.cacheDir.walkBottomUp() + context.codeCacheDir.walkBottomUp())
                .toList()
            if (cacheFiles.isNotEmpty()) {
                val progressMultiplier = floor(MAX_PROGRESS / cacheFiles.size).toInt()
                var progress: Int
                cacheFiles.forEachIndexed { index, file ->
                    if (!file.delete()) {
                        withContext(Dispatchers.Main) { onCompleteFunction(false) }
                        return@launch
                    }
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
            }
            withContext(Dispatchers.Main) { onCompleteFunction(true) }
        }
    }

    companion object {
        internal const val MAX_PROGRESS = 100.0
    }
}
