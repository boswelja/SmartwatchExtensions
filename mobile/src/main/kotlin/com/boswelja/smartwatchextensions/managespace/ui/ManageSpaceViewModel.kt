package com.boswelja.smartwatchextensions.managespace.ui

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appsettings.AppSettingsSerializer
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.appsettings.appSettingsStore
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import kotlin.math.floor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ManageSpaceViewModel internal constructor(
    application: Application,
    private val analytics: Analytics,
    private val watchManager: WatchManager,
    private val appSettingsDataStore: DataStore<Settings>,
    private val coroutineDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private var registeredWatches: List<Watch>? = null

    private val registeredWatchesObserver = Observer<List<Watch>> {
        registeredWatches = it
    }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Analytics(),
        WatchManager.getInstance(application),
        application.appSettingsStore,
        Dispatchers.IO
    )

    init {
        watchManager.registeredWatches.observeForever(registeredWatchesObserver)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        super.onCleared()
        watchManager.registeredWatches.removeObserver(registeredWatchesObserver)
    }

    /**
     * Reset analytics storage. See [Analytics.resetAnalytics].
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetAnalytics(
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            analytics.logStorageManagerAction("analyticsReset")
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
            analytics.logStorageManagerAction("fullReset")
            val registeredWatches = registeredWatches
            if (!registeredWatches.isNullOrEmpty()) {
                var progress: Int
                val watchCount = registeredWatches.count()
                val progressMultiplier =
                    floor(MAX_PROGRESS / watchCount).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.resetWatchPreferences(getApplication<Application>(), watch)
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
            }
            onCompleteFunction(true)
        }
    }

    /**
     * Reset app settings for Wearable Extensions. This does not include extension settings.
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
            analytics.logStorageManagerAction("fullReset")
            val registeredWatches = registeredWatches
            if (!registeredWatches.isNullOrEmpty()) {
                val watchCount = registeredWatches.count()
                var progress: Int
                val progressMultiplier = floor(MAX_PROGRESS / (watchCount + 1)).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.forgetWatch(getApplication<Application>(), watch)
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
            analytics.logStorageManagerAction("clearCache")
            val context = getApplication<Application>()
            val cacheFiles = (context.cacheDir.walkBottomUp() + context.codeCacheDir.walkBottomUp())
                .toList()
            Timber.d("Got ${cacheFiles.count()} cache files")
            if (cacheFiles.isNotEmpty()) {
                val progressMultiplier = floor(MAX_PROGRESS / cacheFiles.size).toInt()
                var progress: Int
                cacheFiles.forEachIndexed { index, file ->
                    Timber.d("Deleting ${file.path}")
                    if (!file.delete()) {
                        Timber.w("Failed to delete cache file ${file.absolutePath}")
                        withContext(Dispatchers.Main) { onCompleteFunction(false) }
                        return@launch
                    }
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
            } else {
                Timber.w("Cache files null or empty")
            }
            withContext(Dispatchers.Main) { onCompleteFunction(true) }
        }
    }

    companion object {
        internal const val MAX_PROGRESS = 100.0
    }
}
