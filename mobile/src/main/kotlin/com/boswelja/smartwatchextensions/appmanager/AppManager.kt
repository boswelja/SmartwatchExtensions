package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages
import com.boswelja.smartwatchextensions.common.appmanager.Messages.START_SERVICE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.STOP_SERVICE
import com.boswelja.smartwatchextensions.common.fromByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.MessageListener
import com.boswelja.watchconnection.core.Watch
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Class for handling the connection with an App Manager service on a connected watch.
 */
@ExperimentalCoroutinesApi
class AppManager internal constructor(
    private val watchManager: WatchManager,
    dispatcher: CoroutineDispatcher
) {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(dispatcher + coroutineJob)

    constructor(context: Context) : this(
        WatchManager.getInstance(context),
        Dispatchers.IO
    )

    private val _state = MutableLiveData(State.CONNECTING)
    private val _userAppsObservable = MutableLiveData<List<App>>(emptyList())
    private val _systemAppsObservable = MutableLiveData<List<App>>(emptyList())
    private val _progress = MutableLiveData(-1)

    private val _userApps = ArrayList<App>()
    private val _systemApps = ArrayList<App>()

    private var watch: Watch? = null
    private var expectedPackageCount: Int = 0

    /**
     * The current [State] of the App Manager.
     */
    val state: LiveData<State>
        get() = _state

    /**
     * A [List] of all user-installed [App]s found on the selected watch.
     */
    val userApps: LiveData<List<App>>
        get() = _userAppsObservable

    /**
     * A [List] of all system [App]s found on the selected watch.
     */
    val systemApps: LiveData<List<App>>
        get() = _systemAppsObservable

    /**
     * The progress of any ongoing operations, or -1 if progress can't be reported.
     */
    val progress: LiveData<Int>
        get() = _progress

    private val messageListener = object : MessageListener {
        override fun onMessageReceived(sourceWatchId: UUID, message: String, data: ByteArray?) {
            if (sourceWatchId == watch?.id && _state.value != State.ERROR) {
                try {
                    when (message) {
                        // Package change messages
                        Messages.PACKAGE_ADDED -> data?.let {
                            addPackage(App.fromByteArray(data))
                        }
                        Messages.PACKAGE_UPDATED -> data?.let {
                            updatePackage(App.fromByteArray(data))
                        }
                        Messages.PACKAGE_REMOVED -> data?.let {
                            removePackage(String(data, Charsets.UTF_8))
                        }

                        // Service state messages
                        Messages.EXPECTED_APP_COUNT -> data?.let {
                            expectPackages(Int.fromByteArray(data))
                        }

                        STOP_SERVICE -> {
                            _state.postValue(State.STOPPED)
                        }
                        else -> Timber.w("Unknown path received, ignoring")
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    _state.postValue(State.ERROR)
                }
            }
        }
    }

    private val selectedWatchObserver = Observer<Watch?> {
        if (it?.id != null && it.id != watch?.id) {
            Timber.d("selectedWatch changed, reconnecting App Manager service")
            _state.postValue(State.CONNECTING)
            _progress.postValue(-1)
            watch?.let { watch ->
                coroutineScope.launch {
                    watchManager.sendMessage(watch, STOP_SERVICE, null)
                }
            }
            watch = it
            startAppManagerService()
        }
    }

    init {
        watchManager.registerMessageListener(messageListener)
        watchManager.selectedWatch.observeForever(selectedWatchObserver)
    }

    internal fun expectPackages(count: Int) {
        _state.postValue(State.LOADING_APPS)
        _progress.postValue(0)
        expectedPackageCount = count
    }

    internal fun addPackage(app: App) {
        Timber.d("Adding app to list")
        if (app.isSystemApp) {
            _systemApps.add(app)
        } else {
            _userApps.add(app)
        }
        if ((_systemApps.count() + _userApps.count()) >= expectedPackageCount) {
            Timber.d("Got all expected packages, updating LiveData")
            _progress.postValue(-1)
            _userAppsObservable.postValue(_userApps)
            _systemAppsObservable.postValue(_systemApps)
        } else {
            val progress = (
                ((_systemApps.count() + _userApps.count()) / expectedPackageCount.toFloat()) * 100
                ).toInt()
            Timber.d("Progress $progress")
            _progress.postValue(progress)
        }
    }

    internal fun updatePackage(app: App) {
        Timber.d("Updating app in list")
        _userApps.removeAll { it.packageName == app.packageName }
        _systemApps.removeAll { it.packageName == app.packageName }
        if (app.isSystemApp) {
            _systemApps.add(app)
            _systemAppsObservable.postValue(_systemApps)
        } else {
            _userApps.add(app)
            _userAppsObservable.postValue(_userApps)
        }
    }

    internal fun removePackage(packageName: String) {
        Timber.d("Removing app from list")
        val wasUser = _userApps.removeAll { it.packageName == packageName }
        val wasSystem = _systemApps.removeAll { it.packageName == packageName }
        if (wasUser) _userAppsObservable.postValue(_userApps)
        if (wasSystem) _systemAppsObservable.postValue(_systemApps)
    }

    /** Start the App Manager service on the connected watch. */
    fun startAppManagerService() {
        Timber.d("startAppManagerService() called")
        watch?.let {
            coroutineScope.launch {
                _state.postValue(State.CONNECTING)
                _userApps.clear()
                _systemApps.clear()
                _userAppsObservable.postValue(_userApps)
                _systemAppsObservable.postValue(_systemApps)
                watchManager.sendMessage(it, START_SERVICE, null)
            }
        }
    }

    /** Stop the App Manager service on the connected watch. */
    fun stopAppManagerService() {
        Timber.d("stopAppManagerService() called")
        watch?.let {
            coroutineScope.launch {
                watchManager.sendMessage(it, STOP_SERVICE, null)
            }
        }
    }

    /**
     * Request uninstalling an app from the connected watch.
     * @param app The [App] to request uninstall for.
     */
    fun sendUninstallRequestMessage(app: App) {
        watch?.let {
            coroutineScope.launch {
                watchManager.sendMessage(
                    it,
                    Messages.REQUEST_UNINSTALL_PACKAGE,
                    app.packageName.toByteArray(Charsets.UTF_8)
                )
            }
        }
    }

    /**
     * Request opening an app's launch activity on the connected watch.
     * @param app The [App] to request open for.
     */
    fun sendOpenRequestMessage(app: App) {
        watch?.let {
            coroutineScope.launch {
                watchManager.sendMessage(
                    it,
                    Messages.REQUEST_OPEN_PACKAGE,
                    app.packageName.toByteArray(Charsets.UTF_8)
                )
            }
        }
    }

    fun destroy() {
        coroutineJob.cancel()
        watchManager.unregisterMessageListener(messageListener)
        watchManager.selectedWatch.removeObserver(selectedWatchObserver)
        stopAppManagerService()
    }
}
