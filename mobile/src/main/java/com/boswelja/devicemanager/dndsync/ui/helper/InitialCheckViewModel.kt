/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync.ui.helper

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_SDK_INT_PATH
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber
import java.math.BigInteger

class InitialCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val watchManager = WatchManager.get(application)
    private val watchId = watchManager.connectedWatch.value!!.id

    private val messageClient = Wearable.getMessageClient(application)
    private val messageListener =
        MessageClient.OnMessageReceivedListener {
            Timber.d("Message received")
            when (it.path) {
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                    val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                    _hasNotiPolicyAccess.postValue(hasNotiPolicyAccess)
                    if (!hasNotiPolicyAccess) checkWatchSdk()
                }
                REQUEST_SDK_INT_PATH -> {
                    val sdkInt = BigInteger(it.data).toInt()
                    Timber.i("Watch SDK = $sdkInt")
                    _hasCorrectSdkInt.postValue(sdkInt <= Build.VERSION_CODES.O)
                }
            }
        }

    private val _hasNotiPolicyAccess = MutableLiveData(false)
    val hasNotiPolicyAccess: LiveData<Boolean>
        get() = _hasNotiPolicyAccess

    private val _hasCorrectSdkInt = MutableLiveData<Boolean?>(null)
    val hasCorrectSdkInt: LiveData<Boolean?>
        get() = _hasCorrectSdkInt

    init {
        messageClient.addListener(messageListener)
        checkWatchPermissions()
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    private fun checkWatchPermissions() {
        messageClient.sendMessage(watchId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
    }

    private fun checkWatchSdk() {
        messageClient.sendMessage(watchId, REQUEST_SDK_INT_PATH, null)
    }
}
