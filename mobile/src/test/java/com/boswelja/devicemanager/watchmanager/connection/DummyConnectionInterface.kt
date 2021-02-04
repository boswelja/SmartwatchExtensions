package com.boswelja.devicemanager.watchmanager.connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.watchmanager.item.Watch

/**
 * A dummy [WatchConnectionInterface] for testing purposes.
 */
class DummyConnectionInterface : WatchConnectionInterface {

    override val dataChanged: Event = Event()
    override val availableWatches: LiveData<List<Watch>> = MutableLiveData()
    override val platformIdentifier: String = PLATFORM

    override fun getWatchStatus(watch: Watch, isRegistered: Boolean): Watch.Status {
        return Watch.Status.UNKNOWN
    }

    override fun sendMessage(watchId: String, path: String, data: ByteArray?) {}

    override fun updatePreferenceOnWatch(watch: Watch, key: String, value: Any) {}

    override fun resetWatchApp(watch: Watch) {}

    override fun refreshData() {}

    companion object {
        const val PLATFORM = "dummy"
    }
}