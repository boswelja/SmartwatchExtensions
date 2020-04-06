/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
