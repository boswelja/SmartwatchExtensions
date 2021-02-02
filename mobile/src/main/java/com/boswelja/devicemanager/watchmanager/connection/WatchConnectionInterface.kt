package com.boswelja.devicemanager.watchmanager.connection

import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.watchmanager.item.Watch

/**
 * An interface for managing connections with third party watch APIs
 */
interface WatchConnectionInterface {

    /**
     * Event that's fired whenever user-facing data is changed.
     */
    val dataChanged: Event

    /**
     * An observable list of all available watches.
     */
    val availableWatches: LiveData<List<Watch>>

    /**
     * Get the [Watch.Status] for a given [Watch].
     * @param watch The [Watch] to get the status of.
     * @param isRegistered Whether the given watch has been registered.
     * @return The [Watch.Status] for the given watch.
     */
    fun getWatchStatus(watch: Watch, isRegistered: Boolean): Watch.Status

    /**
     * Send a message to a watch with the given ID.
     * @param watchId See [Watch.id].
     * @param path The message path to send.
     * @param data The data to send with the message, if any.
     */
    fun sendMessage(watchId: String, path: String, data: ByteArray? = null)

    /**
     * Notify the watch a specified preference has been changed.
     * @param watch The target [Watch].
     * @param key The preference key to send to the watch.
     * @param value The new value of the preference.
     */
    fun updatePreferenceOnWatch(watch: Watch, key: String, value: Any)

    /**
     * Requests the given watch resets it's app.
     * @param watch The [Watch] to request a reset from.
     */
    fun resetWatchApp(watch: Watch)

    /**
     * Manually refresh info such as watch status and available watches.
     */
    fun refreshData()

    /**
     * Returns a string unique to the platform handling the connections. It's important this is
     * constant as it will be stored with registered watches.
     */
    fun getPlatformIdentifier(): String
}
