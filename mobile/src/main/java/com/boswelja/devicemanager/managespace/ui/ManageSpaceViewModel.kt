package com.boswelja.devicemanager.managespace.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import java.io.File
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
    private val sharedPreferences: SharedPreferences,
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
        PreferenceManager.getDefaultSharedPreferences(application),
        Dispatchers.IO
    )

    init {
        watchManager.registeredWatches.observeForever(registeredWatchesObserver)
    }

    override fun onCleared() {
        super.onCleared()
        watchManager.registeredWatches.removeObserver(registeredWatchesObserver)
    }

    /**
     * Reset settings for each registered watch.
     * @param onProgressChanged The function to call when clear progress is changed.
     * @param onCompleteFunction The function to call when the job completes.
     */
    fun resetSettings(
        onProgressChanged: (progress: Int) -> Unit,
        onCompleteFunction: (isSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            analytics.logStorageManagerAction("fullReset")
            val registeredWatches = registeredWatches
            var progress: Int
            if (!registeredWatches.isNullOrEmpty()) {
                val watchCount = registeredWatches.count()
                val progressMultiplier =
                    floor(100.0 / (watchCount + SyncPreferences.ALL_PREFS.count())).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.resetWatchPreferences(watch)
                    progress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(progress) }
                }
                sharedPreferences.edit(commit = true) {
                    SyncPreferences.ALL_PREFS.forEachIndexed { index, s ->
                        remove(s)
                        progress = (watchCount + index + 1) * progressMultiplier
                    }
                }
            }
            onCompleteFunction(true)
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
            var progress: Int
            if (!registeredWatches.isNullOrEmpty()) {
                val watchCount = registeredWatches.count()
                val progressMultiplier = floor(100.0 / (watchCount + 1)).toInt()
                registeredWatches.forEachIndexed { index, watch ->
                    watchManager.requestResetWatch(watch)
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

            val cacheFiles = getFiles(getApplication<Application>().cacheDir) +
                getFiles(getApplication<Application>().codeCacheDir)
            Timber.d("Got ${cacheFiles.count()} cache files")

            if (cacheFiles.isNotEmpty()) {
                val progressMultiplier = floor(100.0 / cacheFiles.size).toInt()
                var currentProgress: Int
                cacheFiles.forEachIndexed { index, file ->
                    Timber.d("Deleting ${file.path}")
                    val successfullyDeleted = file.delete()
                    if (!successfullyDeleted) {
                        Timber.w("Failed to delete cache file ${file.absolutePath}")
                        withContext(Dispatchers.Main) { onCompleteFunction(false) }
                        return@launch
                    }
                    currentProgress = (index + 1) * progressMultiplier
                    withContext(Dispatchers.Main) { onProgressChanged(currentProgress) }
                }
            } else {
                Timber.w("Cache files null or empty")
            }
            withContext(Dispatchers.Main) { onCompleteFunction(true) }
        }
    }

    /**
     * Recursive function to get an [List] of all the files contained within the given [File].
     * @param file The [File] to return children of.
     * @return An [Array] of all the files found inside the given [File].
     */
    private fun getFiles(file: File): List<File> {
        val files = ArrayList<File>()
        if (file.isDirectory) {
            Timber.i("${file.absolutePath} is a directory")
            val innerFiles = file.listFiles()
            if (innerFiles != null && innerFiles.isNotEmpty()) {
                for (innerFile in innerFiles) {
                    files.addAll(getFiles(innerFile))
                }
            } else {
                Timber.i("${file.absolutePath} has no inner files")
            }
        } else {
            Timber.i("${file.absolutePath} is not a directory")
            files.add(file)
        }
        return files
    }
}
