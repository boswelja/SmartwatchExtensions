/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync.helper

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val watchManager = WatchManager.get(application)
    private val watchId = watchManager.connectedWatch.value!!.id

    private val messageClient = Wearable.getMessageClient(application)
    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                _hasNotiPolicyAccess.postValue(hasNotiPolicyAccess)
                if (hasNotiPolicyAccess) enableSyncToWatch()
            }
        }
    }

    private val _hasNotiPolicyAccess = MutableLiveData<Boolean?>(null)
    val hasNotiPolicyAccess: LiveData<Boolean?>
        get() = _hasNotiPolicyAccess

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
        coroutineJob.cancel()
    }

    private fun enableSyncToWatch() {
        coroutineScope.launch {
            sharedPreferences.edit(commit = true) { putBoolean(DND_SYNC_TO_WATCH_KEY, true) }
            watchManager.updatePreferenceOnWatch(DND_SYNC_TO_WATCH_KEY)
        }
    }

    fun requestCheckPermission() {
        messageClient.sendMessage(watchId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
    }

    fun permissionRequestHandled() {
        _hasNotiPolicyAccess.postValue(null)
    }
}
