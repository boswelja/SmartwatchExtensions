package com.boswelja.devicemanager.watchconnectionmanager

import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.watchconnectionmanager.database.WatchDatabase
import com.google.android.gms.wearable.MessageClient

object Utils {

    fun addWatch(database: WatchDatabase, messageClient: MessageClient, watch: Watch): Boolean {
        if (database.addWatch(watch)) {
            messageClient.sendMessage(watch.id, References.WATCH_REGISTERED_PATH, null)
            return true
        }
        return false
    }
}