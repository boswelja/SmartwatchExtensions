package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_START
import com.boswelja.smartwatchextensions.appmanager.CacheValidation
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.appmanager.PackageNameSerializer
import com.boswelja.smartwatchextensions.appmanager.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.appmanager.WatchApp
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.discovery.DiscoveryClient
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A ViewModel for providing data to App Manager.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppManagerViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent {

    private val appRepository: WatchAppRepository by inject()
    private val messageClient: MessageClient by inject()
    private val discoveryClient: DiscoveryClient by inject()
    private val selectedWatchManager: SelectedWatchManager by inject()
    private val cacheMessageHandler by lazy { MessageHandler(CacheValidationSerializer, messageClient) }
    private val packageMessageHandler by lazy { MessageHandler(PackageNameSerializer, messageClient) }

    @OptIn(FlowPreview::class)
    private val allApps = selectedWatchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appRepository.getAppsFor(watch.uid)
        } ?: flowOf(emptyList())
    }.debounce(APP_DEBOUNCE_MILLIS)

    private val _isUpdatingCache = MutableStateFlow(false)

    /**
     * A boolean indicating whether App Manager cache is currently being updated.
     */
    val isUpdatingCache = _isUpdatingCache.asStateFlow()

    /**
     * A [kotlinx.coroutines.flow.Flow] of Boolean indicating whether the currently selected watch
     * is connected.
     */
    val isWatchConnected = selectedWatchManager.selectedWatch
        .filterNotNull()
        .flatMapLatest { watch ->
            discoveryClient.connectionModeFor(watch.uid)
        }.map { status ->
            status != ConnectionMode.Disconnected
        }

    /**
     * A [kotlinx.coroutines.flow.Flow] of user-installed apps on the currently selected watch.
     */
    val userApps = allApps.map { apps ->
        apps.filter { !it.isSystemApp && it.isEnabled }.sortedBy { it.label }
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] of disabled apps on the currently selected watch.
     */
    val disabledApps = allApps.map { apps ->
        apps.filter { !it.isEnabled }.sortedBy { it.label }
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] of system apps on the currently selected watch.
     */
    val systemApps = allApps.map { apps ->
        apps.filter { it.isSystemApp && it.isEnabled }.sortedBy { it.label }
    }

    init {
        viewModelScope.launch {
            launch {
                selectedWatchManager.selectedWatch
                    .filterNotNull()
                    .collect {
                        validateCache()
                    }
            }
            launch {
                messageClient.incomingMessages().collect { message ->
                    when (message.path) {
                        APP_SENDING_COMPLETE -> _isUpdatingCache.emit(false)
                        APP_SENDING_START -> _isUpdatingCache.emit(true)
                    }
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
        return selectedWatchManager.selectedWatch.first()?.let { watch ->
            packageMessageHandler.sendMessage(
                watch.uid,
                Message(
                    REQUEST_OPEN_PACKAGE,
                    app.packageName,
                    Message.Priority.HIGH
                )
            )
        } ?: false
    }

    /**
     * Requests the selected watch uninstall a given [WatchAppDetails].
     * @param app The [WatchAppDetails] to try uninstall.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendUninstallRequest(app: WatchAppDetails): Boolean {
        return selectedWatchManager.selectedWatch.first()?.let { watch ->
            appRepository.delete(app.watchId, app.packageName)
            packageMessageHandler.sendMessage(
                watch.uid,
                Message(
                    REQUEST_UNINSTALL_PACKAGE,
                    app.packageName,
                    Message.Priority.HIGH
                )
            )
        } ?: false
    }

    /**
     * Requests cache validation for the selected watch.
     */
    suspend fun validateCache() {
        selectedWatchManager.selectedWatch.first()?.let { watch ->
            // Get a list of packages we have for the given watch
            val apps = appRepository.getAppVersionsFor(watch.uid)
                .map { apps -> apps.map { it.packageName to it.updateTime } }
                .first()
            val cacheHash = CacheValidation.getHashCode(apps)
            cacheMessageHandler.sendMessage(
                watch.uid,
                Message(
                    VALIDATE_CACHE,
                    cacheHash,
                    Message.Priority.HIGH
                )
            )
        }
    }

    /**
     * Get more app details for a given watch app.
     */
    suspend fun getDetailsFor(app: WatchApp): WatchAppDetails? {
        return selectedWatchManager.selectedWatch.first()?.let {
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
