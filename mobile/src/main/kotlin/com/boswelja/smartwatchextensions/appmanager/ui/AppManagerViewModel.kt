package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.CacheValidation
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_SENDING_START
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.discovery.Status
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(FlowPreview::class)
    private val allApps = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appDatabase.apps().allForWatch(watch.id)
        } ?: flow { emit(emptyList<App>()) }
    }.debounce(APP_DEBOUNCE_MILLIS)

    /**
     * The currently selected watch, or null if there is none
     */
    var selectedWatch: Watch? by mutableStateOf(null)
        private set

    /**
     * A boolean indicating whether App Manager cache is currently being updated.
     */
    var isUpdatingCache by mutableStateOf(false)
        private set

    /**
     * See [WatchManager.registeredWatches].
     */
    val registeredWatches = watchManager.registeredWatches

    /**
     * A [kotlinx.coroutines.flow.Flow] of Boolean indicating whether the currently selected watch
     * is connected.
     */
    val isWatchConnected = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flow { emit(Status.ERROR) }
    }.map { status ->
        status == Status.CONNECTING ||
            status == Status.CONNECTED ||
            status == Status.CONNECTED_NEARBY
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] of user-installed apps on the currently selected watch.
     */
    val userApps = allApps.mapLatest { apps ->
        apps.filter { !it.isSystemApp && it.isEnabled }.sortedBy { it.label }
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] of disabled apps on the currently selected watch.
     */
    val disabledApps = allApps.mapLatest { apps ->
        apps.filter { !it.isEnabled }.sortedBy { it.label }
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] of system apps on the currently selected watch.
     */
    val systemApps = allApps.mapLatest { apps ->
        apps.filter { it.isSystemApp && it.isEnabled }.sortedBy { it.label }
    }

    init {
        // Re-validate cache when selected watch changes
        viewModelScope.launch {
            watchManager.selectedWatch.collect { watch ->
                watch?.let {
                    selectedWatch = watch
                    validateCache()
                }
            }
        }

        // Collect incoming messages
        viewModelScope.launch {
            watchManager.incomingMessages().collect { message ->
                when (message.message) {
                    APP_SENDING_COMPLETE -> isUpdatingCache = false
                    APP_SENDING_START -> isUpdatingCache = true
                }
            }
        }
    }

    /**
     * See [WatchManager.selectWatchById].
     */
    fun selectWatchById(watchId: UUID) = watchManager.selectWatchById(watchId)

    /**
     * Requests the selected watch launch a given [App].
     * @param app The [App] to try launch.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendOpenRequest(app: App): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            watchManager.sendMessage(watch, REQUEST_OPEN_PACKAGE, data)
        } ?: false
    }

    /**
     * Requests the selected watch uninstall a given [App].
     * @param app The [App] to try uninstall.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendUninstallRequest(app: App): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            appDatabase.apps().remove(app)
            watchManager.sendMessage(watch, REQUEST_UNINSTALL_PACKAGE, data)
        } ?: false
    }

    /**
     * Requests cache validation for the selected watch.
     */
    suspend fun validateCache() {
        selectedWatch?.let { watch ->
            Timber.d("Validating cache for %s", watch.id)
            // Get a list of packages we have for the given watch
            val apps = appDatabase.apps().allForWatch(watch.id)
                .map { apps -> apps.map { it.packageName to it.lastUpdateTime } }
                .first()
            val cacheHash = CacheValidation.getHashCode(apps)
            val result = watchManager.sendMessage(watch, VALIDATE_CACHE, cacheHash.toByteArray())
            if (!result) Timber.w("Failed to request cache validation")
        }
    }

    companion object {
        /**
         * Controls the debounce time for app list updates. See [debounce].
         */
        private const val APP_DEBOUNCE_MILLIS = 250L
    }
}
