/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.extensions.ui

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_CONNECTED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable

class ExtensionsViewModel
    @JvmOverloads
    constructor(
        application: Application,
        private val nodeClient: NodeClient = Wearable.getNodeClient(application)
    ) : AndroidViewModel(application) {

    private val phoneId by lazy { sharedPreferences.getString(PHONE_ID_KEY, "") ?: "" }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val _phoneConnected = MutableLiveData(false)
    val phoneConnected: LiveData<Boolean>
        get() = _phoneConnected

    private fun setPhoneConnected(isConnected: Boolean) {
        sharedPreferences.edit { putBoolean(PHONE_CONNECTED_KEY, isConnected) }
        _phoneConnected.postValue(isConnected)
    }

    fun updatePhoneConnectedStatus() {
        nodeClient.connectedNodes.addOnCompleteListener {
            if (it.isSuccessful && !it.result.isNullOrEmpty()) {
                val isPhoneConnected =
                    it.result!!.any { node -> node.id == phoneId && node.isNearby }
                setPhoneConnected(isPhoneConnected)
            } else {
                setPhoneConnected(false)
            }
        }
    }
}
