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
import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
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
    private val connectionManager: WearOSConnectionManager = WearOSConnectionManager(context),
    private val analytics: Analytics = Analytics(context),
    val database: WatchDatabase = WatchDatabase.get(context)
) {

    /**
     * The observable list of registered watches, saturated with watch statuses
     */
    val registeredWatches = database.watchDao().getAllObservable().switchMap {
        liveData {
            for (watch in it) {
                watch.status = connectionManager.getWatchStatus(watch, true)
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
    fun getAvailableWatches(): List<Watch> {
        Timber.d("getAvailableWatches() called")
        return connectionManager.getAvailableWatches()
    }

    /**
     * Gets all registered watches.
     * @return The [List] of [Watch] objects that are registered.
     */
    fun getRegisteredWatches(): List<Watch> {
        Timber.d("getRegisteredWatches() called")
        return registeredWatches.value!!
    }

    /**
     * Register a new [Watch], and let it know it's been registered.
     * @param watch The [Watch] to register.
     * @return true if the [Watch] was successfully registered, false otherwise.
     */
    suspend fun registerWatch(watch: Watch) {
        return withContext(Dispatchers.IO) {
            database.watchDao().add(watch)
            connectionManager.sendMessage(
                watch.id,
                References.WATCH_REGISTERED_PATH
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
     */
    fun requestResetWatch(watchId: String) {
        return connectionManager.sendMessage(watchId, REQUEST_RESET_APP)
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
