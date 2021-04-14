package com.boswelja.smartwatchextensions.aboutapp.ui

import android.app.Application
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class AboutAppViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val watchManager: WatchManager,
    val customTabsIntent: CustomTabsIntent,
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application),
        WatchManager.getInstance(application),
        CustomTabsIntent.Builder().setShowTitle(true).build()
    )

    private val selectedWatchObserver = Observer<Watch?> { watch ->
        if (!watch?.id.isNullOrBlank()) {
            messageClient
                .sendMessage(watch.id, REQUEST_APP_VERSION, null)
                .addOnFailureListener {
                    Timber.w(it)
                    _watchAppVersion.postValue(null)
                }
                .addOnSuccessListener {
                    Timber.i("Message sent successfully")
                    _watchAppVersion.postValue(Pair(null, null))
                }
        } else {
            Timber.w("Selected watch null")
            _watchAppVersion.postValue(null)
        }
    }

    private val messageListener =
        MessageClient.OnMessageReceivedListener {
            when (it.path) {
                REQUEST_APP_VERSION -> {
                    Timber.i("Got watch app version")
                    val versionInfo = parseWatchVersionInfo(it.data)
                    _watchAppVersion.postValue(versionInfo)
                }
            }
        }

    private val _watchAppVersion = MutableLiveData<Pair<String?, String?>?>()
    val watchAppVersion: LiveData<Pair<String?, String?>?>
        get() = _watchAppVersion

    init {
        messageClient.addListener(messageListener)
        watchManager.selectedWatch.observeForever(selectedWatchObserver)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
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
