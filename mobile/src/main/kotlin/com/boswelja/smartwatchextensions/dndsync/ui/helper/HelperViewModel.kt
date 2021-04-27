package com.boswelja.smartwatchextensions.dndsync.ui.helper

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.dndsync.References
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeService
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import timber.log.Timber

class HelperViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val messageClient: MessageClient
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Wearable.getMessageClient(application)
    )

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                _result.postValue(hasNotiPolicyAccess)
                setSyncToWatch(hasNotiPolicyAccess)
            }
        }
    }

    private val _result = MutableLiveData<Boolean?>(null)
    val result: LiveData<Boolean?>
        get() = _result

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        Timber.i("onCleared() called")
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    fun requestCheckPermission() {
        watchManager.selectedWatch.value?.let {
            viewModelScope.launch {
                watchManager.sendMessage(
                    it,
                    References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                    null
                )
            }
        }
    }

    private fun setSyncToWatch(isEnabled: Boolean) {
        Timber.i("setSyncToWatch() called")
        viewModelScope.launch {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, PreferenceKey.DND_SYNC_TO_WATCH_KEY, isEnabled
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
