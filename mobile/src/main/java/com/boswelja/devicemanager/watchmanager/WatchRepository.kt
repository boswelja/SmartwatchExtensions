package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.watchmanager.communication.WatchConnectionInterface
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A repository to handle passing requests on to the appropriate connection manager, and collect
 * data from the connection managers.
 */
class WatchRepository(
    private val context: Context,
    private val database: WatchDatabase
) {

    constructor(context: Context) : this(context, WatchDatabase.get(context))

    private val connectionManagers = HashMap<String, WatchConnectionInterface>()

    /**
     * An observable list of watches registered in the database.
     */
    val registeredWatches: LiveData<List<Watch>>
        get() = database.watchDao().getAllObservable()

    /**
     * A list of all available watches, saturated with watch status already.
     */
    private val _availableWatches: MediatorLiveData<List<Watch>> = MediatorLiveData()
    val availableWatches: LiveData<List<Watch>>
        get() = _availableWatches

    init {
        Timber.d("Creating new repository")
        // Create Wear OS connection manager
        val wearOS = WearOSConnectionManager(context)
        connectionManagers[wearOS.getPlatformIdentifier()] = wearOS

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
    }

    private val Watch.connectionManager: WatchConnectionInterface?
        get() = connectionManagers[this.platform]

    private val Watch.isRegistered: Boolean
        get() = registeredWatches.value?.contains(this) == true

    /**
     * Takes a list of watches from varying platforms and an additional list of watcches from a
     * single platform, and replaces all watches in the original list from the same platform with
     * the new list.
     * @param existingWatches The [List] of [Watch]es from varying platforms.
     * @param newWatches The new [List] of [Watch] from a single platform.
     * @return A [List] of [Watch]es with all watches from the platform matching [newWatches]
     * replaced with [newWatches].
     */
    private fun replaceForPlatform(
        existingWatches: List<Watch>,
        newWatches: List<Watch>
    ): List<Watch>? {
        newWatches.firstOrNull()?.platform?.let { platform ->
            val watchesWithoutPlatform = existingWatches.filterNot { it.platform == platform }
            // Since we've removed all watches from the platform, we don't need union.
            return watchesWithoutPlatform + newWatches
        }
        return null
    }

    private fun updateStatusForPlatform(watches: List<Watch>, platform: String): List<Watch> {
        val platformWatches = watches.filter { it.platform == platform }
        val connectionManager = connectionManagers[platform]
        if (connectionManager == null) {
            Timber.w("Platform $platform not registered")
            return watches
        }
        platformWatches.forEach {
            it.status = connectionManager.getWatchStatus(it, it.isRegistered)
        }
        return replaceForPlatform(watches, platformWatches)!!
    }

    /**
     * Register a given watch, and let it know it's been registered.
     * @param watch The [Watch] to register.
     */
    suspend fun registerWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            database.watchDao().add(watch)
            val connectionManager = watch.connectionManager
            connectionManager?.sendMessage(watch.id, References.WATCH_REGISTERED_PATH)
        }
    }

    /**
     * Removes a given watch from the database, and lets it know it's been removed.
     * @param watch The [Watch] to remove.
     */
    suspend fun forgetWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            database.watchDao().remove(watch.id)
            resetWatch(watch)
        }
    }

    /**
     * Sends the app request message to a given watch.
     * @param watch The [Watch] to reset Wearable Extensions on.
     */
    fun resetWatch(watch: Watch) {
        watch.connectionManager?.sendMessage(watch.id, REQUEST_RESET_APP)
    }
}
