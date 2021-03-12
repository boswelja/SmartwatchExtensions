package com.boswelja.devicemanager.dndsync.ui.helper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.fromByteArray
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val watchManager = WatchManager.getInstance(application)
    private val watchId = watchManager.selectedWatch.value!!.id

    private val messageClient = Wearable.getMessageClient(application)
    private val messageListener =
        MessageClient.OnMessageReceivedListener {
            when (it.path) {
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                    val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                    if (hasNotiPolicyAccess) onNotiPolicyAccessGranted.fire()
                }
            }
        }

    val onNotiPolicyAccessGranted = Event()

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        Timber.i("onCleared() called")
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    fun requestCheckPermission() {
        messageClient.sendMessage(watchId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
    }
}
