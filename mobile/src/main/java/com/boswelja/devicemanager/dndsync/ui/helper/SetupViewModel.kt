/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync.ui.helper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
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
                    _hasNotiPolicyAccess.postValue(hasNotiPolicyAccess)
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
        Timber.i("onCleared() called")
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    fun requestCheckPermission() {
        messageClient.sendMessage(watchId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
    }

    fun permissionRequestHandled() {
        _hasNotiPolicyAccess.postValue(null)
    }
}
