package com.boswelja.devicemanager.managespace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.analytics.Analytics
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
        Dispatchers.IO
    )

    init {
        watchManager.registeredWatches.observeForever(registeredWatchesObserver)
    }

    override fun onCleared() {
        super.onCleared()
        watchManager.registeredWatches.removeObserver(registeredWatchesObserver)
    }

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
                val progressMultiplier = 100.0 / cacheFiles.size
                var currentProgress: Int
                cacheFiles.forEachIndexed { index, file ->
                    Timber.d("Deleting ${file.path}")
                    val successfullyDeleted = file.delete()
                    if (!successfullyDeleted) {
                        Timber.w("Failed to delete cache file ${file.absolutePath}")
                        withContext(Dispatchers.Main) { onCompleteFunction(false) }
                        return@launch
                    }
                    currentProgress = floor((index + 1) * progressMultiplier).toInt()
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
