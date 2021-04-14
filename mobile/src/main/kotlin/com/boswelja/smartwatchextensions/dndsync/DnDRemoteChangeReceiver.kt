package com.boswelja.smartwatchextensions.dndsync

import com.boswelja.smartwatchextensions.common.Compat.setInterruptionFilter
import com.boswelja.smartwatchextensions.common.dndsync.References.NEW_DND_STATE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        val watchDndStates = ArrayList<Boolean>()
        for (dataEvent in dataEventBuffer) {
            val dndEnabled = getDnDStateFromDataEvent(dataEvent)
            watchDndStates.add(dndEnabled)
        }
        val shouldEnableDnD = watchDndStates.any { dndEnabled -> dndEnabled }
        val success = setInterruptionFilter(this, shouldEnableDnD)
        if (!success) {
            WatchSettingsDatabase.getInstance(this).boolPrefDao().apply {
                updateAllForKey(
                    PreferenceKey.DND_SYNC_TO_PHONE_KEY, false
                )
                updateAllForKey(
                    PreferenceKey.DND_SYNC_WITH_THEATER_KEY, false
                )
            }
        }
    }

    /**
     * Gets the Do not Disturb state from a [DataEvent].
     * @param dataEvent The [DataEvent] to read from.
     * @return true if Do not Disturb is enabled, false otherwise.
     */
    private fun getDnDStateFromDataEvent(dataEvent: DataEvent): Boolean {
        Timber.i("getDnDStateFromDataEvent() called")
        val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
        return dataMap.getBoolean(NEW_DND_STATE_KEY)
    }
}
