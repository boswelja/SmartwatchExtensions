package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_START
import com.boswelja.smartwatchextensions.appmanager.CacheValidation
import com.boswelja.smartwatchextensions.appmanager.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.appmanager.WatchApp
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class AppManagerViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val appRepository: WatchAppRepository by instance()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    @OptIn(FlowPreview::class)
    private val allApps = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appRepository.getAppsFor(watch.uid)
        } ?: flowOf(emptyList())
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
        } ?: flowOf(false)
    }.map { status ->
        status != ConnectionMode.Disconnected
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
                when (message.path) {
                    APP_SENDING_COMPLETE -> isUpdatingCache = false
                    APP_SENDING_START -> isUpdatingCache = true
                }
            }
        }
    }

    /**
     * Requests the selected watch launch a given [WatchAppDetails].
     * @param app The [WatchAppDetails] to try launch.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendOpenRequest(app: WatchAppDetails): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            watchManager.sendMessage(watch, REQUEST_OPEN_PACKAGE, data)
        } ?: false
    }

    /**
     * Requests the selected watch uninstall a given [WatchAppDetails].
     * @param app The [WatchAppDetails] to try uninstall.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendUninstallRequest(app: WatchAppDetails): Boolean {
        return selectedWatch?.let { watch ->
            val data = app.packageName.toByteArray(Charsets.UTF_8)
            appRepository.delete(app.watchId, app.packageName)
            watchManager.sendMessage(watch, REQUEST_UNINSTALL_PACKAGE, data)
        } ?: false
    }

    /**
     * Requests cache validation for the selected watch.
     */
    suspend fun validateCache() {
        selectedWatch?.let { watch ->
            Timber.d("Validating cache for %s", watch.uid)
            // Get a list of packages we have for the given watch
            val apps = appRepository.getAppVersionsFor(watch.uid)
                .map { apps -> apps.map { it.packageName to it.updateTime } }
                .first()
            val cacheHash = CacheValidation.getHashCode(apps)
            val result = watchManager.sendMessage(watch, Message(VALIDATE_CACHE, cacheHash))
            if (!result) Timber.w("Failed to request cache validation")
        }
    }

    suspend fun getDetailsFor(app: WatchApp): WatchAppDetails? {
        return selectedWatch?.let {
            appRepository.getDetailsFor(it.uid, app.packageName).first()
        }
    }

    companion object {
        /**
         * Controls the debounce time for app list updates. See [debounce].
         */
        private const val APP_DEBOUNCE_MILLIS = 250L
    }
}
