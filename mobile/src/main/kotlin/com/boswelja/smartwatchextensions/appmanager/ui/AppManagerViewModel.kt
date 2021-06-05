package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Status
import com.boswelja.watchconnection.core.Watch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class AppManagerViewModel internal constructor(
    application: Application,
    private val appDatabase: WatchAppDatabase,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchAppDatabase.getInstance(application),
        WatchManager.getInstance(application)
    )

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    var selectedWatch: Watch? by mutableStateOf(null)
    val registeredWatches = watchManager.registeredWatches

    val allApps = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appDatabase.apps().allForWatch(watch.id)
        } ?: flow { emit(emptyList<App>()) }
    }

    val watchStatus = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flow { emit(Status.ERROR) }
    }

    val userApps = allApps.mapLatest { apps ->
        apps.filter { !it.isSystemApp }
    }

    val systemApps = allApps.mapLatest { apps ->
        apps.filter { it.isSystemApp }
    }

    init {
        viewModelScope.launch {
            watchManager.selectedWatch.collect { watch ->
                watch?.let {
                    selectedWatch = watch
                    validateCacheFor(watch)
                }
            }
        }
    }

    /**
     * Format a given date in milliseconds to the correct formet for display.
     * @param dateMillis The date in milliseconds to convert.
     * @return The formatted date string.
     */
    fun formatDate(dateMillis: Long): String = dateFormatter.format(dateMillis)

    fun selectWatchById(watchId: UUID) = watchManager.selectWatchById(watchId)

    suspend fun sendOpenRequest(app: App): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            watchManager.sendMessage(watch, REQUEST_OPEN_PACKAGE, data)
        } ?: false
    }

    suspend fun sendUninstallRequest(app: App): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            watchManager.sendMessage(watch, REQUEST_UNINSTALL_PACKAGE, data)
        } ?: false
    }

    suspend fun validateCacheFor(watch: Watch) {
        Timber.d("Validating cache for %s", watch.id)
        // Get a list of packages we have for the given watch
        val apps = appDatabase.apps().allForWatch(watch.id)
            .map { apps ->
                apps
                    .map { it.packageName }
                    .sorted()
            }
            .first()
        val result = watchManager.sendMessage(watch, VALIDATE_CACHE, apps.hashCode().toByteArray())
        if (!result) Timber.w("Failed to request cache validation")
    }
}
