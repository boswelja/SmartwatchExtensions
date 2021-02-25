/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.DelayedFunction
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class AppManagerViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application)
    )

    private val _state = MutableLiveData(State.CONNECTING)
    private val _appsObservable = MutableLiveData<List<App>>(emptyList())

    private val _apps = ArrayList<App>()

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            // Package change messages
            Messages.PACKAGE_ADDED -> addPackage(App.fromByteArray(it.data))
            Messages.PACKAGE_UPDATED -> updatePackage(App.fromByteArray(it.data))
            Messages.PACKAGE_REMOVED -> removePackage(App.fromByteArray(it.data))

            // Service state messages
            Messages.SERVICE_RUNNING -> serviceRunning()

            else -> Timber.w("Unknown path received, ignoring")
        }
    }

    private val stateDisconnectedDelay = DelayedFunction(15) {
        _state.postValue(State.DISCONNECTED)
    }

    /**
     * The current [State] of the App Manager.
     */
    val state: LiveData<State>
        get() = _state

    val apps: LiveData<List<App>>
        get() = _appsObservable

    var canStopAppManagerService: Boolean = true
    var watchId: String? = null

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    /** Start the App Manager service on the connected watch. */
    fun startAppManagerService() {
        Timber.i("startAppManagerService() called")
        if (_state.value != State.READY) {
            Timber.i("Trying to start App Manager service")
            messageClient.sendMessage(watchId!!, Messages.START_SERVICE, null)
        }
    }

    /** Stop the App Manager service on the connected watch. */
    fun tryStopAppManagerService() {
        Timber.i("stopAppManagerService() called")
        if (_state.value == State.READY && canStopAppManagerService) {
            Timber.i("Trying to stop App Manager service")
            messageClient.sendMessage(watchId!!, Messages.STOP_SERVICE, null)
        }
    }

    internal fun addPackage(app: App) {
        Timber.i("Adding app to list")
        _apps.add(app)
        _appsObservable.postValue(_apps)
    }

    internal fun updatePackage(app: App) {
        Timber.i("Updating app in list")
        _apps.removeAll { it.packageName == app.packageName }
        _apps.add(app)
        _appsObservable.postValue(_apps)
    }

    internal fun removePackage(app: App) {
        Timber.i("Removing app from list")
        _apps.removeAll { it.packageName == app.packageName }
        _appsObservable.postValue(_apps)
    }

    internal fun serviceRunning() {
        Timber.i("App Manager service is running")
        stateDisconnectedDelay.reset()
    }
}
