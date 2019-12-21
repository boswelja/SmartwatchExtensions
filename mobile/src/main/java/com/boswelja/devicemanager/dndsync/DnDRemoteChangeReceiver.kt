/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.util.Log
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class DnDRemoteChangeReceiver : WearableListenerService() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private var watchConnectionManager: WatchConnectionService? = null

    override fun onCreate() {
        super.onCreate()
        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnectionManagerConnection)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)
        if (watchConnectionManager != null) {
            MainScope().launch {
                val dndSyncReceiveEnabledPrefs = watchConnectionManager!!.getBoolPrefsForRegisteredWatches(PreferenceKey.DND_SYNC_TO_PHONE_KEY)

                if (!dndSyncReceiveEnabledPrefs.isNullOrEmpty()) {
                    val dndStatePerWatch = HashMap<String, Boolean>()
                    for (dataEvent in dataEventBuffer) {
                        val senderId = dataEvent.dataItem.uri.host!!
                        val isDnDSyncReceiveEnabledForWatch = dndSyncReceiveEnabledPrefs.firstOrNull { it.watchId == senderId }?.value
                        if (isDnDSyncReceiveEnabledForWatch == true) {
                            val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                            val dndEnabled = dataMap.getBoolean(NEW_DND_STATE_KEY)
                            dndStatePerWatch[senderId] = dndEnabled
                        }
                    }
                    if (dndStatePerWatch.isNotEmpty()) {
                        var shouldEnableDnD = false
                        for (watchState in dndStatePerWatch) {
                            if (watchState.value) {
                                shouldEnableDnD = true
                                break
                            }
                        }
                        Utils.setInterruptionFilter(this@DnDRemoteChangeReceiver, shouldEnableDnD)
                    }
                } else {
                    Log.e("DnDRemoteChangeReceiver", "dndSyncReceiveEnabledPrefs.isNullOrEmpty()")
                }
            }
        } else {
            Log.e("DnDRemoteChangeReceiver", "watchConnectionManager == null")
        }
    }

    companion object {
        const val NEW_DND_STATE_KEY = "${References.packageName}.dnd-enabled"
    }
}
