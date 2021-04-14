package com.boswelja.smartwatchextensions.watchmanager.connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.smartwatchextensions.common.Event
import com.boswelja.smartwatchextensions.watchmanager.item.Watch

/**
 * A dummy [WatchConnectionInterface] for testing purposes.
 */
class DummyConnectionInterface(platform: String) : WatchConnectionInterface {

    val mutableAvailableWatches = MutableLiveData<List<Watch>>()
    var mutableWatchCapabilities: Map<String, Short> = emptyMap()

    override val watchCapabilities: Map<String, Short>
        get() = mutableWatchCapabilities
    override val dataChanged: Event = Event()
    override val availableWatches: LiveData<List<Watch>>
        get() = mutableAvailableWatches

    override val platformIdentifier: String = platform

    override fun getWatchStatus(watchId: String, isRegistered: Boolean): Watch.Status {
        return Watch.Status.UNKNOWN
    }

    override fun sendMessage(watchId: String, path: String, data: ByteArray?) {}

    override fun updatePreferenceOnWatch(watchId: String, key: String, value: Any) {}

    override fun refreshData() {}
}
