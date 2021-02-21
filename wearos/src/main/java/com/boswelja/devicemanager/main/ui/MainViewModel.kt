/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.main.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    val isRegistered: Boolean
        get() = !sharedPreferences.getString(PHONE_ID_KEY, "").isNullOrBlank()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            CapabilityUpdater(application).updateCapabilities()
        }
    }
}
