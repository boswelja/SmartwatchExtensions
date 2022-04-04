package com.boswelja.smartwatchextensions.aboutapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestAppVersion
import com.boswelja.smartwatchextensions.core.devicemanagement.Version
import com.boswelja.smartwatchextensions.core.devicemanagement.VersionSerializer
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to [AboutAppScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AboutAppViewModel(
    private val watchManager: WatchManager,
    messageClient: MessageClient
) : ViewModel() {

    private val _watchAppVersion = MutableStateFlow<Version?>(null)
    private val messageHandler = MessageHandler(VersionSerializer, messageClient)

    /**
     * Flow the current version of the app on the selected watch.
     */
    val watchAppVersion: Flow<Version?>
        get() = _watchAppVersion

    init {
        // Send app version request to selected watches
        viewModelScope.launch {
            watchManager.selectedWatch.collect { watch ->
                if (watch?.uid != null) {
                    watchManager.sendMessage(watch, RequestAppVersion, null)
                    _watchAppVersion.emit(null)
                } else {
                    _watchAppVersion.emit(null)
                }
            }
        }

        // Listen for app version responses
        viewModelScope.launch {
            messageHandler.incomingMessages()
                .collect { message -> _watchAppVersion.tryEmit(message.data) }
        }
    }
}
