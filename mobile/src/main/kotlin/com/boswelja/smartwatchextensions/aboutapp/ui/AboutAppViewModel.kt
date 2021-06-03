package com.boswelja.smartwatchextensions.aboutapp.ui

import android.app.Application
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.MessageListener
import com.boswelja.watchconnection.core.Watch
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class AboutAppViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    val customTabsIntent: CustomTabsIntent,
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        CustomTabsIntent.Builder().setShowTitle(true).build()
    )

    private val selectedWatchObserver = Observer<Watch?> { watch ->
        if (watch?.id != null) {
            viewModelScope.launch {
                val result = watchManager.sendMessage(watch, REQUEST_APP_VERSION, null)
                if (result) {
                    // Successfully sent message
                    _watchAppVersion.postValue(Pair(null, null))
                } else {
                    // Failed to send message
                    _watchAppVersion.postValue(null)
                }
            }
        } else {
            Timber.w("Selected watch null")
            _watchAppVersion.postValue(null)
        }
    }

    private val messageListener = object : MessageListener {
        override fun onMessageReceived(sourceWatchId: UUID, message: String, data: ByteArray?) {
            data?.let {
                val versionInfo = parseWatchVersionInfo(it)
                _watchAppVersion.postValue(versionInfo)
            }
        }
    }

    private val _watchAppVersion = MutableLiveData<Pair<String?, String?>?>()
    val watchAppVersion: LiveData<Pair<String?, String?>?>
        get() = _watchAppVersion

    init {
        watchManager.registerMessageListener(messageListener)
        watchManager.selectedWatch.observeForever(selectedWatchObserver)
    }

    override fun onCleared() {
        super.onCleared()
        watchManager.unregisterMessageListener(messageListener)
        watchManager.selectedWatch.removeObserver(selectedWatchObserver)
    }

    /**
     * Parse watch app version info from a given ByteArray.
     * @param byteArray The [ByteArray] received from the connected watch.
     * @return A [Pair] of [String] objects containing the watch version name and version code in
     * first and second respectively.
     */
    private fun parseWatchVersionInfo(byteArray: ByteArray): Pair<String, String> {
        val data = String(byteArray, Charsets.UTF_8).split("|")
        val versionName = data[0]
        val versionCode = data[1]
        return Pair(versionName, versionCode)
    }
}
