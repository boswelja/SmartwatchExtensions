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
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.watchmanager.Utils.getCapableNodes
import com.boswelja.devicemanager.watchmanager.Utils.getConnectedNodes
import com.boswelja.devicemanager.watchmanager.Utils.getWatchStatus
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for managing registered watches, finding new watches and updating
 * local SharedPreferences when the connected watch changes.
 */
class WatchManager internal constructor(
    context: Context,
    private val watchPreferenceManager: WatchPreferenceManager =
        WatchPreferenceManager.get(context),
    private val selectedWatchHandler: SelectedWatchHandler = SelectedWatchHandler.get(context),
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context),
    private val nodeClient: NodeClient = Wearable.getNodeClient(context),
    private val messageClient: MessageClient = Wearable.getMessageClient(context),
    private val analytics: Analytics = Analytics(context),
    val database: WatchDatabase = WatchDatabase.get(context)
) {

    /**
     * The observable list of registered watches, saturated with watch statuses
     */
    val registeredWatches = database.watchDao().getAllObservable().switchMap {
        liveData {
            val capableNodes = getCapableNodes(capabilityClient)
            val connectedNodes = getConnectedNodes(nodeClient)
            for (watch in it) {
                watch.status = getWatchStatus(watch.id, database, capableNodes, connectedNodes)
            }
            emit(it)
        }
    }

    val connectedWatch: LiveData<Watch?>
        get() = selectedWatchHandler.selectedWatch

    init {
        connectedWatch.observeForever {
            it?.let { watch -> watchPreferenceManager.updateLocalPreferences(watch.id) }
        }
    }

    /**
     * Gets a list of watches that are reachable, capable and not already registered.
     * @return A [List] of [Watch] objects that are ready to register.
     */
    suspend fun getAvailableWatches(): List<Watch>? {
        Timber.d("getAvailableWatches() called")
        return withContext(Dispatchers.IO) {
            val connectedNodes = getConnectedNodes(nodeClient)
            if (connectedNodes != null) {
                val availableWatches = ArrayList<Watch>()
                val capableNodes = getCapableNodes(capabilityClient)
                val registeredWatches = getRegisteredWatches()
                return@withContext withContext(Dispatchers.Default) {
                    for (node in connectedNodes) {
                        if (registeredWatches.none { it.id == node.id }) {
                            val status = getWatchStatus(node.id, database, capableNodes)
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
            val capableNodes = getCapableNodes(capabilityClient)
            val connectedNodes = getConnectedNodes(nodeClient)
            val databaseWatches = database.watchDao().getAll()
            for (watch in databaseWatches) {
                watch.status = getWatchStatus(watch.id, database, capableNodes, connectedNodes)
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
                null
            )
            analytics.logWatchRegistered()
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
                    analytics.logWatchRemoved()
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
