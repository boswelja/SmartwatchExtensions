/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for managing registered watches, finding new watches and updating
 * local SharedPreferences when the connected watch changes.
 */
class WatchManager
    internal constructor(
        context: Context,
        private val watchPreferenceManager: WatchPreferenceManager =
            WatchPreferenceManager.get(context),
        private val selectedWatchHandler: SelectedWatchHandler = SelectedWatchHandler.get(context),
        private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context),
        private val nodeClient: NodeClient = Wearable.getNodeClient(context),
        private val messageClient: MessageClient = Wearable.getMessageClient(context),
        val database: WatchDatabase = WatchDatabase.get(context)
    ) {

    val connectedWatch: LiveData<Watch?>
        get() = selectedWatchHandler.selectedWatch

    init {
        connectedWatch.observeForever {
            it?.let { watch -> watchPreferenceManager.updateLocalPreferences(watch.id) }
        }
    }

    /**
     * Gets the status of a specified [Watch].
     * @param watchId The [Watch.id] to find a [WatchStatus] for.
     * @param capableNodes The [Set] of capable [Node] objects to check against. Default is null.
     * @param connectedNodes The [List] of connected [Node] objects to check against. Default is
     * null.
     * @return A [WatchStatus] for the [Watch].
     */
    internal fun getWatchStatus(
        watchId: String, capableNodes: Set<Node>? = null, connectedNodes: List<Node>? = null
    ): WatchStatus {
        val isCapable = capableNodes?.any { it.id == watchId } ?: false
        val isConnected = connectedNodes?.any { it.id == watchId } ?: false
        val isRegistered = database.watchDao().get(watchId) != null
        return when {
            isCapable && isConnected && isRegistered -> WatchStatus.CONNECTED
            isCapable && !isConnected && isRegistered -> WatchStatus.DISCONNECTED
            isCapable && !isRegistered -> WatchStatus.NOT_REGISTERED
            !isCapable && !isRegistered -> WatchStatus.MISSING_APP
            else -> WatchStatus.ERROR
        }
    }

    /**
     * Get a [List] of connected [Node], regardless of capability.
     * @return The [List] of connected [Node], or null if the task failed.
     */
    internal suspend fun getConnectedNodes(): List<Node>? {
        Timber.d("getConnectedNodes() called")
        return try {
            withContext(Dispatchers.IO) { Tasks.await(nodeClient.connectedNodes) }
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }

    /**
     * Gets the [Set] of capable [Node]. Each [Node] declares [References.CAPABILITY_WATCH_APP], and
     * is reachable at the time of checking.
     * @return The [Set] of capable [Node], or null if the task failed.
     */
    internal suspend fun getCapableNodes(): Set<Node>? {
        Timber.d("getCapableNodes() called")
        var capableNodes: Set<Node>? = null
        try {
            withContext(Dispatchers.IO) {
                capableNodes =
                    Tasks.await(
                            capabilityClient.getCapability(
                                References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE))
                        .nodes
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
        return capableNodes
    }

    /**
     * Gets a list of watches that are reachable, capable and not already registered.
     * @return A [List] of [Watch] objects that are ready to register.
     */
    suspend fun getAvailableWatches(): List<Watch>? {
        Timber.d("getAvailableWatches() called")
        return withContext(Dispatchers.IO) {
            val connectedNodes = getConnectedNodes()
            if (connectedNodes != null) {
                val availableWatches = ArrayList<Watch>()
                val capableNodes = getCapableNodes()
                val registeredWatches = getRegisteredWatches()
                return@withContext withContext(Dispatchers.Default) {
                    for (node in connectedNodes) {
                        if (registeredWatches.none { it.id == node.id }) {
                            val status = getWatchStatus(node.id, capableNodes)
                            availableWatches.add(Watch(node, status))
                        }
                    }
                    availableWatches
                }
            } else {
                Timber.w("Failed to get available watches")
                null
            }
        }
    }

    /**
     * Gets all registered watches, and finds their [WatchStatus]. Can be empty if no watches are
     * registered.
     * @return The [List] of [Watch] objects that are registered.
     */
    suspend fun getRegisteredWatches(): List<Watch> {
        Timber.d("getRegisteredWatches() called")
        return withContext(Dispatchers.IO) {
            val capableNodes = getCapableNodes()
            val connectedNodes = getConnectedNodes()
            val databaseWatches = database.watchDao().getAll()
            for (watch in databaseWatches) {
                watch.status = getWatchStatus(watch.id, capableNodes, connectedNodes)
            }
            return@withContext databaseWatches
        }
    }

    /**
     * Register a new [Watch], and let it know it's been registered.
     * @param watch The [Watch] to register.
     * @return true if the [Watch] was successfully registered, false otherwise.
     */
    suspend fun registerWatch(watch: Watch) {
        return withContext(Dispatchers.IO) {
            database.watchDao().add(watch)
            messageClient.sendMessage(
                watch.id,
                com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH,
                null)
        }
    }

    /**
     * Removes a watch from the database.
     * @param watchId The [Watch.id] to remove from the database.
     * @return true if the [Watch] was successfully removed, false otherwise.
     */
    suspend fun forgetWatch(watchId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                val success = watchPreferenceManager.clearPreferencesForWatch(watchId)
                if (success) {
                    database.watchDao().remove(watchId)
                    requestResetWatch(watchId)
                }
                return@withContext success
            }
            return@withContext false
        }
    }

    /**
     * Sends [REQUEST_RESET_APP] to the given [Watch].
     * @param watchId The [Watch.id] to send the message to.
     * @return The [Task] for the message send job.
     */
    fun requestResetWatch(watchId: String): Task<Int> {
        return messageClient.sendMessage(watchId, REQUEST_RESET_APP, null)
    }

    companion object {
        private var INSTANCE: WatchManager? = null

        /** Gets an instance of [WatchManager]. */
        fun get(context: Context): WatchManager {
            synchronized(this) {
                if (INSTANCE == null) INSTANCE = WatchManager(context)
                return INSTANCE!!
            }
        }
    }
}
