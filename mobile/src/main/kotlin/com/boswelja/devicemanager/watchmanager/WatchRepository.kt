package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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
    private val _registeredWatches: MediatorLiveData<List<Watch>> = MediatorLiveData()
    private val _availableWatches: MediatorLiveData<List<Watch>> = MediatorLiveData()

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

        // Set up _availableWatches
        connectionManagers.values.forEach {
            _availableWatches.addSource(it.availableWatches) { newAvailableWatches ->
                Timber.d("availableWatches updated for ${it.platformIdentifier}")
                val newWatches = replaceForPlatform(
                    _availableWatches.value ?: emptyList(),
                    newAvailableWatches
                )
                _availableWatches.postValue(newWatches)
            }
        }

        // Set up _registeredWatches
        _registeredWatches.addSource(database.watchDao().getAllObservable()) { watches ->
            Timber.d("registeredWatches updated")
            val watchesWithStatus = watches.map {
                val connectionManager = it.connectionManager
                if (connectionManager != null) {
                    it.status = connectionManager.getWatchStatus(it, true)
                } else {
                    Timber.w("Platform ${it.platform} not registered")
                }
                it
            }
            _registeredWatches.postValue(watchesWithStatus)
        }
        connectionManagers.values.forEach { connectionManager ->
            _registeredWatches.addSource(connectionManager.dataChanged) {
                if (it) {
                    Timber.d("dataChanged fired for ${connectionManager.platformIdentifier}")
                    val watchesWithStatus = updateStatusForPlatform(
                        _registeredWatches.value ?: emptyList(),
                        connectionManager.platformIdentifier
                    )
                    val watches = updateCapabilitiesForPlatform(
                        watchesWithStatus,
                        connectionManager.platformIdentifier
                    )
                    _registeredWatches.postValue(watches)
                }
            }
        }
    }

    private val Watch.connectionManager: WatchConnectionInterface?
        get() = connectionManagers[this.platform]

    private val Watch.isRegistered: Boolean
        get() = registeredWatches.value?.contains(this) == true

    /**
     * Takes a list of watches from varying platforms and an additional list of watches from a
     * single platform, and replaces all watches in the original list from the same platform with
     * the new list.
     * @param existingWatches The [List] of [Watch]es from varying platforms.
     * @param newWatches The new [List] of [Watch] from a single platform.
     * @return A [List] of [Watch]es with all watches from the platform matching [newWatches]
     * replaced with [newWatches].
     */
    @VisibleForTesting
    internal fun replaceForPlatform(
        existingWatches: List<Watch>,
        newWatches: List<Watch>
    ): List<Watch> {
        newWatches.firstOrNull()?.platform?.let { platform ->
            val watchesWithoutPlatform = existingWatches.filterNot { it.platform == platform }
            // Since we've removed all watches from the platform, we don't need union.
            return watchesWithoutPlatform + newWatches
        }
        return existingWatches
    }

    /**
     * Takes a list of watches from varying platforms and updates the [Watch.Status] of all watches
     * from a specified platform.
     * @param watches The [List] of [Watch] from varying platforms.
     * @param platform The [Watch.platform] to update [Watch.Status] for.
     * @return The [List] of [Watch] with newly updated [Watch.Status]
     */
    @VisibleForTesting
    internal fun updateStatusForPlatform(watches: List<Watch>, platform: String): List<Watch> {
        val platformWatches = watches.filter { it.platform == platform }
        val connectionManager = connectionManagers[platform]
        if (connectionManager == null) {
            Timber.w("Platform $platform not registered")
            return watches
        }
        platformWatches.forEach {
            it.status = connectionManager.getWatchStatus(it, it.isRegistered)
        }
        return replaceForPlatform(watches, platformWatches)
    }

    /**
     * Takes a list of watches from varying platforms and updates [Watch.capabilities] of all
     * watches.
     * @param watches The [List] of [Watch] from varying platforms.
     * @param platform The [Watch.platform] to update [Watch.capabilities] for.
     * @return The [List] of [Watch] with newly updated [Watch.capabilities]
     */
    @VisibleForTesting
    internal fun updateCapabilitiesForPlatform(
        watches: List<Watch>,
        platform: String
    ): List<Watch> {
        val platformWatches = watches.filter { it.platform == platform }
        val connectionManager = connectionManagers[platform]
        if (connectionManager == null) {
            Timber.w("Platform $platform not registered")
            return watches
        }
        platformWatches.forEach {
            it.capabilities = connectionManager.watchCapabilities[it.id] ?: 0
        }
        return replaceForPlatform(watches, platformWatches)
    }

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
            database.forgetWatch(watch)
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
        watch.connectionManager?.updatePreferenceOnWatch(watch, key, value)

    /**
     * Send a message to a watch with the given ID.
     * @param watch The [Watch] to send a message to.
     * @param messagePath The message path to send.
     * @param data The data to send with the message, if any.
     */
    fun sendMessage(watch: Watch, messagePath: String, data: ByteArray? = null) =
        watch.connectionManager?.sendMessage(watch.id, messagePath, data)
}
