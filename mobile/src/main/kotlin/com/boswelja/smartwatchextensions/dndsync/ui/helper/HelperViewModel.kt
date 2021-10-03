package com.boswelja.smartwatchextensions.dndsync.ui.helper

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeService
import com.boswelja.smartwatchextensions.dndsync.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class HelperViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    private val _result = MutableStateFlow<Boolean?>(null)
    val result: Flow<Boolean?>
        get() = _result

    init {
        viewModelScope.launch {
            watchManager.incomingMessages().filter {
                it.path == REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH && it.data != null
            }.collect { message ->
                val hasNotiPolicyAccess = Boolean.fromByteArray(message.data!!)
                _result.tryEmit(hasNotiPolicyAccess)
                setSyncToWatch(hasNotiPolicyAccess)
            }
        }
    }

    fun requestCheckPermission() {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.sendMessage(
                selectedWatch!!,
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
            )
        }
    }

    private fun setSyncToWatch(isEnabled: Boolean) {
        Timber.i("setSyncToWatch() called")
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                PreferenceKey.DND_SYNC_TO_WATCH_KEY, isEnabled
            )
            if (isEnabled) {
                val context = getApplication<Application>()
                ContextCompat.startForegroundService(
                    context, Intent(context, DnDLocalChangeService::class.java)
                )
            }
        }
    }
}
