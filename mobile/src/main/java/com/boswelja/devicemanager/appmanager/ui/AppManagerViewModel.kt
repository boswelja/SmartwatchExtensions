package com.boswelja.devicemanager.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.DelayedFunction
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages.EXPECTED_APP_COUNT
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_UPDATED
import com.boswelja.devicemanager.common.appmanager.Messages.SERVICE_RUNNING
import com.boswelja.devicemanager.common.appmanager.Messages.START_SERVICE
import com.boswelja.devicemanager.common.appmanager.Messages.STOP_SERVICE
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

@ExperimentalUnsignedTypes
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
                PACKAGE_ADDED -> addPackage(App.fromByteArray(it.data))
                PACKAGE_UPDATED -> updatePackage(App.fromByteArray(it.data))
                PACKAGE_REMOVED -> removePackage(App.fromByteArray(it.data))

                // Service state messages
                SERVICE_RUNNING -> serviceRunning()
                EXPECTED_APP_COUNT -> expectPackages(it.data.first().toUByte())

                else -> Timber.w("Unknown path received, ignoring")
            }
        } else if (it.path == SERVICE_RUNNING) {
            // If we get SERVICE_RUNNING from any watch that's not the selected watch, stop it.
            messageClient.sendMessage(it.sourceNodeId, STOP_SERVICE, null)
        }
    }

    private val selectedWatchObserver = Observer<Watch?> {
        _state.postValue(State.CONNECTING)
        messageClient.sendMessage(watchId!!, STOP_SERVICE, null)
        _apps.clear()
        _appsObservable.postValue(_apps)
        watchId = it.id
        startAppManagerService()
    }

    private val stateDisconnectedDelay = DelayedFunction(15) {
        _state.postValue(State.DISCONNECTED)
    }

    private var watchId: String? = null
    private var expectedPackageCount: UByte = 0u

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
        Timber.d("startAppManagerService() called")
        if (_state.value != State.READY) {
            Timber.d("Trying to start App Manager service")
            messageClient.sendMessage(watchId!!, START_SERVICE, null)
            stateDisconnectedDelay.reset()
        }
    }

    /** Stop the App Manager service on the connected watch. */
    fun tryStopAppManagerService() {
        Timber.d("stopAppManagerService() called")
        if (_state.value == State.READY && canStopAppManagerService) {
            Timber.d("Trying to stop App Manager service")
            messageClient.sendMessage(watchId!!, STOP_SERVICE, null)
        }
    }

    internal fun expectPackages(count: UByte) {
        _state.postValue(State.LOADING_APPS)
        expectedPackageCount = count
    }

    internal fun addPackage(app: App) {
        Timber.d("Adding app to list")
        _apps.add(app)
        if (expectedPackageCount > 0u) {
            Timber.d("Expected a package, decrementing expectedPackageCount")
            expectedPackageCount--
            if (expectedPackageCount <= 0u) {
                Timber.d("No more expected packages, setting State to READY")
                _state.postValue(State.READY)
            }
        } else {
            Timber.d("Updating LiveData")
            _appsObservable.postValue(_apps)
        }
    }

    internal fun updatePackage(app: App) {
        Timber.d("Updating app in list")
        _apps.removeAll { it.packageName == app.packageName }
        _apps.add(app)
        _appsObservable.postValue(_apps)
    }

    internal fun removePackage(app: App) {
        Timber.d("Removing app from list")
        _apps.removeAll { it.packageName == app.packageName }
        _appsObservable.postValue(_apps)
    }

    internal fun serviceRunning() {
        Timber.d("App Manager service is running")
        stateDisconnectedDelay.reset()
    }
}
