/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.collections.ArrayList

class WatchManager :
    Service(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val binder = Binder()

    private val watchConnectionListeners: ArrayList<WatchConnectionListener> = ArrayList()
    private val watchPreferenceChangeListeners: ArrayList<WatchPreferenceChangeListener> = ArrayList()
    private val coroutineScope = MainScope()

    private lateinit var database: WatchDatabase
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var capabilityClient: CapabilityClient
    private lateinit var dataClient: DataClient
    private lateinit var nodeClient: NodeClient
    private lateinit var messageClient: MessageClient

    private var watchConnectionListener: CapabilityClient.OnCapabilityChangedListener? = null

    var connectedWatch: Watch? = null
        private set

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AUTO_ADD_WATCHES_KEY -> {
                setAutoAddWatches()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate() called")

        database = WatchDatabase.get(applicationContext)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "").also {
            setConnectedWatchById(it ?: "")
        }

        capabilityClient = Wearable.getCapabilityClient(this)
        dataClient = Wearable.getDataClient(this)
        nodeClient = Wearable.getNodeClient(this)
        messageClient = Wearable.getMessageClient(this)

        setAutoAddWatches()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy() called")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        if (database.isOpen) database.close()
        if (watchConnectionListener != null) {
            capabilityClient.removeListener(watchConnectionListener!!)
        }
    }

    /**
     * Sets whether new watches should be automatically registered or not.
     * On enable, this will also check whether there are any capable & unregistered watches
     * already available and register them.
     */
    private fun setAutoAddWatches() {
        Timber.d("setAutoAddWatches() called")
        if (sharedPreferences.getBoolean(AUTO_ADD_WATCHES_KEY, false)) {
            Timber.i("Enabling auto-add watches")
            if (watchConnectionListener == null) {
                watchConnectionListener =
                    CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
                        for (node in capabilityInfo.nodes) {
                            Timber.i("Auto-registering a new watch")
                            ensureWatchRegistered(node)
                        }
                    }
            }
            capabilityClient.addListener(watchConnectionListener!!, References.CAPABILITY_WATCH_APP)

            Timber.i("Checking for any new watches to register")
            capabilityClient
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener {
                    for (node in it.nodes) {
                        Timber.i("Auto-registering a new watch")
                        ensureWatchRegistered(node)
                    }
                }
                .addOnFailureListener {
                    Timber.w("Failed to get capable watches")
                }
        } else if (watchConnectionListener != null) {
            capabilityClient.removeListener(watchConnectionListener!!)
        }
    }

    /**
     * Gets the status of a registered [Watch].
     * @param watchId The ID of the [Watch] to find a [WatchStatus] for.
     * @param capableNodes The [Set] of capable [Node] objects to check against. Default is null.
     * @param connectedNodes The [List] of connected [Node] objects to check against. Default is null.
     * @return A [WatchStatus] for the [Watch].
     */
    private fun getWatchStatus(
        watchId: String,
        capableNodes: Set<Node>? = null,
        connectedNodes: List<Node>? = null
    ): WatchStatus {
        return if (connectedNodes != null && capableNodes != null) {
            val isCapable = capableNodes.any { it.id == watchId }
            val isConnected = connectedNodes.any { it.id == watchId }
            if (isConnected && isCapable) {
                WatchStatus.CONNECTED
            } else if (isConnected && !isCapable) {
                WatchStatus.MISSING_APP
            } else {
                WatchStatus.DISCONNECTED
            }
        } else if (connectedNodes == null && capableNodes != null) {
            val isCapable = capableNodes.any { it.id == watchId }
            if (isCapable) {
                WatchStatus.NOT_REGISTERED
            } else {
                WatchStatus.MISSING_APP
            }
        } else {
            WatchStatus.ERROR
        }
    }

    /**
     * Get a [List] of connected [Node] objects.
     * @return The [List] of connected [Node] objects, or null if the task failed.
     */
    private suspend fun getConnectedNodes(): List<Node>? {
        Timber.d("getConnectedNodes() called")
        return try {
            withContext(Dispatchers.IO) {
                Tasks.await(nodeClient.connectedNodes)
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    /**
     * Get a [Set] of capable [Node] objects.
     * @return The [Set] of capable [Node] objects, or null if the task failed.
     */
    private suspend fun getCapableNodes(): Set<Node>? {
        Timber.d("getCapableNodes() called")
        var capableNodes: Set<Node>? = null
        try {
            withContext(Dispatchers.IO) {
                capableNodes = Tasks.await(
                    capabilityClient.getCapability(
                        References.CAPABILITY_WATCH_APP,
                        CapabilityClient.FILTER_REACHABLE
                    )
                ).nodes
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return capableNodes
    }

    /**
     * Sets all local [SharedPreferences] to their values stored with the connected [Watch],
     * or their default preference value.
     */
    private suspend fun updateLocalPreferences() {
        Timber.d("updateLocalPreferences() called")
        val watch = connectedWatch
        if (watch != null) {
            withContext(Dispatchers.IO) {
                clearLocalPreferences(commitNow = true)
                sharedPreferences.edit {
                    watch.boolPrefs.forEach { (key, value) ->
                        Timber.i("Setting $key to $value")
                        putBoolean(key, value)
                    }
                    watch.intPrefs.forEach { (key, value) ->
                        Timber.i("Setting $key to $value")
                        putInt(key, value)
                    }
                }
            }
        } else {
            Timber.w("Connected watch null, cannot continue.")
        }
    }

    /**
     * Removes all watch-specific preferences from the local [SharedPreferences].
     */
    private fun clearLocalPreferences(commitNow: Boolean = false) {
        Timber.d("deleteLocalPreferences($commitNow) called")
        sharedPreferences.edit(commit = commitNow) {
            remove(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY)
            remove(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY)
            remove(PreferenceKey.DND_SYNC_TO_PHONE_KEY)
            remove(PreferenceKey.DND_SYNC_TO_WATCH_KEY)
            remove(PreferenceKey.DND_SYNC_WITH_THEATER_KEY)
            remove(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
        }
    }

    /**
     * Make sure a given [Node] is registered as a watch. If not, register it.
     * @param node The [Node] to check.
     */
    private fun ensureWatchRegistered(node: Node) {
        Timber.d("ensureWatchRegistered() called")
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (database.watchDao().findById(node.id) == null) {
                    Timber.i("Watch not registered, registering")
                    val newWatch = Watch(node)
                    database.watchDao().add(newWatch)
                    withContext(Dispatchers.Main) {
                        for (connectionInterface in watchConnectionListeners) {
                            connectionInterface.onWatchAdded(newWatch)
                        }
                    }
                } else {
                    Timber.i("Watch already registered, skipping")
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    /**
     * Send all watch-specific preferences to the watch with the given ID.
     * @param watchId The ID of the [Watch] to get preferences from and send preferences to.
     * @return The [Task] created by the preference send job, or null if the task failed.
     */
    private suspend fun updateAllPreferencesOnWatch(watchId: String?): Task<DataItem>? {
        Timber.d("updateAllPreferencesOnWatch")
        if (!watchId.isNullOrEmpty()) {
            Timber.i("Creating update request")
            val watch = getWatchById(watchId)
            // Get updated preferences
            val batterySyncEnabled =
                watch?.boolPrefs?.get(PreferenceKey.BATTERY_SYNC_ENABLED_KEY) ?: false
            val phoneBatteryChargedNoti =
                watch?.boolPrefs?.get(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY) ?: false
            val watchBatteryChargedNoti =
                watch?.boolPrefs?.get(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY) ?: false
            val batteryChargeThreshold =
                watch?.intPrefs?.get(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY) ?: 90
            val dndSyncToWatch =
                watch?.boolPrefs?.get(PreferenceKey.DND_SYNC_TO_WATCH_KEY) ?: false
            val dndSyncToPhone =
                watch?.boolPrefs?.get(PreferenceKey.DND_SYNC_TO_PHONE_KEY) ?: false
            val dndSyncWithTheater =
                watch?.boolPrefs?.get(PreferenceKey.DND_SYNC_WITH_THEATER_KEY) ?: false
            val lockPhoneEnabled =
                watch?.boolPrefs?.get(PreferenceKey.PHONE_LOCKING_ENABLED_KEY) ?: false

            // Create PutDataMapRequest to send the new preferences
            PutDataMapRequest.create("/preference-change_$watchId").also {
                it.dataMap.apply {
                    putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
                    putBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
                    putBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, watchBatteryChargedNoti)
                    putInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, batteryChargeThreshold)
                    putBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, dndSyncToWatch)
                    putBoolean(PreferenceKey.DND_SYNC_TO_PHONE_KEY, dndSyncToPhone)
                    putBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_KEY, dndSyncWithTheater)
                    putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, lockPhoneEnabled)
                }
                // Send updated preferences
                it.setUrgent()
                return dataClient.putDataItem(it.asPutDataRequest())
            }
        } else {
            Timber.w("watchId null or empty")
        }
        return null
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
                Timber.e("Failed to get available watches")
                null
            }
        }
    }

    /**
     * Gets all registered watches, and finds their [WatchStatus].
     * @return The [List] of [Watch] objects that are registered, or null if the task failed.
     */
    suspend fun getRegisteredWatches(): List<Watch> {
        Timber.d("getRegisteredWatches() called")
        return withContext(Dispatchers.IO) {
            val capableNodes = getCapableNodes()
            val connectedNodes = getConnectedNodes()
            val databaseWatches = database.watchDao().getAll()
            for (watch in databaseWatches) {
                watch.status = getWatchStatus(watch.id, capableNodes, connectedNodes)

                database.boolPreferenceDao().getAllForWatch(watch.id).forEach {
                    watch.boolPrefs[it.key] = it.value
                }
                database.intPreferenceDao().getAllForWatch(watch.id).forEach {
                    watch.intPrefs[it.key] = it.value
                }
            }
            return@withContext databaseWatches
        }
    }

    /**
     * Gets a [Watch] by a given ID. Includes the watches preferences and current status.
     * @param watchId The ID of the [Watch] to get.
     * @return The [Watch] that was retrieved, or null if it doesn't exist.
     */
    suspend fun getWatchById(watchId: String?): Watch? {
        Timber.d("getWatchById($watchId) called")
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                Timber.i("Trying to get watch")
                if (watchId == connectedWatch?.id) {
                    return@withContext connectedWatch
                }
                val watch = database.watchDao().findById(watchId)
                if (watch != null) {
                    Timber.i("Getting watch prefs and status")
                    val boolPrefs =
                        database.boolPreferenceDao().getAllForWatch(watch.id)
                    val intPrefs =
                        database.intPreferenceDao().getAllForWatch(watch.id)
                    val capableNodes = getCapableNodes()
                    val connectedNodes = getConnectedNodes()
                    return@withContext withContext(Dispatchers.Default) {
                        watch.status = getWatchStatus(watch.id, capableNodes, connectedNodes)
                        for (intPreference in intPrefs) {
                            watch.intPrefs[intPreference.key] = intPreference.value
                        }
                        for (boolPreference in boolPrefs) {
                            watch.boolPrefs[boolPreference.key] = boolPreference.value
                        }
                        watch
                    }
                } else {
                    Timber.w("Watch null")
                }
            } else {
                Timber.w("watchId null or empty, or database not open")
            }
            null
        }
    }

    /**
     * Sends an updated preference to the connected [Watch].
     * @param preferenceKey The key of the preference to update on the watch.
     * @return The [Task] for the preference send job, or null if the task failed.
     */
    suspend fun updatePreferenceOnWatch(preferenceKey: String): Task<DataItem>? {
        Timber.d("updatePreferenceOnWatch($preferenceKey) called")
        return withContext(Dispatchers.IO) {
            val watch = connectedWatch
            if (watch != null) {
                val syncedPrefUpdateReq =
                    PutDataMapRequest.create("/preference-change_${watch.id}")
                when (preferenceKey) {
                    PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                    PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                    PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                    PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                    PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                    PreferenceKey.DND_SYNC_TO_WATCH_KEY,
                    PreferenceKey.DND_SYNC_WITH_THEATER_KEY
                    -> {
                        sharedPreferences.getBoolean(preferenceKey, false).also {
                            Timber.i("Updating $preferenceKey to $it")
                            syncedPrefUpdateReq.dataMap.putBoolean(preferenceKey, it)
                            watch.boolPrefs[preferenceKey] = it
                            BoolPreference(watch.id, preferenceKey, it).also { boolPreference ->
                                database.boolPreferenceDao().update(boolPreference)
                            }
                        }
                    }
                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                        sharedPreferences.getInt(preferenceKey, 90).also {
                            Timber.i("Updating $preferenceKey to $it")
                            syncedPrefUpdateReq.dataMap.putInt(preferenceKey, it)
                            watch.intPrefs[preferenceKey] = it
                            IntPreference(watch.id, preferenceKey, it).also { intPreference ->
                                database.intPreferenceDao().update(intPreference)
                            }
                        }
                    }
                }
                if (!syncedPrefUpdateReq.dataMap.isEmpty) {
                    Timber.i("Sending updated preference")
                    syncedPrefUpdateReq.setUrgent()
                    return@withContext dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
                } else {
                    Timber.w("No preference to update")
                }
            } else {
                Timber.w("Connected watch is null")
            }
            return@withContext null
        }
    }

    /**
     * Updates the stored [com.boswelja.devicemanager.batterysync.BatterySyncWorker] ID in the database.
     * @param watchId The [Watch.id] associated with the [com.boswelja.devicemanager.batterysync.BatterySyncWorker].
     * @param newWorkerId The new ID of the [com.boswelja.devicemanager.batterysync.BatterySyncWorker].
     * @return true if the value was successfully updated, false otherwise.
     */
    suspend fun updateBatterySyncWorkerId(watchId: String, newWorkerId: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                database.watchDao().updateBatterySyncWorkerId(watchId, newWorkerId)
                return@withContext true
            }
            return@withContext false
        }
    }

    /**
     * Update a given preference in the database for the connected watch.
     * @param preferenceKey The preference key to update.
     * @param newValue The new value of the preference.
     * @return true if the update was successful, false otherwise.
     */
    suspend fun updatePreferenceInDatabase(
        preferenceKey: String,
        newValue: Any
    ): Boolean {
        return updatePreferenceInDatabase(connectedWatch?.id, preferenceKey, newValue)
    }

    /**
     * Update a given preference in the database for a given [Watch].
     * @param watchId The ID of the [Watch] to update the preference for.
     * @param preferenceKey The preference key to update.
     * @param newValue The new value of the preference.
     * @return true if the update was successful, false otherwise.
     */
    suspend fun updatePreferenceInDatabase(
        watchId: String?,
        preferenceKey: String,
        newValue: Any
    ): Boolean {
        Timber.d("updatePreferenceInDatabase($watchId, $preferenceKey, $newValue) called")
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty()) {
                Timber.i("Updating preference")
                val isSuccessful =
                    database.updatePrefInDatabase(watchId, preferenceKey, newValue)
                if (isSuccessful && watchPreferenceChangeListeners.isNotEmpty()) {
                    for (watchPreferenceChangeListener in watchPreferenceChangeListeners) {
                        watchPreferenceChangeListener
                            .onWatchPreferenceChanged(watchId, preferenceKey, newValue)
                    }
                }
                return@withContext isSuccessful
            } else {
                Timber.w("watchId null or empty")
            }
            return@withContext false
        }
    }

    /**
     * Updates a watches name in the database.
     * @param watchId The ID of the [Watch] whose name we're updating.
     * @param newName The new name to set.
     * @return true if the update was successful, false otherwise.
     */
    suspend fun updateWatchName(watchId: String, newName: String): Boolean {
        if (database.isOpen) {
            withContext(Dispatchers.IO) {
                database.watchDao().setWatchName(watchId, newName)
            }
            return true
        }
        return false
    }

    /**
     * Register a new [Watch].
     * @param watch The [Watch] to register.
     * @return true if the [Watch] was successfully registered, false otherwise.
     */
    suspend fun registerWatch(watch: Watch): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext Utils.addWatch(database, messageClient, watch)
        }
    }

    /**
     * Removes a watch from the database.
     * @param watchId The ID of the [Watch] to remove.
     * @return true if the [Watch] was successfully removed, false otherwise.
     */
    suspend fun forgetWatch(watchId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                val success = clearPreferencesForWatch(watchId)
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
     * Clear all preferences for a given [Watch].
     * @param watchId The ID of the [Watch] to clear preferences for.
     * @return true if the preferences were successfully cleared, false otherwise.
     */
    suspend fun clearPreferencesForWatch(watchId: String?): Boolean {
        Timber.d("clearPreferencesForWatch($watchId) called")
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                Timber.i("Clearing watch preferences")
                database.intPreferenceDao().deleteAllForWatch(watchId)
                database.boolPreferenceDao().deleteAllForWatch(watchId)
                updateAllPreferencesOnWatch(watchId)
                if (watchId == connectedWatch?.id) {
                    clearLocalPreferences()
                }
                return@withContext true
            } else {
                Timber.w("watchId null or empty, or database closed")
            }
            return@withContext false
        }
    }

    /**
     * Get a list of all [BoolPreference] objects stored in the database with a specified key.
     * @param preferenceKey The key of the preference you're looking for.
     * @return An [Array] of [BoolPreference] objects, or null if the request failed.
     */
    suspend fun getBoolPrefsForWatches(preferenceKey: String): Array<BoolPreference>? {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                return@withContext database.boolPreferenceDao().getAllForKey(preferenceKey)
            }
            return@withContext null
        }
    }

    /**
     * Get a [BoolPreference] for a specified preference and watch.
     * @param watchId The ID of the [Watch] to get the preference for.
     * @param preferenceKey The key of the preference you're looking for.
     * @return The [BoolPreference], or null if the request failed.
     */
    suspend fun getBoolPrefForWatch(watchId: String, preferenceKey: String): BoolPreference? {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                return@withContext database.boolPreferenceDao().getWhere(watchId, preferenceKey)
            }
            return@withContext null
        }
    }

    /**
     * Get a [IntPreference] for a specified preference and watch.
     * @param watchId The ID of the [Watch] to get the preference for.
     * @param preferenceKey The key of the preference you're looking for.
     * @return The [IntPreference], or null if the request failed.
     */
    suspend fun getIntPrefForWatch(watchId: String, preferenceKey: String): IntPreference? {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                return@withContext database.intPreferenceDao().getWhere(watchId, preferenceKey)
            }
            return@withContext null
        }
    }

    /**
     * Sends the watch app reset message to a given watch.
     * @param watchId The ID of the watch to send the message to.
     * @return The [Task] for the message send job.
     */
    fun requestResetWatch(watchId: String): Task<Int> {
        return messageClient.sendMessage(watchId, REQUEST_RESET_APP, null)
    }

    /**
     * Add a new [WatchConnectionListener].
     */
    fun addWatchConnectionListener(connectionListener: WatchConnectionListener) {
        if (!watchConnectionListeners.contains(connectionListener)) {
            watchConnectionListeners.add(connectionListener)
        }
    }

    /**
     * Remove a [WatchConnectionListener].
     */
    fun removeWatchConnectionListener(connectionListener: WatchConnectionListener) {
        if (watchConnectionListeners.contains(connectionListener)) {
            watchConnectionListeners.remove(connectionListener)
        }
    }

    /**
     * Add a new [WatchPreferenceChangeListener].
     */
    fun addWatchPreferenceChangeListener(watchPreferenceChangeListener: WatchPreferenceChangeListener) {
        if (!watchPreferenceChangeListeners.contains(watchPreferenceChangeListener)) {
            watchPreferenceChangeListeners.add(watchPreferenceChangeListener)
        }
    }

    /**
     * Remove a [WatchPreferenceChangeListener].
     */
    fun removeWatchPreferenceChangeListener(watchPreferenceChangeListener: WatchPreferenceChangeListener) {
        if (watchPreferenceChangeListeners.contains(watchPreferenceChangeListener)) {
            watchPreferenceChangeListeners.remove(watchPreferenceChangeListener)
        }
    }

    /**
     * Sets the currently connected watch by a given [Watch.id].
     * @param watchId The ID of the [Watch] to set as connected.
     */
    fun setConnectedWatchById(watchId: String) {
        for (connectionInterface in watchConnectionListeners) {
            connectionInterface.onConnectedWatchChanging()
        }
        coroutineScope.launch(Dispatchers.IO) {
            val newWatch = getWatchById(watchId)
            val success = newWatch != null

            if (success) {
                connectedWatch = newWatch
                updateLocalPreferences()
                sharedPreferences.edit {
                    putString(LAST_CONNECTED_NODE_ID_KEY, watchId)
                }
            }

            withContext(Dispatchers.Main) {
                for (connectionInterface in watchConnectionListeners) {
                    connectionInterface.onConnectedWatchChanged(success)
                }
            }
        }
    }

    companion object {
        private const val AUTO_ADD_WATCHES_KEY = "auto_add_watches"

        const val LAST_CONNECTED_NODE_ID_KEY = "last_connected_id"

        /**
         * Binds the [WatchManager] to a [Connection].
         * This will auto-create the [WatchManager] if necessary.
         */
        fun bind(context: Context, watchManagerConnection: Connection) {
            Intent(context, WatchManager::class.java).also {
                context.bindService(it, watchManagerConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    inner class Binder : android.os.Binder() {
        val watchManager: WatchManager get() { return this@WatchManager }
    }

    abstract class Connection : ServiceConnection {
        abstract fun onWatchManagerBound(watchManager: WatchManager)
        abstract fun onWatchManagerUnbound()

        final override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            val watchManager = (binder as Binder).watchManager
            onWatchManagerBound(watchManager)
        }

        final override fun onServiceDisconnected(componentName: ComponentName?) {
            onWatchManagerUnbound()
        }
    }
}
