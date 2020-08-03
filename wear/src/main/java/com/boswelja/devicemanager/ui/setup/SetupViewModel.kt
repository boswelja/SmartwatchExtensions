/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.Wearable

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val nodeClient = Wearable.getNodeClient(application)

    private val _localName = MutableLiveData<String?>(null)
    val localName: LiveData<String?>
        get() = _localName

    init {
        refreshLocalName()
    }

    private fun refreshLocalName() {
        nodeClient.localNode.addOnCompleteListener {
            _localName.postValue(it.result?.displayName)
        }
    }
}
