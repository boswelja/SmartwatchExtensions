package com.boswelja.devicemanager.managespace

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ManageSpaceViewModel internal constructor(
    application: Application,
    private val analytics: Analytics,
    private val watchManager: WatchManager,
    private val coroutineDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private var registeredWatches: List<Watch>? = null

    private val _cacheFiles = MutableLiveData<List<File>>(emptyList())
    private val registeredWatchesObserver = Observer<List<Watch>> {
        registeredWatches = it
    }

    val cacheFiles: LiveData<List<File>>
        get() = _cacheFiles

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Analytics(),
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    init {
        watchManager.registeredWatches.observeForever(registeredWatchesObserver)

        // Fetch cache files
        viewModelScope.launch(coroutineDispatcher) {
            val cacheFiles = getFiles(application.cacheDir) + getFiles(application.codeCacheDir)
            _cacheFiles.postValue(cacheFiles)
        }
    }

    override fun onCleared() {
        super.onCleared()
        watchManager.registeredWatches.removeObserver(registeredWatchesObserver)
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
