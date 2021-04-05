package com.boswelja.devicemanager.dndsync.ui

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.launch

class DnDSyncSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private var _syncToPhone =
        MutableLiveData(sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false))
    private var _syncToWatch =
        MutableLiveData(sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false))
    private var _syncWithTheater =
        MutableLiveData(sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false))

    val watchManager = WatchManager.getInstance(application)
    val syncToPhone: LiveData<Boolean>
        get() = _syncToPhone
    val syncToWatch: LiveData<Boolean>
        get() = _syncToWatch
    val syncWithTheater: LiveData<Boolean>
        get() = _syncWithTheater

    fun setSyncToPhone(isEnabled: Boolean) {
        viewModelScope.launch {
            _syncToPhone.postValue(isEnabled)
            sharedPreferences.edit {
                putBoolean(DND_SYNC_TO_PHONE_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_TO_PHONE_KEY,
                isEnabled
            )
        }
    }

    fun setSyncToWatch(isEnabled: Boolean) {
        viewModelScope.launch {
            _syncToWatch.postValue(isEnabled)
            sharedPreferences.edit {
                putBoolean(DND_SYNC_TO_WATCH_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_TO_WATCH_KEY,
                isEnabled
            )
        }
    }

    fun setSyncWithTheater(isEnabled: Boolean) {
        viewModelScope.launch {
            _syncToWatch.postValue(isEnabled)
            sharedPreferences.edit {
                putBoolean(DND_SYNC_WITH_THEATER_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_WITH_THEATER_KEY,
                isEnabled
            )
        }
    }
}
