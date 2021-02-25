package com.boswelja.devicemanager.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.DelayedFunction
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class AppManagerViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application),
        WatchManager.getInstance(application)
    )

    private val _state = MutableLiveData(State.CONNECTING)
    private val _appsObservable = MutableLiveData<List<App>>(emptyList())

    private val _apps = ArrayList<App>()

    private val messageListener = MessageClient.OnMessageReceivedListener {
        if (it.sourceNodeId == watchId) {
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
    }

    private val selectedWatchObserver = Observer<Watch?> {
        _state.postValue(State.CONNECTING)
        messageClient.sendMessage(watchId!!, Messages.STOP_SERVICE, null)
        _apps.clear()
        _appsObservable.postValue(_apps)
        watchId = it.id
        startAppManagerService()
    }

    private val stateDisconnectedDelay = DelayedFunction(15) {
        _state.postValue(State.DISCONNECTED)
    }

    private var watchId: String? = null

    /**
     * The current [State] of the App Manager.
     */
    val state: LiveData<State>
        get() = _state

    val apps: LiveData<List<App>>
        get() = _appsObservable

    var canStopAppManagerService: Boolean = true

    init {
        messageClient.addListener(messageListener)
        watchManager.selectedWatch.observeForever(selectedWatchObserver)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
        watchManager.selectedWatch.removeObserver(selectedWatchObserver)
    }

    /** Start the App Manager service on the connected watch. */
    fun startAppManagerService() {
        Timber.i("startAppManagerService() called")
        if (_state.value != State.READY) {
            Timber.i("Trying to start App Manager service")
            messageClient.sendMessage(watchId!!, Messages.START_SERVICE, null)
            stateDisconnectedDelay.reset()
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
