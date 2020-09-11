/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlin.collections.ArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchManager private constructor(context: Context) {

  private val coroutineJob = Job()
  private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  private val watchPreferenceManager = WatchPreferenceManager(context)

  private val capabilityClient = Wearable.getCapabilityClient(context)
  private val nodeClient = Wearable.getNodeClient(context)
  private val messageClient = Wearable.getMessageClient(context)

  private var watchConnectionListener: CapabilityClient.OnCapabilityChangedListener? = null

  val database = WatchDatabase.get(context)

  private val _connectedWatch = MutableLiveData<Watch?>()
  val connectedWatch: LiveData<Watch?>
    get() = _connectedWatch

  init {
    sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "")?.let { setConnectedWatchById(it) }
    connectedWatch.observeForever {
      it?.let { watch ->
        sharedPreferences.edit { putString(LAST_CONNECTED_NODE_ID_KEY, watch.id) }
        watchPreferenceManager.updateLocalPreferences(watch.id)
      }
    }
  }

  fun destroy() {
    INSTANCE = null
    if (watchConnectionListener != null) {
      capabilityClient.removeListener(watchConnectionListener!!)
    }
    coroutineJob.cancel()
  }

  /**
   * Gets the status of a registered [Watch].
   * @param watchId The ID of the [Watch] to find a [WatchStatus] for.
   * @param capableNodes The [Set] of capable [Node] objects to check against. Default is null.
   * @param connectedNodes The [List] of connected [Node] objects to check against. Default is null.
   * @return A [WatchStatus] for the [Watch].
   */
  private fun getWatchStatus(
      watchId: String, capableNodes: Set<Node>? = null, connectedNodes: List<Node>? = null
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
      withContext(Dispatchers.IO) { Tasks.await(nodeClient.connectedNodes) }
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
        capableNodes =
            Tasks.await(
                    capabilityClient.getCapability(
                        References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE))
                .nodes
      }
    } catch (e: Exception) {
      Timber.e(e)
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
      }
      return@withContext databaseWatches
    }
  }

  /**
   * Sends an updated preference to the connected [Watch].
   * @param preferenceKey The key of the preference to update on the watch.
   * @return The [Task] for the preference send job, or null if the task failed.
   */
  suspend fun updatePreferenceOnWatch(preferenceKey: String): Task<DataItem>? {
    Timber.d("updatePreferenceOnWatch($preferenceKey) called")
    return watchPreferenceManager.updatePreferenceOnWatch(connectedWatch.value!!.id, preferenceKey)
  }

  /**
   * Update a given preference in the database for the connected watch.
   * @param preferenceKey The preference key to update.
   * @param newValue The new value of the preference.
   * @return true if the update was successful, false otherwise.
   */
  suspend fun updatePreferenceInDatabase(preferenceKey: String, newValue: Any): Boolean {
    return withContext(Dispatchers.IO) {
      connectedWatch.value?.let {
        return@withContext database.updatePrefInDatabase(it.id, preferenceKey, newValue)
      }
      return@withContext false
    }
  }

  /**
   * Register a new [Watch].
   * @param watch The [Watch] to register.
   * @return true if the [Watch] was successfully registered, false otherwise.
   */
  suspend fun registerWatch(watch: Watch) {
    return withContext(Dispatchers.IO) {
      database.watchDao().add(watch)
      messageClient.sendMessage(
          watch.id, com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH, null)
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
      val isSuccessful = watchPreferenceManager.clearPreferencesForWatch(watchId)
      if (isSuccessful && watchId == connectedWatch.value?.id) {
         watchPreferenceManager.clearLocalPreferences()
      }
      return@withContext isSuccessful
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
   * Sets the currently connected watch by a given [Watch.id].
   * @param watchId The ID of the [Watch] to set as connected.
   */
  fun setConnectedWatchById(watchId: String) {
    coroutineScope.launch {
      val newWatch = database.watchDao().get(watchId)
      _connectedWatch.postValue(newWatch)
    }
  }

  companion object {
    private var INSTANCE: WatchManager? = null

    const val LAST_CONNECTED_NODE_ID_KEY = "last_connected_id"

    /** Gets an instance of [WatchManager]. */
    fun get(context: Context): WatchManager {
      synchronized(this) {
        if (INSTANCE == null) INSTANCE = WatchManager(context)
        return INSTANCE!!
      }
    }
  }
}
