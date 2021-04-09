package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.common.connection.Messages.RESET_APP
import com.boswelja.devicemanager.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.watchmanager.connection.WatchConnectionInterface
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A repository to handle both registered and available watches, as well as propagating requests to
 * the correct [WatchConnectionInterface].
 */
class WatchRepository internal constructor(
    val database: WatchDatabase,
    vararg connectionInterfaces: WatchConnectionInterface
) {

    constructor(context: Context) : this(
        WatchDatabase.getInstance(context),
        WearOSConnectionInterface(context)
    )

    private val connectionManagers = HashMap<String, WatchConnectionInterface>()
    private val _registeredWatches = MutableWatchList()
    private val _availableWatches = MutableWatchList()

    /**
     * An observable list of watches registered in the database, saturated with statuses.
     */
    val registeredWatches: LiveData<List<Watch>>
        get() = _registeredWatches

    /**
     * A list of all available watches, saturated with statuses.
     */
    val availableWatches: LiveData<List<Watch>>
        get() = _availableWatches

    init {
        Timber.d("Creating new repository")

        // Create a map of platforms to WatchConnectionInterface
        connectionInterfaces.forEach {
            Timber.d("Initializing ${it.platformIdentifier}")
            connectionManagers[it.platformIdentifier] = it
        }

        // Set up _registeredWatches
        _registeredWatches.addSource(database.getAll()) { watches ->
            Timber.d("registeredWatches updated")
            val watchesWithStatus = watches.map {
                val connectionManager = it.connectionManager
                it.status = connectionManager?.getWatchStatus(it.id, true)
                    ?: Watch.Status.ERROR
                it.capabilities = connectionManager?.watchCapabilities?.get(it.id) ?: 0
                it
            }
            _registeredWatches.postValue(watchesWithStatus)
        }

        // Set up connection manager observers
        connectionManagers.values.forEach {
            _availableWatches.addSource(it.availableWatches) { newAvailableWatches ->
                Timber.d("availableWatches updated for ${it.platformIdentifier}")
                _availableWatches.postValueForPlatform(newAvailableWatches)
            }
            _registeredWatches.addSource(it.dataChanged) { dataChanged ->
                if (dataChanged) {
                    Timber.d("dataChanged fired for ${it.platformIdentifier}")
                    val platformWatches = ArrayList<Watch>()
                    (_registeredWatches.value ?: emptyList()).forEach { watch ->
                        if (watch.platform == it.platformIdentifier) {
                            watch.status = it.getWatchStatus(watch.id, true)
                            watch.capabilities = it.watchCapabilities[watch.id] ?: 0
                            platformWatches.add(watch)
                        }
                    }
                    _registeredWatches.postValueForPlatform(platformWatches)
                }
            }
        }
    }

    private val Watch.connectionManager: WatchConnectionInterface?
        get() = connectionManagers[this.platform]

    /**
     * Register a given watch, and let it know it's been registered.
     * @param watch The [Watch] to register.
     */
    suspend fun registerWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            database.addWatch(watch)
            watch.connectionManager?.sendMessage(watch.id, WATCH_REGISTERED_PATH)
        }
    }

    /**
     * Changes a watches stored name.
     * @param watch The [Watch] to rename.
     * @param newName The new name to set for the watch.
     */
    suspend fun renameWatch(watch: Watch, newName: String) {
        withContext(Dispatchers.IO) {
            database.renameWatch(watch, newName)
        }
    }

    /**
     * Removes a given [Watch] from the database, and sends the app reset request.
     * @param watch The [Watch] to perform the request on.
     */
    suspend fun resetWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            watch.connectionManager?.sendMessage(watch.id, RESET_APP)
            database.removeWatch(watch)
        }
    }

    /**
     * Requests all [WatchConnectionInterface]s update their available watches, as well as watch
     * statuses.
     */
    fun refreshData() {
        connectionManagers.values.forEach {
            it.refreshData()
        }
    }

    fun updatePreferenceOnWatch(watch: Watch, key: String, value: Any) =
        watch.connectionManager?.updatePreferenceOnWatch(watch.id, key, value)

    /**
     * Send a message to a watch with the given ID.
     * @param watch The [Watch] to send a message to.
     * @param messagePath The message path to send.
     * @param data The data to send with the message, if any.
     */
    fun sendMessage(watch: Watch, messagePath: String, data: ByteArray? = null) =
        watch.connectionManager?.sendMessage(watch.id, messagePath, data)
}
