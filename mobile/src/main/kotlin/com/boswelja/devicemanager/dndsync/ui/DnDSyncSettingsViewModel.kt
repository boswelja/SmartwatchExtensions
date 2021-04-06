package com.boswelja.devicemanager.dndsync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.launch

class DnDSyncSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private var _syncToPhone = MutableLiveData<Boolean>()
    private var _syncToWatch = MutableLiveData<Boolean>()
    private var _syncWithTheater = MutableLiveData<Boolean>()

    val watchManager = WatchManager.getInstance(application)
    val syncToPhone: LiveData<Boolean>
        get() = _syncToPhone
    val syncToWatch: LiveData<Boolean>
        get() = _syncToWatch
    val syncWithTheater: LiveData<Boolean>
        get() = _syncWithTheater

    init {
        watchManager.selectedWatch.value?.let { selectedWatch ->
            viewModelScope.launch {
                _syncToPhone.postValue(
                    watchManager.getPreference(selectedWatch, DND_SYNC_TO_PHONE_KEY)
                )
                _syncToWatch.postValue(
                    watchManager.getPreference(selectedWatch, DND_SYNC_TO_WATCH_KEY)
                )
                _syncWithTheater.postValue(
                    watchManager.getPreference(selectedWatch, DND_SYNC_WITH_THEATER_KEY)
                )
            }
        }
    }

    fun setSyncToPhone(isEnabled: Boolean) {
        viewModelScope.launch {
            _syncToPhone.postValue(isEnabled)
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
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_WITH_THEATER_KEY,
                isEnabled
            )
        }
    }
}
