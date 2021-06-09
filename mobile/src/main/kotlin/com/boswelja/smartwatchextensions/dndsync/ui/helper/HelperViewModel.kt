package com.boswelja.smartwatchextensions.dndsync.ui.helper

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeService
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.MessageListener
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class HelperViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    private val messageListener = object : MessageListener {
        override fun onMessageReceived(sourceWatchId: UUID, message: String, data: ByteArray?) {
            when (message) {
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                    data?.let {
                        val hasNotiPolicyAccess = Boolean.fromByteArray(data)
                        _result.postValue(hasNotiPolicyAccess)
                        setSyncToWatch(hasNotiPolicyAccess)
                    } ?: Timber.w("Received message but no data")
                }
            }
        }
    }

    private val _result = MutableLiveData<Boolean?>(null)
    val result: LiveData<Boolean?>
        get() = _result

    init {
        watchManager.registerMessageListener(messageListener)
    }

    override fun onCleared() {
        Timber.i("onCleared() called")
        super.onCleared()
        watchManager.unregisterMessageListener(messageListener)
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
