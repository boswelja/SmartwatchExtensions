/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.appinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class AppInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val messageClient = Wearable.getMessageClient(application)
    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            References.REQUEST_APP_VERSION -> {
                Timber.i("Got watch app version")
                val versionInfo = parseWatchVersionInfo(it.data)
                _watchAppVersion.postValue(versionInfo)
            }
        }
    }

    private val _watchAppVersion = MutableLiveData<Pair<String, String?>?>()
    val watchAppVersion: LiveData<Pair<String, String?>?>
        get() = _watchAppVersion

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    /**
     * Requests the current app version info from the connected watch.
     * Result received in [messageListener] if sending the message was successful.
     */
    fun requestUpdateWatchVersion(connectedWatchId: String) {
        Timber.d("requestUpdateWatchVersionPreference")
        if (connectedWatchId.isNotEmpty()) {
            messageClient.sendMessage(connectedWatchId, References.REQUEST_APP_VERSION, null)
                .addOnFailureListener {
                    Timber.w(it)
                    _watchAppVersion.postValue(null)
                }
                .addOnSuccessListener {
                    Timber.i("Message sent successfully")
                    _watchAppVersion.postValue(
                        Pair(
                            getApplication<Application>().getString(R.string.pref_about_watch_version_loading),
                            null
                        )
                    )
                }
        } else {
            Timber.w("connectedWatchId null or empty")
            _watchAppVersion.postValue(null)
        }
    }

    /**
     * Parse watch app version info from a given ByteArray.
     * @param byteArray The [ByteArray] received from the connected watch.
     * @return A [Pair] of [String] objects containing the watch version name and version code in
     * first and second respectively.
     */
    private fun parseWatchVersionInfo(byteArray: ByteArray): Pair<String, String> {
        val data = String(byteArray, Charsets.UTF_8).split("|")
        val versionString = getApplication<Application>()
            .getString(R.string.pref_about_watch_version_title, data[0])
        return Pair(versionString, data[1])
    }
}
