package com.boswelja.smartwatchextensions.appmanager.ui

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.NotifyAppSendingComplete
import com.boswelja.smartwatchextensions.appmanager.NotifyAppSendingStart
import com.boswelja.smartwatchextensions.appmanager.AppVersion
import com.boswelja.smartwatchextensions.appmanager.AppVersions
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.appmanager.RequestValidateCache
import com.boswelja.smartwatchextensions.appmanager.WatchAppIconRepository
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.appmanager.WatchAppWithIcon
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.wearable.ext.receiveMessages
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A ViewModel for providing data to App Manager.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppManagerViewModel(
    private val appRepository: WatchAppRepository,
    private val messageClient: MessageClient,
    private val nodeClient: NodeClient,
    private val selectedWatchController: SelectedWatchController,
    private val appIconRepository: WatchAppIconRepository
) : ViewModel() {

    @OptIn(FlowPreview::class)
    private val allApps = selectedWatchController.selectedWatch
        .filterNotNull()
        .flatMapLatest { watch ->
            appRepository.getAppsFor(watch.uid)
                .map { apps ->
                    apps.map { app ->
                        val icon = appIconRepository.retrieveIconFor(
                            watch.uid,
                            app.packageName
                        )?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        }
                        WatchAppWithIcon(
                            app,
                            icon
                        )
                    }
                }
        }
        .debounce(APP_DEBOUNCE_MILLIS)

    /**
     * A [kotlinx.coroutines.flow.Flow] of user-installed apps on the currently selected watch.
     */
    val userApps = allApps
        .map { apps ->
            apps.filter { !it.isSystemApp && it.isEnabled }.sortedBy { it.label }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_KEEP_ALIVE_TIME),
            initialValue = emptyList()
        )

    /**
     * A [kotlinx.coroutines.flow.Flow] of disabled apps on the currently selected watch.
     */
    val disabledApps = allApps
        .map { apps ->
            apps.filter { !it.isEnabled }.sortedBy { it.label }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_KEEP_ALIVE_TIME),
            initialValue = emptyList()
        )

    /**
     * A [kotlinx.coroutines.flow.Flow] of system apps on the currently selected watch.
     */
    val systemApps = allApps
        .map { apps ->
            apps.filter { it.isSystemApp && it.isEnabled }.sortedBy { it.label }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_KEEP_ALIVE_TIME),
            initialValue = emptyList()
        )

    /**
     * A boolean indicating whether App Manager cache is currently being updated.
     */
    val isUpdatingCache = messageClient.receiveMessages()
        .map {
            when (it.path) {
                NotifyAppSendingComplete -> false
                NotifyAppSendingStart -> true
                else -> null
            }
        }
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_KEEP_ALIVE_TIME),
            initialValue = false
        )

    /**
     * A [kotlinx.coroutines.flow.Flow] of Boolean indicating whether the currently selected watch
     * is connected.
     */
    val isWatchConnected = selectedWatchController.selectedWatch
        .filterNotNull()
        .mapLatest { watch ->
            val connectedNodes = nodeClient.connectedNodes.await()
            val node = connectedNodes.firstOrNull { it.id == watch.uid }
            node != null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_KEEP_ALIVE_TIME),
            initialValue = true
        )

    init {
        viewModelScope.launch {
            selectedWatchController.selectedWatch
                .filterNotNull()
                .collect {
                    validateCache()
                }
        }
    }

    /**
     * Requests cache validation for the selected watch.
     */
    private suspend fun validateCache() {
        selectedWatchController.selectedWatch.first()?.let { watch ->
            // Get a list of packages we have for the given watch
            val appVersionList = appRepository.getAppVersionsFor(watch.uid)
                .map { apps -> apps.map { AppVersion(it.packageName, it.versionCode) } }
                .first()
            messageClient.sendMessage(
                watch.uid,
                RequestValidateCache,
                CacheValidationSerializer.serialize(AppVersions(appVersionList)),
            )
        }
    }

    companion object {
        /**
         * Controls the debounce time for app list updates. See [debounce].
         */
        private const val APP_DEBOUNCE_MILLIS = 250L

        /**
         * Controls the amount of time to keep stateIn Flows alive for.
         */
        private const val FLOW_KEEP_ALIVE_TIME = 500L
    }
}
