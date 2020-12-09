package com.boswelja.devicemanager.dndsync.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY

class DnDSyncPreferenceWidgetViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {
        sharedPreferences,
        key ->
        when (key) {
            DND_SYNC_TO_PHONE_KEY -> {
                syncToPhone = sharedPreferences.getBoolean(key, false)
                updateDrawableRes()
            }
            DND_SYNC_TO_WATCH_KEY -> {
                syncToWatch = sharedPreferences.getBoolean(key, false)
                updateDrawableRes()
            }
        }
    }

    private var syncToPhone: Boolean = sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false)
    private var syncToWatch: Boolean = sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false)

    private val _drawableRes = MutableLiveData(R.drawable.ic_dnd_sync_none)
    val drawableRes: LiveData<Int>
        get() = _drawableRes

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        updateDrawableRes()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun updateDrawableRes() {
        val newRes = when {
            syncToPhone && syncToWatch -> R.drawable.ic_dnd_sync_both
            syncToPhone -> R.drawable.ic_dnd_sync_to_phone
            syncToWatch -> R.drawable.ic_dnd_sync_to_watch
            else -> R.drawable.ic_dnd_sync_none
        }
        _drawableRes.postValue(newRes)
    }
}
