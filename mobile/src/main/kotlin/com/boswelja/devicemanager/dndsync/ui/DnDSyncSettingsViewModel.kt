package com.boswelja.devicemanager.dndsync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.launch

class DnDSyncSettingsViewModel(application: Application) : AndroidViewModel(application) {

    val watchManager = WatchManager.getInstance(application)
    val syncToPhone = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it, DND_SYNC_TO_PHONE_KEY)
        } ?: liveData { }
    }
    val syncToWatch = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it, DND_SYNC_TO_WATCH_KEY)
        } ?: liveData { }
    }
    val syncWithTheater = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it, DND_SYNC_WITH_THEATER_KEY)
        } ?: liveData { }
    }

    fun setSyncToPhone(isEnabled: Boolean) {
        viewModelScope.launch {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_TO_PHONE_KEY,
                isEnabled
            )
        }
    }

    fun setSyncToWatch(isEnabled: Boolean) {
        viewModelScope.launch {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_TO_WATCH_KEY,
                isEnabled
            )
        }
    }

    fun setSyncWithTheater(isEnabled: Boolean) {
        viewModelScope.launch {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                DND_SYNC_WITH_THEATER_KEY,
                isEnabled
            )
        }
    }
}
