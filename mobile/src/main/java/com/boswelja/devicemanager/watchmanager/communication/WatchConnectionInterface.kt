package com.boswelja.devicemanager.watchmanager.communication

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
     * Get a [List] of [Watch]es that are connected, and have Wearable Extensions installed
     */
    fun getAvailableWatches(): List<Watch>

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
     * Returns a string unique to the platform handling the connections. It's important this is
     * constant as it will be stored with registered watches.
     */
    fun getPlatformIdentifier(): String
}
