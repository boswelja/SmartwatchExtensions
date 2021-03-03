package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.boswelja.devicemanager.common.connection.Messages
import com.boswelja.devicemanager.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.connection.WatchConnectionInterface
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Preference
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A repository to handle collecting data from and passing requests to the appropriate
 * [WatchConnectionInterface].
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
            connectionManagers[it.platformIdentifier] = it
        }

        // Set up _availableWatches
        connectionManagers.values.forEach {
            _availableWatches.addSource(it.availableWatches) { newAvailableWatches ->
                val newWatches = replaceForPlatform(
                    _availableWatches.value ?: emptyList(),
                    newAvailableWatches
                )
                _availableWatches.postValue(newWatches)
            }
        }

        // Set up _registeredWatches
        _registeredWatches.addSource(database.watchDao().getAllObservable()) { watches ->
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
            val connectionManager = watch.connectionManager
            connectionManager?.sendMessage(watch.id, WATCH_REGISTERED_PATH)
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
            watch.connectionManager?.resetWatchApp(watch)
            database.forgetWatch(watch)
        }
    }

    /**
     * Calls [WatchConnectionInterface.resetWatchPreferences] on the corresponding
     * [WatchConnectionInterface].
     * @param watch The [Watch] to perform the request on.
     */
    suspend fun resetWatchPreferences(watch: Watch) {
        withContext(Dispatchers.IO) {
            watch.connectionManager?.resetWatchPreferences(watch)
            database.clearWatchPreferences(watch)
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

    /**
     * Sends [Messages.REQUEST_UPDATE_CAPABILITIES] to a given [Watch].
     * @param watch The [Watch] to request capabilities be updated for.
     */
    fun requestRefreshCapabilities(watch: Watch) {
        watch.connectionManager?.sendMessage(watch.id, Messages.REQUEST_UPDATE_CAPABILITIES)
    }

    /**
     * Gets all preferences stored for a given [Watch].
     * @param watch The [Watch] to get preferences for.
     * @return A [List] of [Preference]s for the given watch.
     */
    suspend fun getAllPreferences(watch: Watch): List<Preference<Any>> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAllPreferences(watch)
        }
    }

    /**
     * Notify the watch a specified preference has been changed, and updates the preference in the
     * database.
     * @param watch The target [Watch].
     * @param key The preference key to send to the watch.
     * @param value The new value of the preference.
     */
    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        withContext(Dispatchers.IO) {
            if (key in SyncPreferences.ALL_PREFS) {
                database.updatePrefInDatabase(watch.id, key, value)
                watch.connectionManager?.updatePreferenceOnWatch(watch, key, value)
            } else {
                Timber.w("Tried to update a non-synced preference")
            }
        }
    }

    /**
     * Gets a preference for a given watch with a specified key.
     * @param watch The [Watch] to get the preference for.
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    suspend inline fun <reified T> getPreference(watch: Watch, key: String): T? {
        return withContext(Dispatchers.IO) {
            return@withContext database.getPreference<T>(watch, key)?.value
        }
    }
}
