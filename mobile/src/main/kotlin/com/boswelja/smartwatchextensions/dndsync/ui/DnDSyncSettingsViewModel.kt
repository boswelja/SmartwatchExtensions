package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.launch

class DnDSyncSettingsViewModel(application: Application) : AndroidViewModel(application) {

    val watchManager = WatchManager.getInstance(application)
    val syncToPhone = preferenceForSelectedWatch<Boolean>(DND_SYNC_TO_PHONE_KEY)
    val syncToWatch = preferenceForSelectedWatch<Boolean>(DND_SYNC_TO_WATCH_KEY)
    val syncWithTheater = preferenceForSelectedWatch<Boolean>(DND_SYNC_WITH_THEATER_KEY)

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

    private inline fun <reified T> preferenceForSelectedWatch(key: String): LiveData<T?> {
        return watchManager.selectedWatch.switchMap {
            it?.let { watch ->
                watchManager.getPreferenceObservable<T>(watch.id, key)
            } ?: liveData { }
        }
    }
}
