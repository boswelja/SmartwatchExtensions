package com.boswelja.devicemanager.dndsync.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager

class DnDSyncPreferenceViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {
        sharedPreferences,
        key ->
        when (key) {
            DND_SYNC_TO_PHONE_KEY -> {
                syncToPhone = sharedPreferences.getBoolean(key, false)
            }
            DND_SYNC_TO_WATCH_KEY -> {
                syncToWatch = sharedPreferences.getBoolean(key, false)
            }
        }
    }

    private var syncToPhone: Boolean = sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false)
    private var syncToWatch: Boolean = sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false)

    val watchManager = WatchManager.getInstance(application)

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}
