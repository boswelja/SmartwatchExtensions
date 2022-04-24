package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestUpdateCapabilities
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Watch Manager.
 */
class WatchManagerViewModel(
    private val watchRepository: WatchRepository,
    private val messageClient: MessageClient
) : ViewModel() {

    /**
     * Flow the list of registered watches.
     */
    val registeredWatches = watchRepository.registeredWatches

    fun forgetWatch(watch: Watch) {
        viewModelScope.launch {
            watchRepository.deregisterWatch(watch)
        }
    }

    fun syncCapabilities(watch: Watch) {
        viewModelScope.launch {
            messageClient.sendMessage(watch.uid, Message(RequestUpdateCapabilities, null))
        }
    }

    fun renameWatch(watch: Watch, newName: String) {
        viewModelScope.launch {
            watchRepository.renameWatch(watch, newName)
        }
    }
}
